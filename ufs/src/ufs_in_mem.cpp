#include <unistd.h>

#include "ufs_in_mem.h"
#include "report.h"
#include "inv_log.h"
#include "db.h"

#include "rapidjson/writer.h"
#include "rapidjson/stringbuffer.h"
#include "rapidjson/error/en.h"
#include"ssdb_io_sync.h"
#include"ufs_json_define.h"

using namespace inv;
using namespace inv::monitor;

/*cacheSize = cacheNum / threadNum = 20000/ 30 */
UfsInMem::UfsInMem(size_t cacheSize, const std::string& hashCategory,
        const std::string& hashTag, const std::string& hashStatsCategory,
        const std::string& hashStatsTag, const std::vector<inv::INV_Redis*>& vecRedis,
        const std::vector<inv::INV_Redis*>& vecWRedis, int32_t writeCold,
        const std::string& categoryClickAndImpHashName)
    :cacheSize_(cacheSize), hashCategory_(hashCategory), hashTag_(hashTag),
    hashStatsCategory_(hashStatsCategory), 
    hashStatsTag_(hashStatsTag),
    hashCategoryClickAndImp_(categoryClickAndImpHashName),
    tagCache_(cacheSize_), 
    categoryCache_(cacheSize_/4),
    tagStatsCache_(cacheSize_/8), 
    categoryStatsCache_(cacheSize_/8),
    categoryClickAndImpCache_(cacheSize_/4),
    m_cacheClickAndImp(cacheSize_/32),
    //m_cacheClickAndImp(1),
	ssdb_(new SsdbIo(vecRedis, vecWRedis, writeCold))
    //m_ssdbSync(nullptr)
{
    //m_ssdbSync = new SsdbIoSync(ssdbSync); 
    this->valueBuffer_ = new char[this->jsonBufSize_];
    this->parseBuffer_ = new char[this->jsonBufSize_];
}

UfsInMem::~UfsInMem()
{
    if (ssdb_ != nullptr) {
        delete ssdb_;
        ssdb_ = nullptr;
    }

#if 0
    if (m_ssdbSync != nullptr) {
        delete m_ssdbSync;
        m_ssdbSync = nullptr;
    }
#endif
};

int32_t UfsInMem::GetUfsWeightedTag(std::shared_ptr<WU>& out, const std::string& version, const std::string& uid,
        double decayRate, int32_t decayPeriod)
{
    FUNC_GUARD();
    // out.clear();
    const std::string key = version + "|" + uid;
    ReportIncr("zhizi.ufs.wt.get");
    if (this->tagCache_.Get(out, key) != 0) // not hit
    {
        std::string json;
        std::string hash = this->GetUfsHashName("tag", version);
        int ret = this->ssdb_->GetSsdbJson(json, hash, uid);
        if (ret != 0)
        {
            return -1;
        }
        if (json.empty())
        {
            return 1;
        }

        ret = this->JsonWeight2Map(out, json, decayRate, decayPeriod);
        if (ret != 0)
        {
            return -1;
        }
        this->tagCache_.Set(key, out);
    }
    else
    {
        ReportIncr("zhizi.ufs.wt.hit");
    }
    return 0;
}

int32_t UfsInMem::GetCategoryClickAndImp(std::shared_ptr<MCG>& out, const std::string& version, const std::string& uid,
        double decayRate, int32_t decayPeriod)
{
    //FUNC_GUARD();
    // out.clear();
    const std::string key = version + "|" + uid;
    ReportIncr("zhizi.ufs.cg.get");
    if (this->categoryClickAndImpCache_.Get(out, key) != 0)
    {
        std::string json;
        std::string hashname = this->GetCategoryClickAndImpHashName(version);
        int ret = this->ssdb_->GetSsdbJson(json, hashname, uid);
        if (ret != 0)
        {
            return -1;
        }
        if (json.empty())
        {
            return 1;
        }

        ret = this->JsonCategoryClickAndImp2Map(out, json, decayRate, decayPeriod);
        if (ret != 0)
        {
            return -1;
        }
        this->categoryClickAndImpCache_.Set(key, out);
    }
    else
    {
        ReportIncr("zhizi.ufs.cg.hit");
    }
    return 0;
}

//合并新增数据和历史数据
int32_t UfsInMem::MergeCategoryClickOrImp(const std::string& type, const std::string& version, const std::string& uid, double decayRate, int32_t decayPeriod, int32_t eventTime, std::map<std::string, double>& categoryMerged)
{
    //FUNC_GUARD();
    std::shared_ptr<MCG> historyMapCategoryClickAndImp(new MCG);
    this->GetCategoryClickAndImp(historyMapCategoryClickAndImp, version, uid, decayRate, decayPeriod);
    double decayFactor = UfsInMem::GetDecayFactor(time(NULL), eventTime, decayRate, decayPeriod);

    for (auto it = categoryMerged.begin(); it != categoryMerged.end(); ++it)
    {
        if (historyMapCategoryClickAndImp->count(it->first) <= 0)
        {
            if(type=="click")
            {
                historyMapCategoryClickAndImp->insert(std::make_pair(it->first, std::make_pair(decayFactor, 0.0)));
            }
            else
            {
                historyMapCategoryClickAndImp->insert(std::make_pair(it->first, std::make_pair(0.0, decayFactor)));
            }
        }
        else
        {
            if(type=="click")
            {
                (*historyMapCategoryClickAndImp)[it->first].first += decayFactor;
            }
            else
            {
                (*historyMapCategoryClickAndImp)[it->first].second += decayFactor;
            }
        }
    }
    this->SetCategoryClickOrImp(type, version, uid, historyMapCategoryClickAndImp);
    return 0;
}

