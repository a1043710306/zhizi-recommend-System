#include <cassert>
#include <cstdio>

#include "db.h"
#include "util/inv_util.h"
#include "util/inv_httpclient.h"
#include "inv_log.h"
#include "util/inv_md5.h"
#include "report.h"

#include "rapidjson/writer.h"
#include "rapidjson/stringbuffer.h"
#include "rapidjson/error/en.h"

#include "JsonSaxHandler.h"

#include "ufs_logic.h"
#include "util.h"
#include"zhizi_define.h"

using namespace inv;
using namespace inv::monitor;

#define X(a, b) [a]=b,
const char* ParseErrorTableStr[] = {
    PARSE_ERROR_TABLE
};
#undef X


SfuLogic::SfuLogic(std::shared_ptr<inv::INV_Mysql> conn, int decayPeriod,
        double defaultDecayRate, const std::map<std::string, double>& decayRate,
        const std::vector<inv::INV_Redis*>& vecRedis,
        const std::vector<inv::INV_Redis*>& vecWRedis, const std::string& ufsCategory,
        const std::string& ufsTag, const std::string& ufsStatsCategory,
        const std::string& ufsStatsTag, int ufsNum, int timeZone,
        int cacheNum, int threadNum, int32_t writeCold, const std::string categoryClickAndImpHashName)
:conn_(conn), decayPeriod_(decayPeriod), defaultDecayRate_(defaultDecayRate), decayRate_(decayRate),
    vecRedis_(vecRedis), vecWRedis_(vecWRedis), ufsCategory_(ufsCategory), ufsTag_(ufsTag), ufsStatsCategory_(ufsStatsCategory),
    ufsStatsTag_(ufsStatsTag), ufsNum_(ufsNum), timeZone_(timeZone), valueBuffer_(NULL), parseBuffer_(NULL),
    uim_(NULL)
{
    this->signal_ = new TSignal(this->conn_);
    this->tableIndex_ = new TIndex(this->conn_);
    m_tQuery = new TQuery(this->conn_);
    this->valueBuffer_ = new char[this->jsonBufSize_];
    this->parseBuffer_ = new char[this->jsonBufSize_];
    this->ufsCache_["category"] = std::map<std::string, std::map<std::string, std::map<std::string, double>>>();
    this->ufsCache_["tag"] = std::map<std::string, std::map<std::string, std::map<std::string, double>>>();
    assert(cacheNum > 0);
    assert(threadNum > 0);
    uim_ = new UfsInMem(cacheNum / threadNum, this->ufsCategory_, this->ufsTag_,
            ufsStatsCategory_, this->ufsStatsTag_, 
            vecRedis_,vecWRedis_, writeCold, 
            categoryClickAndImpHashName);
}

SfuLogic::SfuLogic(int decayPeriod, double defaultDecayRate, const std::map<std::string, double>& decayRate,
        const std::vector<inv::INV_Redis*>& vecRedis,
        const std::string& ufsCategory, const std::string& ufsTag,
        const std::string& ufsStatsCategory, const std::string& ufsStatsTag)
:decayPeriod_(decayPeriod), defaultDecayRate_(defaultDecayRate), decayRate_(decayRate),
    vecRedis_(vecRedis), ufsCategory_(ufsCategory), ufsTag_(ufsTag), 
    ufsStatsCategory_(ufsStatsCategory), ufsStatsTag_(ufsStatsTag), valueBuffer_(NULL), parseBuffer_(NULL),
    uim_(NULL)
{
    this->ufsNum_ = 99999999;

    this->valueBuffer_ = new char[this->jsonBufSize_];
    this->parseBuffer_ = new char[this->jsonBufSize_];
    // this->valueAllocator_ = new rapidjson::MemoryPoolAllocator<>(this->valueBuffer_, this->jsonBufSize_);
    // this->parseAllocator_ = new rapidjson::MemoryPoolAllocator<>(this->parseBuffer_, this->jsonBufSize_);
}

SfuLogic::~SfuLogic()
{
    if (signal_ != nullptr) {
        delete this->signal_;
        signal_ = nullptr;
    }

    if (tableIndex_ != nullptr) {
        delete this->tableIndex_;
        tableIndex_ = nullptr;
    }

    if (m_tQuery != nullptr) {
        delete m_tQuery;
        m_tQuery = nullptr;
    }

    if (valueBuffer_ != NULL) {
        delete[] valueBuffer_;
        valueBuffer_ = nullptr;
    }

    if (parseBuffer_ != NULL) {
        delete[] parseBuffer_;
        parseBuffer_ = nullptr;
    }

    if (uim_ != NULL) {
        delete uim_;
        uim_ = nullptr;
    }
}

