#include <cassert>
#include <cstdio>

#include "db.h"
#include "util/inv_util.h"
#include "util/inv_httpclient.h"
#include "inv_log.h"

#include "rapidjson/writer.h"
#include "rapidjson/stringbuffer.h"
#include "rapidjson/error/en.h"

#include "ufs_logic.h"
#include "util.h"
#include "report.h"
#include"zhizi_define.h"

using namespace inv;
using namespace inv::monitor;

SfuLogic::SfuLogic(std::shared_ptr<inv::INV_Mysql> conn,
        const std::vector<inv::INV_Redis*>& vecRedis,
        const std::string& ufsHash, int timeZone,
        std::shared_ptr<KVCache<std::string, std::vector<std::string>>> cache,
        const std::string& typicalVersion)
:conn_(conn), vecRedis_(vecRedis),
    ufsHash_(ufsHash), timeZone_(timeZone),
    cache_(cache), typicalVersion_(typicalVersion)
{
    this->signal_ = new TSignal(this->conn_);
}

// SfuLogic::SfuLogic(const std::vector<inv::INV_Redis*>& vecRedis, const std::string& reformatHash)
// :vecRedis_(vecRedis), ufsHash_(reformatHash)
// {
// }

int32_t SfuLogic::ParseImpressionMsg(std::string& uid, std::string& infoId, std::string& dateTime, std::string& app, const std::string& kafkaMsg, int& contentType)
{
    rapidjson::Document d;
    d.Parse(kafkaMsg.c_str());

    if (d.HasParseError() || !d.IsObject()) {
        return -1;
    }

    rapidjson::Value::ConstMemberIterator it;
    auto end = d.MemberEnd();
    auto& extra = d.FindMember("article_impression_extra")->value;
    auto extraEnd = extra.MemberEnd();

    if (d.FindMember("product_id") != d.MemberEnd()) {
        app = d.FindMember("product_id")->value.GetString();
    }

    if ((it = extra.FindMember("content_type")) == extraEnd) {
        FDLOG("error")<<__FILE__<<"-"<<__LINE__<< ParseErrorTableStr[PARSE_NO_CONTENT_TYPE];

        return -1;
    }
    const char* content_type = it->value.GetString();
    if (strcmp(content_type, ContentTypeTableStr[CONTENT_TYPE_NEWS][2])== 0) {
        contentType = ContentTypeTableInt[CONTENT_TYPE_NEWS];
    } else if (strcmp(content_type, ContentTypeTableStr[CONTENT_TYPE_SHORT_VIDEO][2]) == 0) {
        contentType = ContentTypeTableInt[CONTENT_TYPE_SHORT_VIDEO];
    } else  {
        FDLOG("error")<<__FILE__<<"-"<<__LINE__<< ParseErrorTableStr[PARSE_CONTENT_TYPE]<<content_type<<std::endl;

        return 1;
    }

    if ((it = d.FindMember("uid")) == end) {
        FDLOG("error")<<__FILE__<<"-"<<__LINE__<< ParseErrorTableStr[PARSE_NO_UID];

        return -1;
    }
    uid = it->value.GetString();

    if ((it = extra.FindMember("content_id")) == extraEnd) {
        FDLOG("error")<<__FILE__<<"-"<<__LINE__<< ParseErrorTableStr[PARSE_NO_CONTENT_ID];

        return -1;
    }
    infoId = it->value.GetString();

    if ((it = d.FindMember("event_time")) == end) {
        FDLOG("error")<<__FILE__<<"-"<<__LINE__<< ParseErrorTableStr[PARSE_NO_EVNET_TIME];
        if ((it = d.FindMember("log_time")) == end) {
            FDLOG("error")<<__FILE__<<"-"<<__LINE__<< ParseErrorTableStr[PARSE_NO_LOG_TIME];

            return -1;
        } else {
            /* log_time as dateTime */
            dateTime = it->value.GetString();
        }
    } else {
        /* event_time as dateTime */
        dateTime = it->value.GetString();
    }

    return 0;
}