int UfsInMem::ParseCacheFromSsdb(const DocumentType& d, std::unordered_map<std::string, std::array<double,2>>& out, double decayFactor, bool tsFlag) {
    rapidjson::Value::ConstMemberIterator data = d.FindMember("data");
    if (data == d.MemberEnd()) {
        return 1;
    }

    if (!data->value.IsArray()) {
        return 1;
    }

    rapidjson::SizeType size = data->value.Size();
    if (!tsFlag) {
        for (rapidjson::SizeType i = 0; i < size; ++i) {
            if (!data->value[i].IsObject() || 3 != data->value[i].MemberCount()) {
                continue;
            }

            rapidjson::Value::ConstMemberIterator it;
            rapidjson::Value::ConstMemberIterator end = data->value[i].MemberEnd(); 

            if ((it = data->value[i].FindMember("key")) != end && it->value.IsString()) {
                std::string key = it->value.GetString();
                double click = 0;
                double imp = 0;
                if ((it = data->value[i].FindMember("click")) != end && it->value.IsDouble()) {
                    click = it->value.GetDouble();
                }

                if ((it = data->value[i].FindMember("imp")) != end && it->value.IsDouble()) {
                    imp = it->value.GetDouble();
                }

                /* because get from ssdb, we need decay them */
                std::array<double, 2> arr = {click * decayFactor, imp * decayFactor};
                out.emplace(std::move(key), std::move(arr));
            }
        }
    } else {
        for (rapidjson::SizeType i = 0; i < size; ++i) {
            if (!data->value[i].IsObject() || 3 != data->value[i].MemberCount()) {
                continue;
            }

            rapidjson::Value::ConstMemberIterator it;
            rapidjson::Value::ConstMemberIterator end = data->value[i].MemberEnd(); 

            if ((it = data->value[i].FindMember("key")) != end && it->value.IsString()) {
                std::string key = it->value.GetString();
                double click = 0;
                double imp = 0;
                if ((it = data->value[i].FindMember("click")) != end && it->value.IsDouble()) {
                    click = it->value.GetDouble();
                }

                if ((it = data->value[i].FindMember("imp")) != end && it->value.IsDouble()) {
                    imp = it->value.GetDouble();
                }

                /* because get from ssdb, we need decay them */
                std::array<double, 2> arr = {click, imp};
                out.emplace(std::move(key), std::move(arr));
            }
        }
    }

    return 0;
}

int UfsInMem::ParseCacheFromSsdb(int type, DocumentType& d, std::unordered_map<std::string, std::array<double,2>>& out, double decayFactor, std::unordered_map<std::string, std::array<double, 2>>* outOfImp) {
    rapidjson::Value::ConstMemberIterator data = d.FindMember("data");
    if (data == d.MemberEnd()) {
        return 1;
    }

    if (!data->value.IsArray()) {
        return 1;
    }

    rapidjson::SizeType size = data->value.Size();
    do {
        if (type == EVENT_CLICK) {
            for (rapidjson::SizeType i = 0; i < size; ++i) {
                if (!data->value[i].IsObject()) {
                    continue;
                }

                rapidjson::Value* keyOfClick = GetValueByPointer(d, VideoJsonFieldTableStr[i][0]);
                rapidjson::Value* clickOfClick = GetValueByPointer(d, VideoJsonFieldTableStr[i][1]);
                rapidjson::Value* impOfClick = GetValueByPointer(d, VideoJsonFieldTableStr[i][2]);

                if (keyOfClick && clickOfClick) {
                    /* if imp exists, this must be old-style: key, click and imp
                     * because the new-style contain key and click/imp*/
                    if (impOfClick) {
                        std::string key = keyOfClick->GetString();
                        double click = clickOfClick->GetDouble();
                        double imp = impOfClick->GetDouble();

                        /* because get from ssdb, we need decay them */
                        std::array<double, 2> arr = {click * decayFactor, 0};
                        out.emplace(key, std::move(arr));

                        /* should insert the imp into redis */
                        std::array<double, 2> arrOfImp = { 0, imp * decayFactor};
                        outOfImp->emplace(std::move(key), std::move(arrOfImp));
                    } else {
                        std::string key = keyOfClick->GetString();
                        double click = clickOfClick->GetDouble();

                        /* because get from ssdb, we need decay them */
                        std::array<double, 2> arr = {click * decayFactor, 0};
                        out.emplace(std::move(key), std::move(arr));
                    }
                }
            }

            break;
        }

        if (type == EVENT_IMP) {
            for (rapidjson::SizeType i = 0; i < size; ++i) {
                if (!data->value[i].IsObject()) {
                    continue;
                }

                rapidjson::Value* keyOfImp = GetValueByPointer(d, VideoJsonFieldTableStr[i][0]);
                rapidjson::Value* impOfImp = GetValueByPointer(d, VideoJsonFieldTableStr[i][2]);

                if (keyOfImp && impOfImp) {
                    std::string key = keyOfImp->GetString();
                    double imp = impOfImp->GetDouble();

                    /* because get from ssdb, we need decay them */
                    std::array<double, 2> arr = {0, imp * decayFactor};
                    out.emplace(std::move(key), std::move(arr));
                }
            }
            break;
        }
    } while (false);

    return 0;
}

int UfsInMem::ParseJsonsClickAndImp(std::shared_ptr<NEWCACHE>& out, std::vector<std::string>& jsons, double decayRate, int decayPeriod) {
    FUNC_GUARD();
    /**  all formats like
      {
      "ts": 1504751055,
      "data": [
      {
      "key": "103",
      "click": 58.609,
      "imp": 423.67
      },
      {
      "key": "107",
      "click": 58.609,
      "imp": 323.67
      }
      ]
      }
      */
    /* parse publisher */
    for (int i=0; i < TQUERY_FIELDS_SIZE; ++i) {
        rapidjson::MemoryPoolAllocator<> valueAllocator(this->valueBuffer_, jsonBufSize_);
        rapidjson::MemoryPoolAllocator<> parseAllocator(this->parseBuffer_, jsonBufSize_);
        DocumentType d(&valueAllocator, jsonBufSize_, &parseAllocator);

        d.Parse(jsons[i].c_str());
        if (d.HasParseError() || !d.IsObject()) {
            TLOGINFO(__FILE__<<"-"<<__LINE__<<" cache click_and_imp json parse fail: "<< d.GetErrorOffset() 
                    << ": " << (rapidjson::GetParseError_En(d.GetParseError())) << endl);

            return -1;
        }

        if (d.FindMember("data") == d.MemberEnd()) {
            return 1;
        }

        //double decayFactor = GetDecayFactor(time(NULL), d.FindMember("ts")->value.GetInt(), decayRate, decayPeriod);
        int ts = d.FindMember("ts")->value.GetInt();
        double decayFactor = GetDecayFactor(time(NULL), ts, decayRate, decayPeriod);
        std::unordered_map<std::string, std::array<double, 2>> elem;
        //ParseCacheFromSsdb(d, elem, decayFactor);
        //if (i == TQUERY_UFS_TS) {
        if (i == TQUERY_FIELDS_SIZE) {
            ParseCacheFromSsdb(d, elem, decayFactor, true);
        } else {
            ParseCacheFromSsdb(d, elem, decayFactor, false);
        }

        if (!elem.empty()) {
            out->emplace(std::make_pair(i, std::move(elem)));
        }
    }

    return 0;
}