int32_t SfuLogic::GetWeightedTags(std::map<std::string, std::map<std::string, double> >& ret,
        const std::string& json)
{
    // FUNC_GUARD();
    ret.clear();
    rapidjson::Document d;
    d.Parse(json.c_str());

    if (d.HasParseError())
    {
        //std::cout << "Error at offset " << d.GetErrorOffset() << ": " << GetParseError_En(d.GetParseError()) << std::endl;
        return -1;
    }

    // assert(d.IsObject());
    if (!d.IsObject())
    {
        return -1;
    }
    for (rapidjson::Value::ConstMemberIterator itr = d.MemberBegin();
                itr != d.MemberEnd(); ++itr)
    {
        std::string ver = itr->name.GetString();
        std::map<std::string, double> wtags;
        if (!itr->value.IsObject())
        {
            continue;
        }
        for (rapidjson::Value::ConstMemberIterator jtr = itr->value.MemberBegin();
                jtr != itr->value.MemberEnd(); ++jtr)
        {
            std::string tag(jtr->name.GetString(), jtr->name.GetString() + jtr->name.GetStringLength());
            assert(jtr->value.FindMember("weight") != jtr->value.MemberEnd());
            // double weight = jtr->value.FindMember("weight")->value.GetDouble() * decayFactor;
            double weight = jtr->value.FindMember("weight")->value.GetDouble();
            wtags.insert(std::make_pair(tag, weight));
        }
        // ret.insert(std::make_pair(versionPrefix+ver, wtags));
        ret.insert(std::make_pair(ver, wtags));
    }
    return 0;
}

//from internet
time_t SfuLogic::time_to_epoch ( const struct tm *ltm, int utcdiff )
{
    const int mon_days [] =
    {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    long tyears, tdays, leaps, utc_hrs;
    int i;

    tyears = ltm->tm_year - 70;
    leaps = (tyears + 2) / 4; // no of next two lines until year 2100.
    // i = (ltm->tm_year – 100) / 100;
    // leaps -= ( (i/4)*3 + i%4 );
    tdays = 0;
    for (i=0; i < ltm->tm_mon; i++) tdays += mon_days[i];

    tdays += ltm->tm_mday-1; // days of month passed.
    tdays = tdays + (tyears * 365) + leaps;

    utc_hrs = ltm->tm_hour + utcdiff; // for your time zone.
    return (tdays * 86400) + (utc_hrs * 3600) + (ltm->tm_min * 60) + ltm->tm_sec;
}

int32_t SfuLogic::GetTimestampFromDateTime(const std::string& date_time, int32_t &timestamp)
{
    const char *iso8601 = "%F %T";//"%Y-%m-%d %H:%M:%S";
    //char *strptime(const char *s, const char *format, struct tm *tm);

    struct tm dt;
    char *p = strptime(date_time.c_str(), iso8601, &dt);
    if (p == NULL)
    {
        return -1;
    }
    if (*p != '\0')
    {
        return -1;
    }

    dt.tm_isdst = 0;
    timestamp = this->time_to_epoch(&dt, 0 - this->timeZone_);
    // timestamp = mktime(&dt);
    return 0;
}

int32_t SfuLogic::SerializeToSsdbJson(std::string& out, std::vector<std::pair<std::string, double> >& wtags,
        int32_t ts, const std::string& app, const std::string& configId)
{
    rapidjson::StringBuffer s;
    rapidjson::Writer<rapidjson::StringBuffer> writer(s);
    writer.SetMaxDecimalPlaces(3);
    writer.StartObject();
    writer.Key("config");
    writer.String(configId.c_str());
    writer.Key("ts");
    writer.Int(ts);
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
    out = s.GetString();
    return 0;
}

// std::map<std::string, double>
// SfuLogic::MergeWeightMaps(const std::map<std::string, double>& m1, const std::map<std::string, double>& m2)
int32_t SfuLogic::MergeWeightMaps(std::shared_ptr<std::map<std::string, double>>& m1, const std::map<std::string, double>& m2)
{
    // FUNC_GUARD();
    // std::map<std::string, double>& out = m1;
    for (auto it = m2.begin(); it != m2.end(); ++it)
    {
        if (m1->count(it->first) <= 0)
        {
            m1->insert(*it);
        }
        else
        {
            (*m1)[it->first] += it->second;
        }
    }
    return 0;
}

std::string SfuLogic::GetHashName(const std::string& type, const std::string& version)
{
    std::string hashName;
    if (type == "category")
    {
        hashName = this->ufsCategory_ + version;
    }
    else if (type == "tag")
    {
        hashName = this->ufsTag_ + version;
    }
    else
    {
        assert(0);
    }
    return hashName;
}

// int32_t SfuLogic::GetSsdbJson(rapidjson::Document* pd, const std::string& type, const std::string& version,
int32_t SfuLogic::GetSsdbJson(DocumentType* pd, const std::string& type, const std::string& version,
        const std::string& uid)
{
    FUNC_GUARD();
    std::string rsp;
    // int ret = RedisHashGet(rsp, this->redis_, this->ufsHash_, uid);
    int ret = RedisHashGet(rsp, this->ChooseSsdb(uid), this->GetHashName(type, version), uid);
    if (ret == 0)
    {
        // ReportIncr("zhizi.ufs." + type + ".ssdb.get.count");
        // ReportIncr("zhizi.ufs." + type + ".ssdb.get.traffic", rsp.length());
        // ReportAvg("zhizi.ufs." + type + ".ssdb.get.time", TIME_DIFF(1));
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" redis get ok: "<<ret<<"-"<<rsp.length()<<endl);
    }
    if (ret == 1)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" redis get, no data: "<< uid <<endl);
        return 1;
    }

    if (rsp.empty())
    {
        return 1;
    }
    TIME_LABEL(1);
    pd->Parse(rsp.c_str());
    if (pd->HasParseError())
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" json parse fail: "<< pd->GetErrorOffset() << ": " << (rapidjson::GetParseError_En(pd->GetParseError())) << endl);
        return -1;
    }
    if (rsp.size() > 10000)
    {
        ReportAvg("zhizi.ufs.json.time.gt10k", TIME_DIFF(1));
    }
    else if (rsp.size() > 1000)
    {
        ReportAvg("zhizi.ufs.json.time.gt1k", TIME_DIFF(1));
    }
    else
    {
        ReportAvg("zhizi.ufs.json.time.lt1k", TIME_DIFF(1));
    }
    return 0;
}