int32_t SfuLogic::ParseClickMsg(std::string& uid, std::string& infoId, std::string& dateTime, std::string& app, std::string& configId, const std::string& kafkaMsg, int& contentType)
{
    rapidjson::Document d;
    d.Parse(kafkaMsg.c_str());

    if (d.HasParseError() || !d.IsObject()) {
        return -1;
    }

    rapidjson::Value::ConstMemberIterator it;
    auto end = d.MemberEnd();
    auto& extra = d.FindMember("article_click_extra")->value;
    auto extraEnd = extra.MemberEnd();

    if (d.FindMember("app") != d.MemberEnd()) {
        app = d.FindMember("app")->value.GetString();
    }

    if ((it = extra.FindMember("content_type")) == extraEnd) {
        FDLOG("error")<<__FILE__<<"-"<<__LINE__<< ParseErrorTableStr[PARSE_NO_CONTENT_TYPE];

        return -1;
    }
    const char* content_type = it->value.GetString();
    if (strcmp(content_type, ContentTypeTableStr[CONTENT_TYPE_NEWS][2])== 0) {
        contentType = ContentTypeTableInt[CONTENT_TYPE_NEWS];
    } else if (strcmp(content_type, ContentTypeTableStr[CONTENT_TYPE_SHORT_VIDEO][2]) == 0) {
        contentType = ContentTypeTableInt[CONTENT_TYPE_SHORT_VIDEO];
    } else  {
        FDLOG("error")<<__FILE__<<"-"<<__LINE__<<ParseErrorTableStr[PARSE_CONTENT_TYPE]<< content_type << endl;

        return 1;
    }

    if ((it = d.FindMember("uid")) == end) {
        FDLOG("error")<<__FILE__<<"-"<<__LINE__<< ParseErrorTableStr[PARSE_NO_UID];

        return -1;
    }
    uid = it->value.GetString();

    if ((it = extra.FindMember("content_id")) == extraEnd) {
        FDLOG("error")<<__FILE__<<"-"<<__LINE__<< ParseErrorTableStr[PARSE_NO_CONTENT_ID];

        return -1;
    }
    infoId = it->value.GetString();

    if ((it = d.FindMember("event_time")) == end) {
        FDLOG("error")<<__FILE__<<"-"<<__LINE__<< ParseErrorTableStr[PARSE_NO_EVNET_TIME];
        if ((it = d.FindMember("log_time")) == end) {
            FDLOG("error")<<__FILE__<<"-"<<__LINE__<< ParseErrorTableStr[PARSE_NO_LOG_TIME];

            return -1;
        } else {
            /* log_time as dateTime */
            dateTime = it->value.GetString();
        }
    } else {
        /* event_time as dateTime */
        dateTime = it->value.GetString();
    }

    const auto& upack = d.FindMember("upack");
    if (upack != d.MemberEnd())
    {
        const auto& ab = upack->value.FindMember("abtest_ver");
        if (ab != upack->value.MemberEnd())
        {
            configId = ab->value.GetString();
        }
    }

    return 0;
}

int32_t SfuLogic::GetCategoryOfInfo(std::vector<std::string>& category, const std::string& infoId)
{
    const std::string key(infoId);
    int ret = this->cache_->Get(category, key);
    if (ret == 0)
    {
        return 0;
    }
    if (this->GetCategoryFromDb(category, key) != 0)
    {
        return -1;
    }
    this->cache_->Set(key, category);
    return 0;
}

int32_t SfuLogic::GetCategoryFromDb(std::vector<std::string>& category, const std::string& infoId)
{
    inv::INV_Mysql::MysqlData records;
    int ret = this->signal_->GetRecordsByInfoId(records, infoId);
    if (ret > 0)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" no record: "<<infoId<<endl);
        return 1;
    }
    if (ret < 0)
    {
        return -1;
    }
    assert(records.size() > 0);
    std::string categories = records[0]["categories"];

    rapidjson::Document d;
    d.Parse(categories.c_str());

    if (d.HasParseError())
    {
        return -1;
    }

    if (!d.IsObject())
    {
        return -1;
    }

    auto v1 = d.FindMember(this->typicalVersion_.c_str());
    if (v1 == d.MemberEnd())
    {
        return 1;
    }
    category.clear();
    for (rapidjson::Value::ConstMemberIterator it = v1->value.MemberBegin();
            it != v1->value.MemberEnd(); ++it)
    {
        category.push_back(it->name.GetString());
    }
    return 0;
}