int UfsInMem::ParseJsonsClickAndImp(int type, std::shared_ptr<NEWCACHE>& out, std::vector<std::string>& jsons, double decayRate, int decayPeriod) {
    FUNC_GUARD();
    /**  all formats like
      {
      "ts": 1504751055,
      "data": [
      {
      "key": "103",
      "click": 58.609,
      "imp": 423.67
      },
      {
      "key": "107",
      "click": 58.609,
      "imp": 323.67
      }
      ]
      }
      */
    /* parse publisher, keywords, and topics */
    do {
        if (type == EVENT_CLICK) {
            for (int i=0; i < TQUERY_FIELDS_SIZE; ++i) {
                rapidjson::MemoryPoolAllocator<> valueAllocator(this->valueBuffer_, jsonBufSize_);
                rapidjson::MemoryPoolAllocator<> parseAllocator(this->parseBuffer_, jsonBufSize_);
                DocumentType d(&valueAllocator, jsonBufSize_, &parseAllocator);

                d.Parse(jsons[i].c_str());
                if (d.HasParseError() || !d.IsObject()) {
                    TLOGINFO(__FILE__<<"-"<<__LINE__<<" cache click_and_imp json parse fail: "<< d.GetErrorOffset() 
                            << ": " << (rapidjson::GetParseError_En(d.GetParseError())) << endl);

                    return -1;
                }

                if (d.FindMember("data") == d.MemberEnd()) {
                    return 1;
                }

                //double decayFactor = GetDecayFactor(time(NULL), d.FindMember("ts")->value.GetInt(), decayRate, decayPeriod);
                int ts = d.FindMember("ts")->value.GetInt();
                double decayFactor = GetDecayFactor(time(NULL), ts, decayRate, decayPeriod);
                std::unordered_map<std::string, std::array<double, 2>> elem;
                std::unordered_map<std::string, std::array<double, 2>> elemOfImp;
                ParseCacheFromSsdb(type, d, elem, decayFactor, &elemOfImp);

                if (!elem.empty()) {
                    out->emplace(std::make_pair(i, std::move(elem)));
                }

                if (!elemOfImp.empty()) {
                    out->emplace(std::make_pair(i+TQUERY_FIELDS_SIZE, std::move(elemOfImp)));
                }
            }

            break;
        } 

        if (type == EVENT_IMP) {
            for (int i=0; i < TQUERY_FIELDS_SIZE; ++i) {
                rapidjson::MemoryPoolAllocator<> valueAllocator(this->valueBuffer_, jsonBufSize_);
                rapidjson::MemoryPoolAllocator<> parseAllocator(this->parseBuffer_, jsonBufSize_);
                DocumentType d(&valueAllocator, jsonBufSize_, &parseAllocator);

                d.Parse(jsons[i].c_str());
                if (d.HasParseError() || !d.IsObject()) {
                    TLOGINFO(__FILE__<<"-"<<__LINE__<<" cache click_and_imp json parse fail: "<< d.GetErrorOffset() 
                            << ": " << (rapidjson::GetParseError_En(d.GetParseError())) << endl);

                    return -1;
                }

                if (d.FindMember("data") == d.MemberEnd()) {
                    return 1;
                }

                //double decayFactor = GetDecayFactor(time(NULL), d.FindMember("ts")->value.GetInt(), decayRate, decayPeriod);
                int ts = d.FindMember("ts")->value.GetInt();
                double decayFactor = GetDecayFactor(time(NULL), ts, decayRate, decayPeriod);
                std::unordered_map<std::string, std::array<double, 2>> elem;
                ParseCacheFromSsdb(type, d, elem, decayFactor);

                if (!elem.empty()) {
                    out->emplace(std::make_pair(i, std::move(elem)));
                }
            }

            break;
        } 
    } while(false);

    return 0;
}

int UfsInMem::SerializeCacheClickAndImpJson(std::vector<std::string>& outs, std::shared_ptr<NEWCACHE>& in) {
    //FUNC_GUARD();
    /* capacity is 2000 */
    static const int SIZE = 1000;

    //using NEWCACHE_PAIR = std::pair<std::string, std::array<double, 2>>;
    std::vector<std::vector<NEWCACHE_PAIR>> vs;
    vs.reserve(TQUERY_FIELDS_SIZE);
    for (int i = 0; i < TQUERY_FIELDS_SIZE; ++i) {
        std::vector<NEWCACHE_PAIR> v((*in)[i].begin(), (*in)[i].end());
        std::sort(v.begin(), v.end(), NewCacheCompare());
        if (v.size() > SIZE) {
            /*remove elem start position 20000 */
            v.resize(SIZE);
            //v.shrink_to_fit();
        }
        vs.emplace_back(std::move(v));
    }

    //int jsonTs = (*in)[TQUERY_UFS_TS][TQueryTable[TQUERY_UFS_TS].C_STR()][0];
    int jsonTs = (*in)[TQUERY_FIELDS_SIZE][TQueryTable[TQUERY_FIELDS_SIZE].C_STR()][0];
    for (int i = 0; i < TQUERY_FIELDS_SIZE; ++i) {
        rapidjson::StringBuffer s;
        rapidjson::Writer<rapidjson::StringBuffer> writer(s);
        writer.SetMaxDecimalPlaces(3);
        writer.StartObject();
        writer.Key("ts");
        //writer.Int(time(NULL));
        writer.Int(jsonTs);
        writer.Key("data");
        writer.StartArray();

        auto end = vs[i].end();
        for (auto it = vs[i].begin(); it != end; ++it) {
            writer.StartObject();
            writer.Key("key");
            writer.String(it->first.c_str());
            writer.Key("click");
            writer.Double(it->second[0]);
            writer.Key("imp");
            writer.Double(it->second[1]);
            writer.EndObject();
        }

        writer.EndArray();
        writer.EndObject();
        assert(writer.IsComplete());
        outs.emplace_back(s.GetString());
    }

    return 0;
}