bool SfuLogic::IsDecimal(const std::string& word)
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

std::map<std::string, double> SfuLogic::JsonWeightArray2Map(const std::string& type, const rapidjson::Value& obj, double decayFactor)
{
    std::map<std::string, double> out;
    for (rapidjson::Value::ConstValueIterator itr = obj.Begin();
            itr != obj.End(); ++itr)
    {
        std::string tagKey = itr->GetObject().FindMember("tag")->value.GetString();
        if ((type == "category" && !SfuLogic::IsDecimal(tagKey)) || (type == "tag" && SfuLogic::IsDecimal(tagKey)))
        {
            TLOGINFO(__FILE__<<"-"<<__LINE__<<" delete dirty: " << type << ", " << tagKey << endl);
            continue;
        }
        double tagWeight = itr->GetObject().FindMember("weight")->value.GetDouble() * decayFactor;
        out.insert(std::make_pair(tagKey, tagWeight));
    }
    return out;
}

// int32_t SfuLogic::JsonWeight2Map(std::map<std::string, double>& out, const rapidjson::Document* pd,
int32_t SfuLogic::JsonWeight2Map(std::map<std::string, double>& out, const std::string& type, const DocumentType* pd,
        int curTime, const std::string& configId)
{
    // FUNC_GUARD();
    if (pd->FindMember("weighted") == pd->MemberEnd())
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" no weighted field" << endl);
        return -1;
    }

    double decayFactor = this->GetDecayFactor(curTime, pd->FindMember("ts")->value.GetInt(), configId);
    rapidjson::Value::ConstMemberIterator itwt = pd->FindMember("weighted");
    out = SfuLogic::JsonWeightArray2Map(type, itwt->value, decayFactor);

    return 0;
}