int32_t SfuLogic::GetCategoryOfUserFromSsdb(std::map<std::string, int32_t>& category, const std::string& uid)
{
    std::string rsp;
    rapidjson::Document d;
    int ret = RedisHashGet(rsp, this->ChooseSsdb(uid), this->ufsHash_, uid);
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
    d.Parse(rsp.c_str());
    if (d.HasParseError())
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" json parse fail: "<< d.GetErrorOffset() << ": " 
                << (rapidjson::GetParseError_En(d.GetParseError())) << endl);
        return -1;
    }
    if (!d.IsObject())
    {
        return -1;
    }
    category.clear();
    auto last = d.FindMember("last_action_time");
    if (last == d.MemberEnd())
    {
        return -1;
    }
    for (rapidjson::Value::ConstMemberIterator it = last->value.MemberBegin();
            it != last->value.MemberEnd(); ++it)
    {
        category.insert(std::make_pair(it->name.GetString(), it->value.GetInt()));
    }
    return 0;
}

int32_t SfuLogic::GetCategoryOfUser(std::map<std::string, int32_t>& category, const std::string& uid)
{
    ReportIncr("zhizi.ufs.reformat.cache-query");
    if (this->reformatCache_.count(uid) > 0)
    {
        category = this->reformatCache_[uid];
        ReportIncr("zhizi.ufs.reformat.cache-hit");
        return 0;
    }
    int ret = this->GetCategoryOfUserFromSsdb(category, uid);
    if (ret != 0)
    {
        return ret;
    }
    this->reformatCache_[uid] = category;
    return 0;
}

std::string SfuLogic::MakeJsonOfCategory(const std::map<std::string, int32_t>& category, const std::string& app)
{
    rapidjson::StringBuffer s;
    rapidjson::Writer<rapidjson::StringBuffer> writer(s);
    writer.StartObject();
    writer.Key("app");
    writer.String(app.c_str());
    writer.Key("last_action_time");
    writer.StartObject();
    for (const auto& it: category)
    {
        writer.Key(it.first.c_str());
        writer.Int(it.second);
    }
    writer.EndObject();
    writer.EndObject();
    assert(writer.IsComplete());
    return s.GetString();
}

int32_t SfuLogic::UpdateClickTimeOfCategory(const std::string& kafkaMsg)
{
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

#if 0
    int ret = this->ParseClickMsg(uid, infoId, dateTime, app, configId, kafkaMsg, contentType);
    if (ret != 0)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" ParseClickMsg fail"<<endl);
        return -1;
    }
#else
    int contentType = 0;
    int ret = ParseClickMsg(uid, infoId, dateTime, app, configId, kafkaMsg, contentType);
    if (ret != 0) {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" ParseClickMsg fail"<<endl);
        return -1;
    }
#endif

    int eventTime;
    int curTime = time(NULL);
    if (this->GetTimestampFromDateTime(dateTime, eventTime))
    {
        eventTime = curTime;
    }

    std::vector<std::string> category;
    ret = this->GetCategoryOfInfo(category, infoId);
    if (ret != 0)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" GetCategoryOfInfo fail: "<< infoId <<endl);
        return -1;
    }
    if (category.empty())
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" no category : "<< infoId <<endl);
        return 0;
    }
    std::map<std::string, int32_t> categoryClick;
    ret = this->GetCategoryOfUser(categoryClick, uid);
    if (ret != 0)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" GetCategoryOfUser fail: "<< uid <<endl);
    }
    for (const auto& it: category)
    {
        if (categoryClick.count(it) <= 0)
        {
            categoryClick.insert(std::make_pair(it, eventTime));
        }
        else
        {
            if (eventTime > categoryClick[it])
            {
                categoryClick[it] = eventTime;
            }
        }
    }
    ret = this->SetUserCategory(uid, categoryClick, app);
    if (ret != 0)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" redis set fail: "<< uid <<endl);
        return -1;
    }
    return 0;
}

int32_t SfuLogic::UpdateImpressionTimeOfCategory(const std::string& kafkaMsg)
{
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

#if 0
    int ret = this->ParseImpressionMsg(uid, infoId, dateTime, app, kafkaMsg);
    if (ret != 0)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" ParseImpressionMsg fail"<<endl);
        return -1;
    }