int UfsInMem::SerializeCacheClickAndImpJson(int type, const std::string& uid, std::vector<std::string>& outs, std::shared_ptr<NEWCACHE>& in) {
    //FUNC_GUARD();
    /* capacity is 2000 */
    static const int SIZE = 1000;
    std::vector<std::vector<NEWCACHE_PAIR>> vs;

    size_t outsSize = TQUERY_FIELDS_SIZE;
    if (in->rbegin()->first >TQUERY_FIELDS_SIZE) {
        outsSize = TQUERY_OVERFLOW;
        if (in->size() != outsSize) {
            auto end = in->end();
            std::string str;
            for (auto it = in->begin(); it != end; ++it) {
                str.append(TQueryTable[it->first].C_STR()).append(", ");
            }
            FDLOG(EventTypeTableStr[type][0].c_str())<<"uid="<<uid<<"|"<<str<<std::endl;
        }
    }
    vs.reserve(outsSize);

    switch(outsSize) {
        case TQUERY_OVERFLOW: /* just click have six elems: publisher(imp), topics(imp), keywords(imp), */
            {
                for (size_t i = TQUERY_FIELDS_SIZE; i < outsSize; ++i) {
                    std::vector<NEWCACHE_PAIR> v((*in)[i].begin(), (*in)[i].end());
                    std::sort(v.begin(), v.end(), NewCacheCompare2(EVENT_IMP));

                    if (v.size() > SIZE) {
                        /*remove elem start position 20000 */
                        v.resize(SIZE);
                    }

                    vs.emplace_back(std::move(v));
                }
            }

        case TQUERY_FIELDS_SIZE:
            {
                for (size_t i = 0; i < TQUERY_FIELDS_SIZE; ++i) {
                    std::vector<NEWCACHE_PAIR> v((*in)[i].begin(), (*in)[i].end());
                    std::sort(v.begin(), v.end(), NewCacheCompare2(type));

                    if (v.size() > SIZE) {
                        /*remove elem start position 20000 */
                        v.resize(SIZE);
                    }

                    vs.emplace_back(std::move(v));
                }
            }

    }

    int offSet = ((outsSize == TQUERY_OVERFLOW)?TQUERY_FIELDS_SIZE:0);
    switch(type) {
        case EVENT_CLICK:
            {
                for (int i = 0; i < TQUERY_FIELDS_SIZE; ++i) {
                    rapidjson::StringBuffer s;
                    rapidjson::Writer<rapidjson::StringBuffer> writer(s);
                    writer.SetMaxDecimalPlaces(3);
                    writer.StartObject();
                    writer.Key("ts");
                    writer.Int(time(NULL));
                    writer.Key("data");
                    writer.StartArray();

                    auto end = vs[i].end();
                    for (auto it = vs[i].begin(); it != end; ++it) {
                        writer.StartObject();
                        writer.Key("key");
                        writer.String(it->first.c_str());
                        writer.Key("click");
                        writer.Double(it->second[0]);
                        writer.EndObject();
                    }

                    writer.EndArray();
                    writer.EndObject();
                    assert(writer.IsComplete());
                    outs.emplace_back(s.GetString());
                }

                if (offSet == 0) {
                    break;
                }
            }

        case EVENT_IMP:
            {
                //int offSet = ((outsSize == TQUERY_OVERFLOW)?3:0);
                for (int i = 0; i < TQUERY_FIELDS_SIZE; ++i) {
                    rapidjson::StringBuffer s;
                    rapidjson::Writer<rapidjson::StringBuffer> writer(s);
                    writer.SetMaxDecimalPlaces(3);
                    writer.StartObject();
                    writer.Key("ts");
                    writer.Int(time(NULL));
                    writer.Key("data");
                    writer.StartArray();

                    auto end = vs[i + offSet].end();
                    for (auto it = vs[i + offSet].begin(); it != end; ++it) {
                        writer.StartObject();
                        writer.Key("key");
                        writer.String(it->first.c_str());
                        writer.Key("imp");
                        writer.Double(it->second[1]);
                        writer.EndObject();
                    }

                    writer.EndArray();
                    writer.EndObject();
                    assert(writer.IsComplete());
                    outs.emplace_back(s.GetString());
                }
            }
    }

    return 0;
}

int UfsInMem::SetCacheClickAndImp(int type, std::shared_ptr<NEWCACHE>& in, const std::string& uid, double decayRate, int decayPeriod) {
    m_cacheClickAndImp.Set(uid, in);
    ReportAvg("zhizi.ufs.cacheClickAndImp.size", m_cacheClickAndImp.Size());

    std::vector<std::string> jsons;
    SerializeCacheClickAndImpJson(type, uid, jsons, in);
    g_concurrentQueue.enqueue(make_pair(uid, std::move(jsons)));
    
    return 0;
}

int UfsInMem::GetCacheClickAndImp(std::shared_ptr<NEWCACHE>& out, const std::string& uid, double decayRate, int decayPeriod) {
    if (m_cacheClickAndImp.Get(out, uid) != 0) {
        /* get from ssdb */
        std::vector<std::string> jsons;

        int ret = this->ssdb_->GetSsdbJsons(jsons, uid, g_constFields);
        if (ret != 0) {
            return -1;
        }

        if (jsons.empty()) {
            return 1;
        }

        if (jsons.size() != TQUERY_FIELDS_SIZE) {
            TLOGINFO(__FILE__<<"-"<<__LINE__<<"uid|"<<uid<<" cache click_and_imp jsons size error\n");

            return 1;
        }

        ret = ParseJsonsClickAndImp(out, jsons, decayRate, decayPeriod);
        if (ret != 0) {
            return -1;
        }

        this->m_cacheClickAndImp.Set(uid, out);
    } else {

        ReportIncr("zhizi.ufs.cg.hit");
    }

    return 0;
}