double SfuLogic::GetDecayFactor(int32_t current, int32_t create_time, const std::string& configId)
{
    if (create_time <= 0)
    {
        create_time = 1460004953;//20160407
    }

    int period = this->decayPeriod_;//GET_CONF()->get_conf_int("decay_period", 60*60*24);
    double rate = this->defaultDecayRate_;//GET_CONF()->get_conf_float("decay_rate", 0.995);
    if (this->decayRate_.count(configId) > 0)
    {
        rate = this->decayRate_[configId];
    }
    return std::pow(rate, (current - create_time) / period);
}

std::vector<std::pair<std::string, double> >
SfuLogic::SortMapByWeight(const std::map<std::string, double>& merged,
        const std::unordered_set<std::string>& blackWords)
{
    std::vector<std::pair<std::string, double> > t;
    for (const auto& j: merged)
    {
        t.push_back(j);
    }
    std::sort(t.begin(), t.end(), SfuLogic::WeightCmp);
    std::vector<std::pair<std::string, double> > out;
    // out.insert(std::make_pair(i->first, (std::vector<std::pair<std::string, double> >())));
    for (size_t k = 0; k < t.size(); ++k)
    {
        if (out.size() >= this->ufsNum_)
        {
            break;
        }
        // if (std::find(black_words.begin(), black_words.end(), t[k].first) != black_words.end())
        if (blackWords.count(t[k].first) > 0)
        {
            TLOGINFO(__FILE__<<"-"<<__LINE__<<" will filter black word: " << (t[k].first) << endl);
            continue;
        }
        out.push_back(t[k]);
    }
    return out;
}

int64_t SfuLogic::DiffTime(struct timeval* a, struct timeval* b)
{
    return 1000*1000*(b->tv_sec - a->tv_sec) + (b->tv_usec - a->tv_usec);
}

int32_t SfuLogic::UpdateUfsCategoryClickOrImp(const std::string& dbJson, const std::string& uid, const int32_t eventTime, const std::string type)
{
    //FUNC_GUARD();
    std::map<std::string, std::map<std::string, double> > cwt;
    GetWeightedTags(cwt, dbJson);

    for (const auto& vw: cwt)
    {
        if (vw.first != "v28")
        {
            continue;
        }
        std::string version = vw.first;
        std::map<std::string, double> categoryMerged(vw.second);
        uim_->MergeCategoryClickOrImp(type, version, uid, this->defaultDecayRate_, this->decayPeriod_, eventTime, categoryMerged);
    }

    return 0;
}

/* format: {"v1":[{"str":"''10''"},{"str":"''pulsa''"}]} */
int SfuLogic::ParseKeywordsFromTContentYYYYMM(const char* keywords, std::multimap<std::string, std::string>& mulMap) {
    rapidjson::Document d;
    d.Parse(keywords);

    if (d.HasParseError() || !d.IsObject()) {
        return -1;
    }

    rapidjson::Value::ConstMemberIterator end = d.MemberEnd();
    for (rapidjson::Value::ConstMemberIterator it = d.MemberBegin(); it != end; ++it) {
        std::string ver = it->name.GetString();
        std::map<std::string, std::string> keyword;
        if (!it->value.IsArray()) {
            continue;
        }

        rapidjson::SizeType size = it->value.Size();
        for (rapidjson::SizeType i = 0; i < size; ++i) {
            if (!it->value[i].IsObject()) {
                continue;
            }

            rapidjson::Value::ConstMemberIterator inEnd = it->value[i].MemberEnd();
            for (rapidjson::Value::ConstMemberIterator inIt = it->value[i].MemberBegin(); inIt != inEnd; ++inIt) {
                mulMap.emplace(ver, inIt->value.GetString());
            }
        }
    }

    return 0;
}

/* format: ["Sports","Football"] */
int SfuLogic::ParseTopicsFromTContentExtened(const char* topics, std::unordered_set<std::string>& out) {
    rapidjson::Document d;
    d.Parse(topics);

    if (d.HasParseError() || !d.IsArray()) {
        return -1;
    }

    rapidjson::SizeType size = d.Size();
    for (rapidjson::SizeType i = 0; i < size; ++i) {
        if (!d[i].IsString()) {
            continue;
        }
        out.emplace(d[i].GetString());
    }

    return 0;
}

