#include <unistd.h>

#include "common_weight.h"
#include "report.h"
#include "inv_log.h"

#include "rapidjson/writer.h"
#include "rapidjson/stringbuffer.h"
#include "rapidjson/error/en.h"

using namespace inv;
using namespace inv::monitor;

CommonWeight::CommonWeight(size_t cacheSize, const std::string& hashName,
        const std::vector<inv::INV_Redis*>& vecRedis,
        const std::vector<inv::INV_Redis*>& vecWRedis, int32_t writeCold)
    :cacheSize_(cacheSize), hash_(hashName), 
    cache_(cacheSize_), ssdb_(new SsdbIo(vecRedis, vecWRedis, writeCold))
{
    this->valueBuffer_ = new char[this->jsonBufSize_];
    this->parseBuffer_ = new char[this->jsonBufSize_];
}

int32_t CommonWeight::GetWeightedTag(std::shared_ptr<WU>& out, const std::string& version, const std::string& uid,
        double decayRate, int32_t decayPeriod)
{
    FUNC_GUARD();
    // out.clear();
    const std::string key = version + "|" + uid;
    ReportIncr("zhizi.ufs.wt.get");
    if (this->cache_.Get(out, key) != 0) // not hit
    {
        std::string json;
        std::string hash = this->GetHashName("", version);
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
        this->cache_.Set(key, out);
    }
    else
    {
        ReportIncr("zhizi.ufs.wt.hit");
    }
    return 0;
}

int32_t CommonWeight::SetWeightedTag(const std::string& version, const std::string& uid, const std::shared_ptr<WU>& out,
        const std::string& app, const std::string& configId, uint32_t tagNumLimit,
        const std::unordered_set<std::string>& blackWords)
{
    FUNC_GUARD();
    const std::string key = version + "|" + uid;
    this->cache_.Set(key, out);
    ReportAvg("zhizi.ufs.cache_.size", this->cache_.Size());

    std::vector<std::pair<std::string, double> > sorted = SortMapByWeight(out, blackWords, tagNumLimit);
    std::string hash = this->GetHashName("", version);
    std::shared_ptr<std::string> json(new std::string);
    this->SerializeJson(json, sorted, app, configId);
    if (0 != this->ssdb_->AddSyncTask(hash, uid, json))// queue full, slow down
    {
        usleep(this->nap_);
    }
    return 0;
}

int32_t CommonWeight::SerializeJson(std::shared_ptr<std::string>& out, std::vector<std::pair<std::string, double> >& wtags,
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

int32_t CommonWeight::JsonWeightArray2Map(std::shared_ptr<WU>& out/*, const std::string& type*/, const rapidjson::Value& obj, double decayFactor)
{
    for (rapidjson::Value::ConstValueIterator itr = obj.Begin();
            itr != obj.End(); ++itr)
    {
        std::string tagKey = itr->GetObject().FindMember("tag")->value.GetString();
        double tagWeight = itr->GetObject().FindMember("weight")->value.GetDouble() * decayFactor;
        out->insert(std::make_pair(tagKey, tagWeight));
    }
    return 0;
}

int32_t CommonWeight::JsonWeight2Map(std::shared_ptr<WU>& out, const std::string& json,
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

    double decayFactor = CommonWeight::GetDecayFactor(time(NULL), d.FindMember("ts")->value.GetInt(), decayRate, decayPeriod);
    rapidjson::Value::ConstMemberIterator itwt = d.FindMember("weighted");
    CommonWeight::JsonWeightArray2Map(out, itwt->value, decayFactor);

    return 0;
}

double CommonWeight::GetDecayFactor(int32_t current, int32_t createTime, double rate, int32_t period)
{
    assert(period > 0);
    if (createTime <= 0)
    {
        createTime = time(NULL) - 1*24*60*60;
    }

    return std::pow(rate, (current - createTime) / period);
}

bool CommonWeight::IsDecimal(const std::string& word)
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

std::string CommonWeight::GetHashName(const std::string& type, const std::string& version)
{
    return this->hash_ + version;
    // std::string hashName;
    // if (type == "category")
    // {
    //     hashName = this->hashCategory_ + version;
    // }
    // else if (type == "tag")
    // {
    //     hashName = this->hashTag_ + version;
    // }
    // else
    // {
    //     assert(0);
    // }
    // return hashName;
}

std::vector<std::pair<std::string, double> >
CommonWeight::SortMapByWeight(const std::shared_ptr<WU>& merged, const std::unordered_set<std::string>& blackWords, uint32_t tagNumLimit)
{
    FUNC_GUARD();
    std::vector<std::pair<std::string, double> > t;
    for (const auto& j: *merged)
    {
        t.push_back(j);
    }
    std::sort(t.begin(), t.end(), CommonWeight::WeightCmp);
    std::vector<std::pair<std::string, double> > out;
    for (size_t k = 0; k < t.size(); ++k)
    {
        if (out.size() >= tagNumLimit)
        {
            break;
        }
        if (fabs(t[k].second) < 0.001)
        {
            break;
        }
        if (blackWords.count(t[k].first) > 0)
        {
            continue;
        }
        out.push_back(t[k]);
    }
    return out;
}

int32_t CommonWeight::MergeWeightMaps(std::shared_ptr<std::map<std::string, double>>& m1,
        const std::map<std::string, double>& m2, int sign)
{
    assert(sign == 1 || sign == -1);
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
            (*m1)[it->first] += sign * it->second;
        }
    }
    return 0;
}