int UfsInMem::GetCacheClickAndImp(int type, std::shared_ptr<NEWCACHE>& out, const std::string& uid, double decayRate, int decayPeriod) {
    do {
        if (type == EVENT_CLICK) {
            if (m_cacheClickAndImp.Get(out, uid) != 0) {
                /* get from ssdb */
                std::vector<std::string> jsons;

                int ret = this->ssdb_->GetSsdbJsons(jsons, uid, g_constFields);
                if (ret != 0) {
                    return -1;
                }

                if (jsons.empty()) {
                    return 1;
                }

                if (jsons.size() != TQUERY_FIELDS_SIZE) {
                    FDLOG(EventTypeTableStr[type][0].c_str())<<"uid|"<<uid<<" cache click_and_imp jsons size error\n";
                    //TLOGINFO(__FILE__<<"-"<<__LINE__<<"uid|"<<uid<<" cache click_and_imp jsons size error\n");

                    return 1;
                }

                ret = ParseJsonsClickAndImp(type, out, jsons, decayRate, decayPeriod);
                if (ret != 0) {
                    return -1;
                }

                this->m_cacheClickAndImp.Set(uid, out);
            } else {

                ReportIncr("zhizi.ufs.cg.hit");
            }

            break;
        }

        if (type == EVENT_IMP) {
            if (m_cacheClickAndImp.Get(out, uid) != 0) {
                /* get from ssdb */
                std::vector<std::string> jsons;

                int ret = this->ssdb_->GetSsdbJsons(jsons, uid, g_constFieldsImp);
                if (ret != 0) {
                    return -1;
                }

                if (jsons.empty()) {
                    return 1;
                }

                if (jsons.size() != TQUERY_FIELDS_SIZE) {
                    TLOGINFO(__FILE__<<"-"<<__LINE__<<"uid|"<<uid<<" cache click_and_imp jsons size error\n");

                    return 1;
                }

                ret = ParseJsonsClickAndImp(type, out, jsons, decayRate, decayPeriod);
                if (ret != 0) {
                    return -1;
                }

                this->m_cacheClickAndImp.Set(uid, out);
            } else {

                ReportIncr("zhizi.ufs.cg.hit");
            }

            break;
        }
    } while(false);

    return 0;
}

int UfsInMem::MergeNewClickOrImp(int type, const std::string& uid, const std::string& content_id, 
        std::tuple<std::string/* publisher */, 
        std::multimap<std::string, std::string> /* keywords */, 
        std::unordered_set<std::string>/* topics */>& elem, 
        double decayRate, 
        int decayPeriod,
        int eventTime) {
    auto cacheClickAndImp = std::make_shared<NEWCACHE>();
    GetCacheClickAndImp(type, cacheClickAndImp, uid, decayRate, decayPeriod);

    /* std::pow(rate, (current - createTime) / period) */
    double decayFactor = GetDecayFactor(time(NULL), eventTime, decayRate, decayPeriod);

    /* pop the publisher */
    std::string& publisher = std::get<0>(elem);
    std::multimap<std::string, std::string>& keywords = std::get<1>(elem);
    std::unordered_set<std::string>& topics = std::get<2>(elem);

    if (keywords.size() == 0) {
        FDLOG(EventTypeTableStr[type][0].c_str())<<"uid="<<uid<<", content_id="<<content_id<<"|keywords empty"<<std::endl;
    }

    if (topics.size() == 0) {
        FDLOG(EventTypeTableStr[type][0].c_str())<<"uid="<<uid<<", content_id="<<content_id<<"|topics empty"<<std::endl;
    }

    /* merge publisher */
    auto pub = (*cacheClickAndImp)[TQUERY_CONTENT_PUBLISHER].find(publisher);
    if (pub == (*cacheClickAndImp)[TQUERY_CONTENT_PUBLISHER].end()) {
        /* not found */
        if (type == EVENT_CLICK) {
            std::array<double, 2> clickAndImp = {decayFactor, 0.0};
            (*cacheClickAndImp)[TQUERY_CONTENT_PUBLISHER].emplace(std::move(publisher), std::move(clickAndImp)); 
        } else {
            std::array<double, 2> clickAndImp = {0.0, decayFactor};
            (*cacheClickAndImp)[TQUERY_CONTENT_PUBLISHER].emplace(std::move(publisher), std::move(clickAndImp)); 
        }
    } else {
        /* found then update */
        if (type == EVENT_CLICK) {
            pub->second[0] += decayFactor;
        } else {
            pub->second[1] += decayFactor;
        }
    }

    /* merge keyword */
    auto keywordsEnd = keywords.end();
    for (auto it = keywords.begin(); it != keywordsEnd; ++it) {
        auto keyword = (*cacheClickAndImp)[TQUERY_CONTENT_KEYWORDS].find(it->second);
        if (keyword == (*cacheClickAndImp)[TQUERY_CONTENT_KEYWORDS].end()) {
            if (type == EVENT_CLICK) {
                std::array<double, 2> clickAndImp = {decayFactor, 0.0};
                (*cacheClickAndImp)[TQUERY_CONTENT_KEYWORDS].emplace(it->second, std::move(clickAndImp)); 
            } else {
                std::array<double, 2> clickAndImp = {0.0, decayFactor};
                (*cacheClickAndImp)[TQUERY_CONTENT_KEYWORDS].emplace(it->second, std::move(clickAndImp)); 
            }
        } else {
            /* found then update */
            if (type == EVENT_CLICK) {
                keyword->second[0] += decayFactor; 
            } else {
                keyword->second[1] += decayFactor; 
            }
        }
    }

    /* merge topics */
    auto topicsEnd = topics.end();
    for (auto it = topics.begin(); it != topicsEnd; ++it) {
        auto topic = (*cacheClickAndImp)[TQUERY_EXTEND_TOPICS].find(*it);
        if (topic == (*cacheClickAndImp)[TQUERY_EXTEND_TOPICS].end()) {
            if (type == EVENT_CLICK) {
                std::array<double, 2> clickAndImp = {decayFactor, 0.0};
                (*cacheClickAndImp)[TQUERY_EXTEND_TOPICS].emplace(*it, std::move(clickAndImp)); 
            } else {
                std::array<double, 2> clickAndImp = {0.0, decayFactor};
                (*cacheClickAndImp)[TQUERY_EXTEND_TOPICS].emplace(*it, std::move(clickAndImp)); 
            }
        } else {
            /* found then update */
            if (type == EVENT_CLICK) {
                topic->second[0] += decayFactor; 
            } else {
                topic->second[1] += decayFactor; 
            }
        }
    }

#if 0
    /* calc the ts */
    auto ufsTs = (*cacheClickAndImp)[TQUERY_UFS_TS].find(TQueryTable[TQUERY_UFS_TS].C_STR());
    if (ufsTs == (*cacheClickAndImp)[TQUERY_UFS_TS].end()) {
            double ts = time(NULL);
            std::array<double, 2> clickAndImp = {ts, ts};
            (*cacheClickAndImp)[TQUERY_UFS_TS].emplace(TQueryTable[TQUERY_UFS_TS].C_STR(), std::move(clickAndImp)); 
    } else {
        int ts = time(NULL);
        if (ts - int(ufsTs->second[0]) > 86400) {
            /* 24*60*60 = 86400 */ 
            ufsTs->second[0] = ufsTs->second[1] = ts;
        }
    }
#endif

    SetCacheClickAndImp(type, cacheClickAndImp, uid, decayRate, decayPeriod);
    
    return 0;
}