int SfuLogic::UpdateUfsTQueryClickOrImp(const std::string& uid, const std::string& tableSuffix, const std::string& content_id, double decayRate, int decayPeriod, int eventTime, int type) {
    /* select publisher, keywords, and topics from mysql */
    char* sqlbuf = nullptr;
    inv::INV_Mysql::MysqlData publisher_keywords_record;
    asprintf(&sqlbuf, SELECT_KEYWORDS_AND_PUBLISHER_FROM_T_CONTENT_YYYY_MM_BY_CONTENT_ID.c_str(), tableSuffix.c_str(), content_id.c_str());
    int ret = m_tQuery->Query(publisher_keywords_record, sqlbuf);
    if (ret != 0) {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" no record: "<<content_id<<endl);
        free(sqlbuf);
        sqlbuf = nullptr;

        return -1;
    }
    free(sqlbuf);
    sqlbuf = nullptr;

    //assert(records.size() > 0);
    std::string publisher = publisher_keywords_record[0][TQueryTable[TQUERY_CONTENT_PUBLISHER].C_STR()];
    std::multimap<std::string, std::string> keywords;
    ParseKeywordsFromTContentYYYYMM(publisher_keywords_record[0][TQueryTable[TQUERY_CONTENT_KEYWORDS].C_STR()].c_str(), keywords);
    
    inv::INV_Mysql::MysqlData topics_record;
    asprintf(&sqlbuf, SELECT_TOPICS_FROM_T_CONTENT_EXTEND_BY_CONTENT_ID.c_str(), content_id.c_str());
    ret = m_tQuery->Query(topics_record, sqlbuf);
    if (ret != 0) {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" no record: "<<content_id<<endl);
        free(sqlbuf);
        sqlbuf = nullptr;

        return -1;
    }
    free(sqlbuf);
    sqlbuf = nullptr;

    //assert(records.size() > 0);
    std::unordered_set<std::string> topics;
    ParseTopicsFromTContentExtened(topics_record[0][TQueryTable[TQUERY_EXTEND_TOPICS].C_STR()].c_str(), topics);

    std::tuple<std::string, std::multimap<std::string, std::string>, std::unordered_set<std::string>> elem = std::make_tuple(std::move(publisher), std::move(keywords), std::move(topics));
    uim_->MergeNewClickOrImp(type, uid, content_id, elem, decayRate, decayPeriod, eventTime);

    return 0;
}

// type: category or tag
int32_t SfuLogic::UpdateUfs(const std::string& type, const std::string& dbJson, const std::string& uid,
        const std::string& app, const std::unordered_set<std::string>& blackWords,
        const std::unordered_set<std::string>& versionToBeRemoved, const std::string& configId)
{
    FUNC_GUARD();
    // int curTime = time(NULL);
    std::map<std::string, std::map<std::string, double> > cwt;
    // GetWeightedTags(cwt, dbJson, "", 1.0);
    GetWeightedTags(cwt, dbJson);

    std::map<std::string, std::pair<uint32_t, double>> statsOfCt;
    for (const auto& vw: cwt)
    {
        if (versionToBeRemoved.count(vw.first) > 0)
        {
            continue;
        }
        std::string version = vw.first;
        std::string keyDecay = type + "_" + version + "_" + configId;
        std::map<std::string, double> merged(vw.second);

        double rate = this->defaultDecayRate_;//GET_CONF()->get_conf_float("decay_rate", 0.995);
        if (this->decayRate_.count(keyDecay) > 0)
        {
            rate = this->decayRate_[keyDecay];
        }

        std::shared_ptr<std::map<std::string, double>> history(new std::map<std::string, double>);
        // this->GetUserProfile(history, type, uid, version, keyDecay);
        // merged = SfuLogic::MergeWeightMaps(merged, history);
        assert(type == "tag" || type == "category");
        if (type == "tag")
        {
            this->uim_->GetUfsWeightedTag(history, version, uid, rate, this->decayPeriod_);
        }
        else
        {
            this->uim_->GetUfsWeightedCategory(history, version, uid, rate, this->decayPeriod_);
        }
        SfuLogic::MergeWeightMaps(history, merged);

        double sumOfWeight = 0.0;
        for_each(history->begin(), history->end(), [&sumOfWeight](const std::pair<std::string, double>& p){sumOfWeight += p.second;});
        statsOfCt.insert(std::make_pair(version, std::make_pair(history->size(), sumOfWeight)));
        
        // this->SetUserProfile(uid, version, merged, app, type, configId, blackWords);
        if (type == "tag")
        {
            this->uim_->SetUfsWeightedTag(version, uid, history, app, configId, this->ufsNum_, blackWords);
        }
        else
        {
            this->uim_->SetUfsWeightedCategory(version, uid, history, app, configId);
        }
    }
    // this->UpdateUfsStats(const std::string& type, const std::string& uid, statsOfCt);
    this->UpdateUfsStats(type, uid, statsOfCt);
    return 0;
}