#else
    int contentType = 0;
    int ret = ParseImpressionMsg(uid, infoId, dateTime, app, kafkaMsg, contentType);
    if (ret != 0 || contentType != ContentTypeTableInt[CONTENT_TYPE_NEWS])
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" ParseImpressionMsg fail"<<endl);
        return -1;
    }
#endif
    int eventTime;
    int curTime = time(NULL);
    if (this->GetTimestampFromDateTime(dateTime, eventTime))
    {
        eventTime = curTime;
    }

    std::vector<std::string> category;
    ret = this->GetCategoryOfInfo(category, infoId);
    if (ret != 0)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" GetCategoryOfInfo fail: "<< infoId <<endl);
        return -1;
    }
    if (category.empty())
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" no category : "<< infoId <<endl);
        return 0;
    }
    std::map<std::string, int32_t> categoryImpression;
    ret = this->GetCategoryOfUser(categoryImpression, uid);
    if (ret != 0)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" GetCategoryOfUser fail: "<< uid <<endl);
    }
    for (const auto& it: category)
    {
        if (categoryImpression.count(it) <= 0)
        {
            categoryImpression.insert(std::make_pair(it, eventTime));
        }
        else
        {
            if (eventTime > categoryImpression[it])
            {
                categoryImpression[it] = eventTime;
            }
        }
    }
    ret = this->SetUserCategory(uid, categoryImpression, app);
    if (ret != 0)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" redis set fail: "<< uid <<endl);
        return -1;
    }
    return 0;
}

int32_t SfuLogic::SetUserCategory(const std::string& uid, const std::map<std::string, int32_t>& category, const std::string& app)
{
    TLOGINFO(__FILE__<<"-"<<__LINE__<<" reformatCache_ size: "<< this->reformatCache_.size() <<endl);
    if (this->reformatCache_.count(uid) > 0) // update
    {
        this->reformatCache_[uid] = category;
    }
    else // insert
    {
        if (this->reformatCache_.size() > 10000)
        {
            TLOGINFO(__FILE__<<"-"<<__LINE__<<" ignore msg, huge reformatCache_: "<< this->reformatCache_.size() <<endl);
        }
        else
        {
            this->reformatCache_[uid] = category;
        }
    }
    int oriSize = this->reformatCache_.size();
    if (oriSize < 6000)
    {
        return 0;
    }
    int writeSize = this->WriteBack(app);
    TLOGINFO(__FILE__<<"-"<<__LINE__<< " " << writeSize << " of " << oriSize << " keys writeback" <<endl);
    return 0;
}

int32_t SfuLogic::WriteBack(const std::string& app)
{
    std::vector<std::vector<std::string>> keysOfConn(this->vecRedis_.size());
    std::vector<std::vector<std::string>> valsOfConn(this->vecRedis_.size());

    for (const auto& it: this->reformatCache_)
    {
        const std::string uid = it.first;
        size_t idx = GetIdxOfConns(uid, this->vecRedis_.size(), "");
        keysOfConn[idx].push_back(uid);
        std::string jsonStr = this->MakeJsonOfCategory(it.second, app);
        valsOfConn[idx].push_back(jsonStr);
        assert(keysOfConn[idx].size() == valsOfConn[idx].size());
    }
    int count = 0;
    for (size_t i = 0; i < this->vecRedis_.size(); ++i)
    {
        assert(keysOfConn[i].size() == valsOfConn[i].size());
        if (keysOfConn[i].size() == 0)
        {
            continue;
        }
        int ret = RedisHashMSet(this->vecRedis_[i], this->ufsHash_, keysOfConn[i], valsOfConn[i]);
        ReportAvg("zhizi.ufs.reformat.write-size", keysOfConn[i].size());
        ReportIncr("zhizi.ufs.reformat.write-count");
        if (ret != 0)
        {
            TLOGINFO(__FILE__<<"-"<<__LINE__<<" redis set fail, size: "<< keysOfConn[i].size() <<endl);
        }
        // else
        // {
        count += keysOfConn[i].size();
        for (const auto& it : keysOfConn[i])
        {
            this->reformatCache_.erase(it);
        }
        // }
    }
    return count;
}