int32_t UfsInMem::SetCategoryClickOrImp(const std::string& type, const std::string& version, const std::string& uid, std::shared_ptr<MCG>& input)
{
    //FUNC_GUARD();
    const std::string key = version + "|" + uid;
    this->categoryClickAndImpCache_.Set(key, input);
    ReportAvg("zhizi.ufs.categoryClickAndImpCache_.size", this->categoryClickAndImpCache_.Size());

    std::string hashname = this->GetCategoryClickAndImpHashName(version);
    std::shared_ptr<std::string> json(new std::string);
    this->SerializeCategoryClickAndImpJson(json, input);
    if (0 != this->ssdb_->AddSyncTask(hashname, uid, json))
    {
        usleep(this->nap_);
    }
    return 0;
}

int32_t UfsInMem::SerializeCategoryClickAndImpJson(std::shared_ptr<std::string>& out, const std::shared_ptr<MCG>& input)
{
    //FUNC_GUARD();
    rapidjson::StringBuffer s;
    rapidjson::Writer<rapidjson::StringBuffer> writer(s);
    writer.SetMaxDecimalPlaces(3);
    writer.StartObject();
    writer.Key("ts");
    writer.Int(time(NULL));
    writer.Key("data");
    writer.StartArray();
    for (const auto& i :*input)
    {
        writer.StartObject();
        writer.Key("category");
        writer.String(i.first.c_str());
        writer.Key("click");
        writer.Double(i.second.first);
        writer.Key("imp");
        writer.Double(i.second.second);
        writer.EndObject();
    }
    writer.EndArray();
    writer.EndObject();

    assert(writer.IsComplete());
    out = std::shared_ptr<std::string>(new std::string(s.GetString()));
    return 0;
}

int32_t UfsInMem::GetUfsWeightedCategory(std::shared_ptr<WU>& out, const std::string& version, const std::string& uid,
        double decayRate, int32_t decayPeriod)
{
    FUNC_GUARD();
    // out.clear();
    const std::string key = version + "|" + uid;
    ReportIncr("zhizi.ufs.wc.get");
    if (this->categoryCache_.Get(out, key) != 0)
    {
        std::string json;
        std::string hash = this->GetUfsHashName("category", version);
        /* hget  zhizi.ufs.category.v28 01011803211734125201000215337307 */
        int ret = this->ssdb_->GetSsdbJson(json, hash, uid);
        if (ret != 0)
        {
            return -1;
        }
        if (json.empty())
        {
            return 1;
        }

        ret = this->JsonWeight2Map(out, json, decayRate, decayPeriod);
        if (ret != 0)
        {
            return -1;
        }
        this->categoryCache_.Set(key, out);
    }
    else
    {
        ReportIncr("zhizi.ufs.wc.hit");
    }
    return 0;
}

int32_t UfsInMem::GetTagStats(std::shared_ptr<VS>& out, const std::string& uid)
{
    FUNC_GUARD();
    // out.clear();
    const std::string key = uid;
    ReportIncr("zhizi.ufs.ts.get");
    if (this->tagStatsCache_.Get(out, key) != 0)
    {
        std::string json;
        std::string hash = this->GetStatsHashName("tag");
        int ret = this->ssdb_->GetSsdbJson(json, hash, uid);
        if (ret != 0)
        {
            return -1;
        }
        if (json.empty())
        {
            return 1;
        }

        ret = this->JsonStats2Map(out, json);
        if (ret != 0)
        {
            return -1;
        }
        this->tagStatsCache_.Set(key, out);
    }
    else
    {
        ReportIncr("zhizi.ufs.ts.hit");
    }
    return 0;
}

int32_t UfsInMem::GetCategoryStats(std::shared_ptr<VS>& out, const std::string& uid)
{
    FUNC_GUARD();
    // out.clear();
    const std::string key = uid;
    ReportIncr("zhizi.ufs.cs.get");
    if (this->categoryStatsCache_.Get(out, key) != 0)
    {
        std::string json;
        std::string hash = this->GetStatsHashName("category");
        int ret = this->ssdb_->GetSsdbJson(json, hash, uid);
        if (ret != 0)
        {
            return -1;
        }
        if (json.empty())
        {
            return 1;
        }

        ret = this->JsonStats2Map(out, json);
        if (ret != 0)
        {
            return -1;
        }
        this->categoryStatsCache_.Set(key, out);
    }
    else
    {
        ReportIncr("zhizi.ufs.cs.hit");
    }
    return 0;
}

int32_t UfsInMem::SetUfsWeightedTag(const std::string& version, const std::string& uid, const std::shared_ptr<WU>& out,
        const std::string& app, const std::string& configId, uint32_t tagNumLimit,
        const std::unordered_set<std::string>& blackWords)
{
    FUNC_GUARD();
    const std::string key = version + "|" + uid;
    this->tagCache_.Set(key, out);
    ReportAvg("zhizi.ufs.tagCache_.size", this->tagCache_.Size());

    std::vector<std::pair<std::string, double> > sorted = SortMapByWeight(out, blackWords, tagNumLimit);
    std::string hash = this->GetUfsHashName("tag", version);
    std::shared_ptr<std::string> json(new std::string);
    this->SerializeUfsJson(json, sorted, app, configId);
    if (0 != this->ssdb_->AddSyncTask(hash, uid, json))// queue full, slow down
    {
        usleep(this->nap_);
    }
    return 0;
}