int32_t SfuLogic::GetSsdbUfsStats(rapidjson::Document* pd, const std::string& type, const std::string& uid)
{
    // FUNC_GUARD();
    std::string rsp;
    // int ret = RedisHashGet(rsp, this->redis_, this->ufsHash_, uid);
    int ret = RedisHashGet(rsp, this->ChooseSsdb(uid), this->GetStatsHashName(type), uid);
    if (ret == 0)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" redis get ok: "<<ret<<"-"<<rsp.length()<<endl);
    }
    if (ret == 1)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" redis get, no data: "<< uid <<endl);
        return 1;
    }

    if (rsp.empty())
    {
        return 1;
    }
    pd->Parse(rsp.c_str());
    if (pd->HasParseError())
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" json parse fail: "<< pd->GetErrorOffset() << ": " << (rapidjson::GetParseError_En(pd->GetParseError())) << endl);
        return -1;
    }
    return 0;
}

int32_t SfuLogic::JsonStats2Map(std::map<std::string, std::pair<uint32_t, double>>& statsMap, const rapidjson::Document* pd)
{
    auto& data = pd->FindMember("data")->value;
    for (auto it = data.MemberBegin(); it != data.MemberEnd(); ++it)
    {
        const std::string version = it->name.GetString();
        const auto& stats = it->value;
        const uint32_t num = stats.FindMember("num")->value.GetInt();
        const double sum = stats.FindMember("sum")->value.GetDouble();
        statsMap.insert(std::make_pair(version, std::make_pair(num, sum)));
    }
    return 0;
}

// std::map<std::string, std::pair<uint32_t, double>>
int32_t SfuLogic::MergeStatsMaps(std::shared_ptr<std::map<std::string, std::pair<uint32_t, double>>>& oldMap,
        const std::map<std::string, std::pair<uint32_t, double>>& newMap)
{
    // std::map<std::string, std::pair<uint32_t, double>> out(newMap);
    for (auto it = newMap.begin(); it != newMap.end(); ++it)
    {
        // if (oldMap->count(it->first) <= 0)
        // {
        (*oldMap)[it->first] = it->second;
        // }
    }
    return 0;
}

std::string SfuLogic::GetStatsHashName(const std::string& type)
{
    if (type == "tag")
    {
        return this->ufsStatsTag_;
    }
    else if (type == "category")
    {
        return this->ufsStatsCategory_;
    }
    assert(0);
    return "";
}

int32_t SfuLogic::UpdateUfsStats(const std::string& type, const std::string& uid,
        const std::map<std::string, std::pair<uint32_t, double>>& statsOfCt)
{
    FUNC_GUARD();
    std::shared_ptr<std::map<std::string, std::pair<uint32_t, double>>> statsMerged(new std::map<std::string, std::pair<uint32_t, double>>);
    assert(type == "tag" || type == "category");
    if (type == "tag")
    {
        this->uim_->GetTagStats(statsMerged, uid);
    }
    else
    {
        this->uim_->GetCategoryStats(statsMerged, uid);
    }
    SfuLogic::MergeStatsMaps(statsMerged, statsOfCt);

    if (type == "tag")
    {
        this->uim_->SetTagStats(uid, statsMerged);
    }
    else
    {
        this->uim_->SetCategoryStats(uid, statsMerged);
    }

    return 0;
}

