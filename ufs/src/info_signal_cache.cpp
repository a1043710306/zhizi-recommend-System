#include "rapidjson/document.h"     // rapidjson's DOM-style API
#include "rapidjson/writer.h"
#include "rapidjson/stringbuffer.h"
#include "rapidjson/error/en.h"

#include "info_signal_cache.h"
#include "util.h"
#include "report.h"

using namespace inv::monitor;
InfoSignalCache::InfoSignalCache(TSignal* signal, size_t capacity, int timeZone)
    :signal_(signal), cache_(capacity), timeZone_(timeZone)
{
    assert(pthread_mutex_init(&this->conn_lock_, NULL) == 0);
}

int InfoSignalCache::GetSignal(inv::INV_Mysql::MysqlData& records, const std::string& infoId)
{
    pthread_mutex_lock(&this->conn_lock_);
    int ret = this->signal_->GetRecordsByInfoId(records, infoId);
    pthread_mutex_unlock(&this->conn_lock_);
    return ret;
}

int InfoSignalCache::GetTitleTags(std::map<std::string, std::map<std::string, double>>& out, const std::string& infoId)
{
    FUNC_GUARD();
    const std::string prefix = "tt_";
    const std::string key = prefix + infoId;
    ReportIncr("zhizi.info.title_tag.get");
    ReportAvg("zhizi.infoCache.size.avg", this->cache_.Size());
    if (0 == this->cache_.Get(out, key))
    {
        ReportIncr("zhizi.info.title_tag.hit");
        return 0;
    }

    inv::INV_Mysql::MysqlData records;
    int ret = this->GetSignal(records, infoId);
    if (ret != 0)
    {
        return ret;
    }

    std::string json = records[0]["tags_title"];
    int uts = 0;
    ret = TimestampFromDateTime(uts, records[0]["update_time"], this->timeZone_);
    if (ret != 0)
    {
        uts = time(NULL);
    }
    ret = InfoSignalCache::GetWeightedTags(out, json);
    if (ret != 0)
    {
        return ret;
    }

    this->cache_.Set(key, out, uts);
    return 0;
}

int InfoSignalCache::GetCategories(std::map<std::string, std::map<std::string, double>>& out, const std::string& infoId)
{
    FUNC_GUARD();
    const std::string prefix = "ctgy_";
    const std::string key = prefix + infoId;
    ReportIncr("zhizi.info.category.get");
    ReportAvg("zhizi.infoCache.size.avg", this->cache_.Size());
    if (0 == this->cache_.Get(out, key))
    {
        ReportIncr("zhizi.info.category.hit");
        return 0;
    }

    inv::INV_Mysql::MysqlData records;
    int ret = this->GetSignal(records, infoId);
    if (ret != 0)
    {
        return ret;
    }

    std::string json = records[0]["categories"];
    int uts = 0;
    ret = TimestampFromDateTime(uts, records[0]["update_time"], this->timeZone_);
    if (ret != 0)
    {
        uts = time(NULL);
    }
    ret = InfoSignalCache::GetWeightedTags(out, json);
    if (ret != 0)
    {
        return ret;
    }

    this->cache_.Set(key, out, uts);
    return 0;
}

int InfoSignalCache::GetTags(std::map<std::string, std::map<std::string, double>>& out, const std::string& infoId)
{
    FUNC_GUARD();
    const std::string prefix = "tag_";
    const std::string key = prefix + infoId;
    ReportIncr("zhizi.info.tag.get");
    ReportAvg("zhizi.infoCache.size.avg", this->cache_.Size());
    if (0 == this->cache_.Get(out, key))
    {
        ReportIncr("zhizi.info.tag.hit");
        return 0;
    }

    inv::INV_Mysql::MysqlData records;
    int ret = this->GetSignal(records, infoId);
    if (ret != 0)
    {
        return ret;
    }

    std::string json = records[0]["tags"];
    int uts = 0;
    ret = TimestampFromDateTime(uts, records[0]["update_time"], this->timeZone_);
    if (ret != 0)
    {
        uts = time(NULL);
    }
    ret = InfoSignalCache::GetWeightedTags(out, json);
    if (ret != 0)
    {
        return ret;
    }

    this->cache_.Set(key, out, uts);
    return 0;
}

int32_t InfoSignalCache::GetWeightedTags(std::map<std::string, std::map<std::string, double> >& ret,
        const std::string& json)
{
    ret.clear();
    rapidjson::Document d;
    d.Parse(json.c_str());

    if (d.HasParseError())
    {
        return -1;
    }

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
            double weight = jtr->value.FindMember("weight")->value.GetDouble();
            wtags.insert(std::make_pair(tag, weight));
        }
        ret.insert(std::make_pair(ver, wtags));
    }
    return 0;
}