int32_t UfsInMem::SetUfsWeightedCategory(const std::string& version, const std::string& uid, const std::shared_ptr<WU>& out,
        const std::string& app, const std::string& configId)
{
    FUNC_GUARD();
    const std::string key = version + "|" + uid;
    this->categoryCache_.Set(key, out);
    ReportAvg("zhizi.ufs.categoryCache_.size", this->categoryCache_.Size());

    std::vector<std::pair<std::string, double> > sorted = SortMapByWeight(out, (std::unordered_set<std::string>()), 2000);
    std::string hash = this->GetUfsHashName("category", version);
    std::shared_ptr<std::string> json(new std::string);
    this->SerializeUfsJson(json, sorted, app, configId);
    if (0 != this->ssdb_->AddSyncTask(hash, uid, json))
    {
        usleep(this->nap_);
    }
    return 0;
}

int32_t UfsInMem::SetTagStats(const std::string& uid, const std::shared_ptr<VS>& out)
{
    FUNC_GUARD();
    const std::string key = uid;
    this->tagStatsCache_.Set(key, out);
    ReportAvg("zhizi.ufs.tagStatsCache_.size", this->tagStatsCache_.Size());

    std::string hash = this->GetStatsHashName("tag");
    std::shared_ptr<std::string> json(new std::string);
    this->SerializeStatsJson(json, out);
    if (0 != this->ssdb_->AddSyncTask(hash, uid, json))
    {
        usleep(this->nap_);
    }
    return 0;
}

int32_t UfsInMem::SetCategoryStats(const std::string& uid, const std::shared_ptr<VS>& out)
{
    FUNC_GUARD();
    const std::string key = uid;
    this->categoryStatsCache_.Set(key, out);
    ReportAvg("zhizi.ufs.categoryStatsCache_.size", this->categoryStatsCache_.Size());

    std::string hash = this->GetStatsHashName("category");
    std::shared_ptr<std::string> json(new std::string);
    this->SerializeStatsJson(json, out);
    if (0 != this->ssdb_->AddSyncTask(hash, uid, json))
    {
        usleep(this->nap_);
    }
    return 0;
}

int32_t UfsInMem::SerializeStatsJson(std::shared_ptr<std::string>& out, const std::shared_ptr<VS>& stats)
{
    FUNC_GUARD();
    rapidjson::StringBuffer s;
    rapidjson::Writer<rapidjson::StringBuffer> writer(s);
    writer.SetMaxDecimalPlaces(3);
    writer.StartObject();
    writer.Key("ts");
    writer.Int(time(NULL));
    writer.Key("data");
    writer.StartObject();
    for (const auto& i :*stats)
    {
        writer.Key(i.first.c_str());
        writer.StartObject();
        writer.Key("num");
        writer.Int(i.second.first);
        writer.Key("sum");
        writer.Double(i.second.second);
        writer.EndObject();
    }
    writer.EndObject();
    writer.EndObject();

    assert(writer.IsComplete());
    out = std::shared_ptr<std::string>(new std::string(s.GetString()));
    return 0;
}

int32_t UfsInMem::SerializeUfsJson(std::shared_ptr<std::string>& out, std::vector<std::pair<std::string, double> >& wtags,
        const std::string& app, const std::string& configId)
{
    FUNC_GUARD();
    rapidjson::StringBuffer s;
    rapidjson::Writer<rapidjson::StringBuffer> writer(s);
    writer.SetMaxDecimalPlaces(3);
    writer.StartObject();
    writer.Key("config");
    writer.String(configId.c_str());
    writer.Key("ts");
    writer.Int(time(NULL));
    writer.Key("app");
    writer.String(app.c_str());
    writer.Key("weighted");
    writer.StartArray();
    for (const auto& j: wtags)
    {
        writer.StartObject();
        writer.Key("tag");
        writer.String(j.first.c_str());
        writer.Key("weight");
        writer.Double(j.second);
        writer.EndObject();
    }
    writer.EndArray();
    writer.EndObject();

    assert(writer.IsComplete());
    out = std::shared_ptr<std::string>(new std::string(s.GetString()));
    return 0;
}

int32_t UfsInMem::JsonStats2Map(std::shared_ptr<VS>& statsMap, const std::string& json)
{
    FUNC_GUARD();
    assert(!json.empty());
    rapidjson::MemoryPoolAllocator<> valueAllocator(this->valueBuffer_, this->jsonBufSize_);
    rapidjson::MemoryPoolAllocator<> parseAllocator(this->parseBuffer_, this->jsonBufSize_);
    DocumentType d(&valueAllocator, this->jsonBufSize_, &parseAllocator);

    d.Parse(json.c_str());
    if (d.HasParseError())
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" json parse fail: "<< d.GetErrorOffset()
                << ": " << (rapidjson::GetParseError_En(d.GetParseError())) << endl);
        return -1;
    }
    if (d.FindMember("data") == d.MemberEnd())
    {
        return 1;
    }

    auto& data = d.FindMember("data")->value;
    for (auto it = data.MemberBegin(); it != data.MemberEnd(); ++it)
    {
        const std::string version = it->name.GetString();
        const auto& stats = it->value;
        const uint32_t num = stats.FindMember("num")->value.GetInt();
        const double sum = stats.FindMember("sum")->value.GetDouble();
        statsMap->insert(std::make_pair(version, std::make_pair(num, sum)));
    }
    return 0;
}