int32_t SfuLogic::FeedUfs(const std::string& kafkaMsg,
        const std::unordered_set<std::string>& blackWords,
        const std::unordered_set<std::string>& categoryVersionToBeRemoved,
        const std::unordered_set<std::string>& tagVersionToBeRemoved)
{
    FUNC_GUARD();
    // fprintf(stdout, "%s-%d, kafka msg: %s\n", __FILE__, __LINE__, kafkaMsg.c_str());
    TLOGINFO(__FILE__<<"-"<<__LINE__<<" kafka msg: "<<kafkaMsg<<endl);
    if (kafkaMsg.empty())
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" message error"<<endl);
        return -1;
    }

    std::string uid;
    std::string infoId;
    std::string dateTime;
    std::string app;
    std::string configId;

    int contentType = 0;
    int ret = this->ParseClickMsg(uid, infoId, dateTime, app, configId, kafkaMsg, contentType);
    if (ret != 0)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" ParseClickMsg fail"<<endl);
        return -1;
    }

    TLOGINFO(__FILE__<<"-"<<__LINE__<<" infoid: "<<infoId<<", uid: "<<uid<<endl);
    inv::INV_Mysql::MysqlData tabelSuffixRecords;
    ret = this->tableIndex_->GetTabelIndexByContentId(tabelSuffixRecords, infoId);
    if (ret != 0)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<"tableSuffix no record: "<<infoId<<endl);
        return -1;
    }
    std::string tableSuffix = tabelSuffixRecords[0]["table_name"];
    
    inv::INV_Mysql::MysqlData records;
    ret = this->signal_->GetRecordsByInfoId(records, infoId, tableSuffix);
    if (ret != 0)
    {
        // fprintf(stderr, "no record: %s\n", infoId.c_str());
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" no record: "<<infoId<<endl);
        return -1;
    }
    assert(records.size() > 0);
    // std::string keywords = records[0]["keywords"];
    std::string tags = records[0]["tags"];
    std::string categories = records[0]["categories"];

    int32_t eventTime = 0;
    if (dateTime.length() > 10)
    {
        ret = this->GetTimestampFromDateTime(dateTime, eventTime);
    }
    else
    {
        eventTime = atoi(dateTime.c_str());
    }

    int32_t span = time(NULL) - eventTime;
    if (eventTime > 1400000000 && span >= 0)
    {
        ReportMax("zhizi.ufs.click.till.process.max", span);
        ReportMin("zhizi.ufs.click.till.process.min", span);
        ReportAvg("zhizi.ufs.click.till.process.avg", span);
    }

    do {
        if (contentType == ContentTypeTableInt[CONTENT_TYPE_NEWS]) {
            this->UpdateUfs("category", categories, uid, app, blackWords, categoryVersionToBeRemoved, configId);
            this->UpdateUfs("tag", tags, uid, app, blackWords, tagVersionToBeRemoved, configId);
            this->UpdateUfsCategoryClickOrImp(categories, uid, eventTime, "click");

            break;
        }

        if (contentType == ContentTypeTableInt[CONTENT_TYPE_SHORT_VIDEO]) {
            UpdateUfsTQueryClickOrImp(uid, tableSuffix, infoId, defaultDecayRate_, decayPeriod_, eventTime, EVENT_CLICK);

            break;
        }
    } while (false);

    return 0;
}

int32_t SfuLogic::FeedImpUfs(const std::string kafkaMsg)
{
    //FUNC_GUARD();
    TLOGINFO(__FILE__<<"-"<<__LINE__<<" kafka imp msg: "<<kafkaMsg<<endl);
    if (kafkaMsg.empty())
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" message error"<<endl);
        return -1;
    }

    std::string uid;
    std::string infoId;
    std::string dateTime;
    std::string app;
    std::string configId;

    int contentType = 0;
    int ret = this->ParseImpressionMsg(uid, infoId, dateTime, app, kafkaMsg, contentType);
    if (ret != 0)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" ParseImpressionMsg fail"<<endl);
        return -1;
    }
    TLOGINFO(__FILE__<<"-"<<__LINE__<<" infoid: "<<infoId<<", uid: "<<uid<<endl);

    inv::INV_Mysql::MysqlData tabelSuffixRecords;
    ret = this->tableIndex_->GetTabelIndexByContentId(tabelSuffixRecords, infoId);
    if (ret != 0)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<"tableSuffix no record: "<<infoId<<endl);
        return -1;
    }
    std::string tableSuffix = tabelSuffixRecords[0]["table_name"];

    inv::INV_Mysql::MysqlData records;
    ret = this->signal_->GetRecordsByInfoId(records, infoId, tableSuffix);
    if (ret != 0)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" no record: "<<infoId<<endl);
        return -1;
    }
    assert(records.size() > 0);
    std::string categories = records[0]["categories"];

    int32_t eventTime = 0;
    if (dateTime.length() > 10)
    {
        ret = this->GetTimestampFromDateTime(dateTime, eventTime);
    }
    else
    {
        eventTime = atoi(dateTime.c_str());
    }

    int32_t span = time(NULL) - eventTime;
    if (eventTime > 1400000000 && span >= 0)
    {
        ReportMax("zhizi.ufs.imp.till.process.max", span);
        ReportMin("zhizi.ufs.imp.till.process.min", span);
        ReportAvg("zhizi.ufs.imp.till.process.avg", span);
    }

    do {
        if (contentType == ContentTypeTableInt[CONTENT_TYPE_NEWS]) {
            this->UpdateUfsCategoryClickOrImp(categories, uid, eventTime, "imp");

            break;
        }

        if (contentType == ContentTypeTableInt[CONTENT_TYPE_SHORT_VIDEO]) {
            UpdateUfsTQueryClickOrImp(uid, tableSuffix, infoId, defaultDecayRate_, decayPeriod_, eventTime, EVENT_IMP);

            break;
        }
    } while(false);

    return 0;
}

int SfuLogic::GetByTQuery(const std::string& content_id, const std::string& tableSuffix, std::map<std::string, std::string>& out) {
    /* Get Topics from t_content_extend */
    char* sql = nullptr;
    int ret = 0;
    {
        inv::INV_Mysql::MysqlData records;
        asprintf(&sql, SELECT_TOPICS_FROM_T_CONTENT_EXTEND_BY_CONTENT_ID.c_str(), content_id.c_str());
        std::shared_ptr<char> psql(sql, free);
        
        ret = m_tQuery->Query(records, sql);
        if (ret != 0) {
            TLOGINFO(__FILE__<<"-"<<__LINE__<<"tableSuffix no record|sql="<<m_tQuery->GetLastErrMsg()<<endl);

            return -1;
        }

        out.emplace(TQueryTable[TQUERY_EXTEND_TOPICS].C_STR(), std::move(records[0][TQueryTable[TQUERY_EXTEND_TOPICS].C_STR()]));
    }

    {
        inv::INV_Mysql::MysqlData records;
        asprintf(&sql, SELECT_KEYWORDS_AND_PUBLISHER_FROM_T_CONTENT_YYYY_MM_BY_CONTENT_ID.c_str(), tableSuffix.c_str(), content_id.c_str());
        std::shared_ptr<char> psql(sql, free);
        
        ret = m_tQuery->Query(records, sql);
        if (ret != 0) {
            TLOGINFO(__FILE__<<"-"<<__LINE__<<"tableSuffix no record|sql="<<m_tQuery->GetLastErrMsg()<<endl);

            return -1;
        }

        out.emplace(TQueryTable[TQUERY_CONTENT_KEYWORDS].C_STR(), std::move(records[0][TQueryTable[TQUERY_CONTENT_KEYWORDS].C_STR()]));
        out.emplace(TQueryTable[TQUERY_CONTENT_PUBLISHER].C_STR(), std::move(records[0][TQueryTable[TQUERY_CONTENT_PUBLISHER].C_STR()]));

        return 0;
    }
}

inv::INV_Redis* SfuLogic::ChooseSsdb(const std::string& uid)
{
    size_t idx = GetIdxOfConns(uid, this->vecRedis_.size(), "");
    TLOGINFO(__FILE__<<"-"<<__LINE__<<" "<< uid << " " << idx <<endl);

    return vecRedis_[idx];
}

int32_t SfuLogic::GetRpcJson(std::string& _return, const std::string& type, const std::string& uid, const std::string& version)
{
    rapidjson::MemoryPoolAllocator<> valueAllocator(this->valueBuffer_, this->jsonBufSize_);
    rapidjson::MemoryPoolAllocator<> parseAllocator(this->parseBuffer_, this->jsonBufSize_);
    DocumentType d(&valueAllocator, this->jsonBufSize_, &parseAllocator);
    if (this->GetSsdbJson(&d, type, version, uid) != 0)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" GetSsdbJson fail " << type << " " << version << " " << uid <<endl);
        return -1;
    }
    std::map<std::string, double> history;
    time_t curTime = time(NULL);
    std::string configId;
    if (d.FindMember("config") != d.MemberEnd())
    {
        configId = d.FindMember("config")->value.GetString();
    }
    configId = type + "_" + version + "_" + configId;
    this->JsonWeight2Map(history, type, &d, curTime, configId);
    std::unordered_set<std::string> blackWords;
    std::vector<std::pair<std::string, double> > sorted = this->SortMapByWeight(history, blackWords);
    SfuLogic::SerializeToSsdbJson(_return, sorted, curTime, "", configId);
    return 0;
}