int32_t UfsInMem::JsonCategoryClickAndImp2Map(std::shared_ptr<MCG>& out, const std::string& json,
        double decayRate, int32_t decayPeriod)
{
    FUNC_GUARD();
    assert(!json.empty());
    rapidjson::MemoryPoolAllocator<> valueAllocator(this->valueBuffer_, this->jsonBufSize_);
    rapidjson::MemoryPoolAllocator<> parseAllocator(this->parseBuffer_, this->jsonBufSize_);
    DocumentType d(&valueAllocator, this->jsonBufSize_, &parseAllocator);

    d.Parse(json.c_str());
    if (d.HasParseError())
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" category click_and_imp json parse fail: "<< d.GetErrorOffset()
                << ": " << (rapidjson::GetParseError_En(d.GetParseError())) << endl);
        return -1;
    }
    if (d.FindMember("data") == d.MemberEnd())
    {
        return 1;
    }

    double decayFactor = UfsInMem::GetDecayFactor(time(NULL), d.FindMember("ts")->value.GetInt(), decayRate, decayPeriod);
    rapidjson::Value::ConstMemberIterator itwt = d.FindMember("data");

    for (rapidjson::Value::ConstValueIterator itr = itwt->value.Begin(); itr != itwt->value.End(); ++itr)
    {
        std::string categoryKey = itr->GetObject().FindMember("category")->value.GetString();
        double click = itr->GetObject().FindMember("click")->value.GetDouble();
        double imp = itr->GetObject().FindMember("imp")->value.GetDouble();

        out->insert(std::make_pair(categoryKey, std::make_pair(click*decayFactor, imp*decayFactor)));
    }

    return 0;
}

int32_t UfsInMem::JsonWeightArray2Map(std::shared_ptr<WU>& out/*, const std::string& type*/, const rapidjson::Value& obj, double decayFactor)
{
    // std::map<std::string, double> out;
    for (rapidjson::Value::ConstValueIterator itr = obj.Begin();
            itr != obj.End(); ++itr)
    {
        std::string tagKey = itr->GetObject().FindMember("tag")->value.GetString();
        // if ((type == "category" && !UfsInMem::IsDecimal(tagKey)) || (type == "tag" && UfsInMem::IsDecimal(tagKey)))
        // {
        // //     TLOGINFO(__FILE__<<"-"<<__LINE__<<" delete dirty: " << type << ", " << tagKey << endl);
        //     continue;
        // }
        double tagWeight = itr->GetObject().FindMember("weight")->value.GetDouble() * decayFactor;
        out->insert(std::make_pair(tagKey, tagWeight));
    }
    return 0;
}

int32_t UfsInMem::JsonWeight2Map(std::shared_ptr<WU>& out, const std::string& json,
        double decayRate, int32_t decayPeriod)
{
    FUNC_GUARD();
    assert(!json.empty());
    rapidjson::MemoryPoolAllocator<> valueAllocator(this->valueBuffer_, this->jsonBufSize_);
    rapidjson::MemoryPoolAllocator<> parseAllocator(this->parseBuffer_, this->jsonBufSize_);
    DocumentType d(&valueAllocator, this->jsonBufSize_, &parseAllocator);

    TIME_LABEL(1);
    d.Parse(json.c_str());
    if (d.HasParseError())
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" json parse fail: "<< d.GetErrorOffset()
                << ": " << (rapidjson::GetParseError_En(d.GetParseError())) << endl);
        return -1;
    }
    if (json.size() > 10000)
    {
        ReportAvg("zhizi.ufs.json.time.gt10k", TIME_DIFF(1));
    }
    else if (json.size() > 1000)
    {
        ReportAvg("zhizi.ufs.json.time.gt1k", TIME_DIFF(1));
    }
    else
    {
        ReportAvg("zhizi.ufs.json.time.lt1k", TIME_DIFF(1));
    }
    if (d.FindMember("weighted") == d.MemberEnd())
    {
        return 1;
    }

    double decayFactor = UfsInMem::GetDecayFactor(time(NULL), d.FindMember("ts")->value.GetInt(), decayRate, decayPeriod);
    rapidjson::Value::ConstMemberIterator itwt = d.FindMember("weighted");
    UfsInMem::JsonWeightArray2Map(out, itwt->value, decayFactor);

    return 0;
}

double UfsInMem::GetDecayFactor(int32_t current, int32_t createTime, double rate, int32_t period)
{
    assert(period > 0);
    if (createTime <= 0)
    {
        /* 
         * createTime = time(NULL) - 1*24*60*60;
        * 1*24*60*60 = 86400   
        * */
        createTime = time(NULL) - 86400;
    }

    /* pow(0.995, (current-create)/86400)*/
    return std::pow(rate, (current - createTime) / period);
}

bool UfsInMem::IsDecimal(const std::string& word)
{
    if (word.empty())
    {
        return false;
    }
    if (word == "-1")
    {
        return true;
    }
    if (word[0] > '9' || word[0] < '1')
    {
        return false;
    }
    for (const auto& it: word.substr(1))
    {
        if (it > '9' || it < '0') 
        {
            return false;
        }
    }
    return true;
}

std::string UfsInMem::GetUfsHashName(const std::string& type, const std::string& version)
{
    std::string hashName;
    if (type == "category")
    {
        hashName = this->hashCategory_ + version;
    }
    else if (type == "tag")
    {
        hashName = this->hashTag_ + version;
    }
    else
    {
        assert(0);
    }
    return hashName;
}

std::string UfsInMem::GetCategoryClickAndImpHashName(const std::string& version)
{
    std::string hashName = this->hashCategoryClickAndImp_ + version;
    return hashName;
}

std::string UfsInMem::GetStatsHashName(const std::string& type)
{
    if (type == "tag")
    {
        return this->hashStatsTag_;
    }
    else if (type == "category")
    {
        return this->hashStatsCategory_;
    }
    assert(0);
    return "";
}

std::vector<std::pair<std::string, double> >
UfsInMem::SortMapByWeight(const std::shared_ptr<WU>& merged, const std::unordered_set<std::string>& blackWords, uint32_t tagNumLimit)
{
    FUNC_GUARD();
    std::vector<std::pair<std::string, double> > t;
    for (const auto& j: *merged)
    {
        t.push_back(j);
    }
    std::sort(t.begin(), t.end(), UfsInMem::WeightCmp);
    std::vector<std::pair<std::string, double> > out;
    for (size_t k = 0; k < t.size(); ++k)
    {
        if (out.size() >= tagNumLimit)
        {
            break;
        }
        // if (std::find(black_words.begin(), black_words.end(), t[k].first) != black_words.end())
        if (t[k].second < 0.001)
        {
            break;
        }
        if (blackWords.count(t[k].first) > 0)
        {
            // TLOGINFO(__FILE__<<"-"<<__LINE__<<" will filter black word: " << (t[k].first) << endl);
            continue;
        }
        out.push_back(t[k]);
    }
    return out;
}
