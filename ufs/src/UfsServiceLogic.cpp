#include "util/inv_redis.h"
#include "util/inv_config.h" 
#include "inv_log.h"
#include "report.h"

#include"ufs_json_define.h"

#include "UfsServiceLogic.h"
#include "util.h"
#include"const_str.h"
#include"zhizi_define.h"
#include"ssdb_io_sync.h"
#include <mutex>

using namespace inv;
using namespace inv::monitor;
std::once_flag winner_flag;

#define X(a, b) [a]=StrLiter(b),
const StrLiter UserInfoTableStr[USER_INFO_OVERFLOW + 1] = {
    USER_INFO_TABLE
};
#undef X

ThreadPool* g_pool = nullptr;
bool gRun = false;
UfsServiceLogic::UfsServiceLogic() {
}

UfsServiceLogic::~UfsServiceLogic() {
    DELETE(&logic_);

    {
        auto end = vecTagRedis_.end();
        for (auto it = vecTagRedis_.begin(); it != end; ++it) {
            DELETE(&(*it));
        }
    }

    {
        auto end = vecCategoryRedis_.end();
        for (auto it = vecCategoryRedis_.begin(); it != end; ++it) {
            DELETE(&(*it));
        }
    }

    {
        auto end = vecNegativeRedis_.end();
        for (auto it = vecNegativeRedis_.begin(); it != end; ++it) {
            DELETE(&(*it));
        }
    }

    {
        auto end = vecTopicRedis_.end();
        for (auto it = vecTopicRedis_.begin(); it != end; ++it) {
            DELETE(&(*it));
        }
    }

    {
        auto end = vecLastClickRedis_.end();
        for (auto it = vecLastClickRedis_.begin(); it != end; ++it) {
            DELETE(&(*it));
        }
    }

    {
        auto end = vecLastImpressionRedis_.end();
        for (auto it = vecLastImpressionRedis_.begin(); it != end; ++it) {
            DELETE(&(*it));
        }
    }

    {
        auto end = vecUserGmpRedis_.end();
        for (auto it = vecUserGmpRedis_.begin(); it != end; ++it) {
            DELETE(&(*it));
        }
    }

    {
        auto end = vecProfileRedis_.end();
        for (auto it = vecProfileRedis_.begin(); it != end; ++it) {
            DELETE(&(*it));
        }
    }

    {
        auto end = vecBehaviorRedis_.end();
        for (auto it = vecBehaviorRedis_.begin(); it != end; ++it) {
            DELETE(&(*it));
        }
    }

    {
        auto end = vecCategoryClickAndImpRedis_.end();
        for (auto it = vecCategoryClickAndImpRedis_.begin(); it != end; ++it) {
            DELETE(&(*it));
        }
    }

    {
        auto end = m_vecNewUidRedis.end();
        for (auto it = m_vecNewUidRedis.begin(); it != end; ++it) {
            DELETE(&(*it));
        }
    }

    for (int i=0; i < TQUERY_OVERFLOW; ++i) {
        DELETE(&m_valueAllocator[i]);
        DELETE(& m_parseAllocator[i]);
    }

}

void UfsServiceLogic::init(const std::string& confpath)
{
    std::call_once (winner_flag,[]{
            if (g_constFields.empty()) {
            //for (int i=0; i<TQUERY_OVERFLOW; ++i) {
            for (int i=0; i<TQUERY_OVERFLOW; ++i) {
            g_constFields.emplace_back(TQueryTable[i].c_str());
            }
            }
            });

    inv::INV_Config config;
    config.parseFile(confpath);

    m_takeIn[USER_INFO_TAG] = takeInTag_ = (config.get("/main/[take_in_tag]") == "true");
    m_takeIn[USER_INFO_CATEGORY] = takeInCategory_ = (config.get("/main/[take_in_category]") == "true");
    m_takeIn[USER_INFO_NEGATIVE] = takeInNegative_ = (config.get("/main/[take_in_negative]") == "true");
    m_takeIn[USER_INFO_TOPIC] = takeInTopic_ = (config.get("/main/[take_in_topic]") == "true");
    m_takeIn[USER_INFO_LAST_CLICK] = takeInLastClick_ = (config.get("/main/[take_in_last_click]") == "true");
    m_takeIn[USER_INFO_LAST_IMPRESSION] = takeInLastImpression_ = (config.get("/main/[take_in_last_impression]") == "true");
    m_takeIn[USER_INFO_USER_GMP] = takeInUserGmp_ = (config.get("/main/[take_in_user_gmp]") == "true");
    m_takeIn[USER_INFO_PROFILE] = takeInProfile_ = (config.get("/main/[take_in_profile]") == "true");
    m_takeIn[USER_INFO_BEHAVIOR] = takeInBehavior_ = (config.get("/main/[take_in_behavior]") == "true");
    m_takeIn[USER_INFO_CATEGORY_CLICK_AND_IMP] = takeInCategoryClickAndImp_ = (config.get("/main/[take_in_category_click_and_imp]") == "true");
    m_takeIn[USER_INFO_NEW_UID] = m_takeInNewUid = (config.get("/main/[take_in_new_uid]") == "true");

    for (int i=0; i<USER_INFO_OVERFLOW; ++i) {
        if (m_takeIn[i]) {
            ++m_userInfoSize;
        }
    }
    
    if (this->takeInCategoryClickAndImp_) {
        this->userCategoryClickAndImpHash_ = config.get("/main/[category_click_and_imp_hashname]");
        assert(!this->userCategoryClickAndImpHash_.empty());
    }

    if (m_takeInNewUid) {
        {
            std::string value_allocator = config.get("/main/[value_allocator]");
            std::vector<std::string> valueAllocator = inv::INV_Util::sepstr<std::string>(value_allocator, "|", true);

            if (valueAllocator.size() != TQUERY_OVERFLOW) {
                m_valueAllocator.push_back(new char[100*1024]);
                m_valueAllocator.push_back(new char[100*1024]);
                m_valueAllocator.push_back(new char[100*1024]);
                m_valueAllocator.push_back(new char[100*1024]);
                m_valueAllocator.push_back(new char[100*1024]);
                m_valueAllocator.push_back(new char[100*1024]);
                for (int i=0; i < TQUERY_OVERFLOW; ++i) {
                    m_valueAllocatorSize[i] = 100 * 1024;
                }
            } else {
                for (int i=0; i < TQUERY_OVERFLOW; ++i) {
                    int size = atoi(valueAllocator[i].c_str());
                    m_valueAllocator.push_back(new char[size]);
                    m_valueAllocatorSize[i] = size;
                }
            }
        }

        {
            std::string parse_allocator = config.get("/main/[parse_allocator]");
            std::vector<std::string> parseAllocator = inv::INV_Util::sepstr<std::string>(parse_allocator, "|", true);

            if (parseAllocator.size() != TQUERY_OVERFLOW) {
                m_parseAllocator.push_back(new char[100*1024]);
                m_parseAllocator.push_back(new char[100*1024]);
                m_parseAllocator.push_back(new char[100*1024]);
                m_parseAllocator.push_back(new char[100*1024]);
                m_parseAllocator.push_back(new char[100*1024]);
                m_parseAllocator.push_back(new char[100*1024]);

                for (int i=0; i < TQUERY_OVERFLOW; ++i) {
                    m_parseAllocatorSize[i] = 100 * 1024;
                }
            } else {
                for (int i=0; i < TQUERY_OVERFLOW; ++i) {
                    int size = atoi(parseAllocator[i].c_str());
                    m_parseAllocator.push_back(new char[size]);
                    m_parseAllocatorSize[i] = size;
                }
            }
        }
    }

    std::string getUserInfoCount = config.get("/main/[get_user_info_count]");
    if (getUserInfoCount.empty()) {
        m_getUserInfoCount = 300;
    } else {
        m_getUserInfoCount = atoi(getUserInfoCount.c_str());
    }

    if (this->takeInUserGmp_) {
        this->userGmpHash_ = config.get("/main/[user_gmp_hash]");
        assert(!this->userGmpHash_.empty());
    }

    if (this->takeInProfile_) {
        this->userProfileHash_ = config.get("/main/[user_profile_hash]");
        assert(!this->userProfileHash_.empty());
    }

    if (this->takeInBehavior_) {
        this->userBehaviorHash_ = config.get("/main/[user_behavior_hash]");
        assert(!this->userBehaviorHash_.empty());
    }

    this->categoryHash_ = config.get("/main/[category_hash]");
    if (this->categoryHash_.empty()) {
        fprintf(stderr, "/main/[category_hash] empty\n");
        exit(-1);
    }

    this->tagHash_ = config.get("/main/[tag_hash]");
    if (this->tagHash_.empty())
    {
        fprintf(stderr, "/main/[tag_hash] empty\n");
        exit(-1);
    }

    {
        this->negativeImpressionHash_ = config.get("/main/[negative_impression_hash]");
        if (this->negativeImpressionHash_.empty())
        {
            fprintf(stderr, "/main/[negative_impression_hash] empty\n");
            exit(-1);
        }

        this->negativeDislikeHash_ = config.get("/main/[negative_dislike_hash]");
        if (this->negativeDislikeHash_.empty())
        {
            fprintf(stderr, "/main/[negative_dislike_hash] empty\n");
            exit(-1);
        }
    }

    {
        this->ldaTopicHash_ = config.get("/main/[lda_topic_hash]");
        if (this->ldaTopicHash_.empty())
        {
            fprintf(stderr, "/main/[lda_topic_hash] empty\n");
            exit(-1);
        }
    }
    this->reformatClickHash_ = config.get("/main/[reformat_click_hash]");
    if (this->reformatClickHash_.empty())
    {
        fprintf(stderr, "/main/[reformat_click_hash] empty\n");
        exit(-1);
    }

    this->reformatImpressionHash_ = config.get("/main/[reformat_impression_hash]");
    if (this->reformatImpressionHash_.empty())
    {
        fprintf(stderr, "/main/[reformat_impression_hash] empty\n");
        exit(-1);
    }

    //assert(!this->userGmpHash_.empty());
    {
        std::string strSsdbConn = config.get("/main/[negative_ssdb_conn]");
        std::vector<std::string> fields = inv::INV_Util::sepstr<string>(strSsdbConn, ",", true);
        assert(!fields.empty());
        for (size_t i = 0; i < fields.size(); ++i)
        {
            std::vector<std::string> hpt = inv::INV_Util::sepstr<string>(fields[i], ":", true);
            assert(hpt.size() == 3);
            std::string host = hpt[0];
            int port = atoi(hpt[1].c_str());
            assert(port > 0);
            int timeout = atoi(hpt[2].c_str());
            assert(timeout > 0);
            // pcf->addrs.push_back({host, port, timeout});
            {
                inv::INV_Redis* redis = new inv::INV_Redis;
                redis->init(host, port, timeout);
                redis->connect();
                this->vecNegativeRedis_.push_back(redis);
            }
            {
                inv::INV_Redis* redis = new inv::INV_Redis;
                redis->init(host, port, timeout);
                redis->connect();
                this->vecUserGmpRedis_.push_back(redis);
            }
            if (this->takeInProfile_)
            {
                inv::INV_Redis* redis = new inv::INV_Redis;
                redis->init(host, port, timeout);
                redis->connect();
                this->vecProfileRedis_.push_back(redis);
            }
            if (this->takeInBehavior_)
            {
                inv::INV_Redis* redis = new inv::INV_Redis;
                redis->init(host, port, timeout);
                redis->connect();
                this->vecBehaviorRedis_.push_back(redis);
            }
        }
    }

    {
        std::string strSsdbConn = config.get("/main/[ssdb_conn]");
        std::vector<std::string> fields = inv::INV_Util::sepstr<string>(strSsdbConn, ",", true);
        assert(!fields.empty());
        size_t size = fields.size();
        for (size_t i = 0; i < size; ++i)
        {
            std::vector<std::string> hpt = inv::INV_Util::sepstr<string>(fields[i], ":", true);
            assert(hpt.size() == 3);
            std::string host = hpt[0];
            int port = atoi(hpt[1].c_str());
            assert(port > 0);
            int timeout = atoi(hpt[2].c_str());
            assert(timeout > 0);
            // pcf->addrs.push_back({host, port, timeout});
            {
                inv::INV_Redis* redis = new inv::INV_Redis;
                redis->init(host, port, timeout);
                redis->connect();
                this->vecTagRedis_.push_back(redis);

                this->vecRedis_.push_back(redis);
            }
            {
                inv::INV_Redis* redis = new inv::INV_Redis;
                redis->init(host, port, timeout);
                redis->connect();
                this->vecCategoryRedis_.push_back(redis);
            }
            {
                inv::INV_Redis* redis = new inv::INV_Redis;
                redis->init(host, port, timeout);
                redis->connect();
                this->vecLastClickRedis_.push_back(redis);
            }
            {
                inv::INV_Redis* redis = new inv::INV_Redis;
                redis->init(host, port, timeout);
                redis->connect();
                this->vecLastImpressionRedis_.push_back(redis);
            }
            if (this->takeInCategoryClickAndImp_)
            {
                 inv::INV_Redis* redis = new inv::INV_Redis;
                 redis->init(host, port, timeout);
                 redis->connect();
                 this->vecCategoryClickAndImpRedis_.push_back(redis);
            }
        }
    }

    {
        std::string strSsdbConn = config.get("/main/[topic_ssdb_conn]");
        std::vector<std::string> fields = inv::INV_Util::sepstr<string>(strSsdbConn, ",", true);
        assert(!fields.empty());
        size_t size = fields.size();
        for (size_t i = 0; i < size; ++i)
        {
            std::vector<std::string> hpt = inv::INV_Util::sepstr<string>(fields[i], ":", true);
            assert(hpt.size() == 3);
            std::string host = hpt[0];
            int port = atoi(hpt[1].c_str());
            assert(port > 0);
            int timeout = atoi(hpt[2].c_str());
            assert(timeout > 0);
            // pcf->addrs.push_back({host, port, timeout});
            inv::INV_Redis* redis = new inv::INV_Redis;
            redis->init(host, port, timeout);
            redis->connect();
            this->vecTopicRedis_.push_back(redis);
        }
    }

    {
        std::string strSsdbConn = config.get("/main/[new_uid_ssdb_conn]");
        std::vector<std::string> fields = inv::INV_Util::sepstr<string>(strSsdbConn, ",", true);
        assert(!fields.empty());
        size_t size = fields.size();
        for (size_t i = 0; i < size; ++i)
        {
            std::vector<std::string> hpt = inv::INV_Util::sepstr<string>(fields[i], ":", true);
            assert(hpt.size() == 3);
            std::string host = hpt[0];
            int port = atoi(hpt[1].c_str());
            assert(port > 0);
            int timeout = atoi(hpt[2].c_str());
            assert(timeout > 0);
            // pcf->addrs.push_back({host, port, timeout});
            inv::INV_Redis* redis = new inv::INV_Redis;
            redis->init(host, port, timeout);
            redis->connect();
            m_vecNewUidRedis.push_back(redis);
        }
    }

    std::string ddr = config.get("/main/[default_decay_rate]");
    this->defaultDecayRate_ = atof(ddr.c_str());
    if (this->defaultDecayRate_ <= 0)
    {
        fprintf(stderr, "/main/[default_decay_rate] empty\n");
        exit(-1);
    }
    
    {
        std::string dr = config.get("/main/[decay_rate]");
        std::vector<std::string> fields = inv::INV_Util::sepstr<string>(dr, ",", true);
        for (size_t i = 0; i < fields.size(); ++i)
        {
            std::vector<std::string> hpt = inv::INV_Util::sepstr<string>(fields[i], ":", true);
            assert(hpt.size() == 2);
            double rate = atof(hpt[1].c_str());
            assert(rate > 0);
            this->decayRate_.insert(std::make_pair(hpt[0], rate));
        }
    }

    std::string dp = config.get("/main/[decay_period]");
    this->decayPeriod_ = atoi(dp.c_str());
    if (this->decayPeriod_ <= 0)
    {
        fprintf(stderr, "/main/[decay_period] empty\n");
        exit(-1);
    }

    this->nodeName_ = config.get("/main/[node_name]");

    this->timeFeatureHash_ = config.get("/main/[time_feature_hash]");
    assert(!this->timeFeatureHash_.empty());
    this->networkFeatureHash_ = config.get("/main/[network_feature_hash]");
    assert(!this->networkFeatureHash_.empty());

    this->ufsStatsTag_ = config.get("/main/[stats_tag_hash]");
    assert(!this->ufsStatsTag_.empty());
    this->ufsStatsCategory_ = config.get("/main/[stats_category_hash]");
    assert(!this->ufsStatsCategory_.empty());

    this->newUserHash_ = config.get("/main/[new_user_hash]");
    assert(!this->newUserHash_.empty());

    this->interestHash_ = config.get("/main/[interest_hash]");
    assert(!this->interestHash_.empty());

    this->local_ip_ = config.get("/main/[log_ip]");
    if (this->local_ip_.empty())
    {
        fprintf(stderr, "/main/[log_ip] empty\n");
        exit(-1);
    }

    this->logic_ = new SfuLogic(this->decayPeriod_, this->defaultDecayRate_, this->decayRate_, this->vecRedis_,
            this->categoryHash_, this->tagHash_, this->ufsStatsCategory_, this->ufsStatsTag_);
    return;
}

void UfsServiceLogic::GetWeightedJson(std::string& _return, const std::string& type, const std::string& uid, const std::string& version)
{
    // rapidjson::Document d;
    this->logic_->GetRpcJson(_return, type, uid, version);
    return;
}

void UfsServiceLogic::GetWeightedCategories(std::string& _return, const std::string& uid, const std::string& version)
{
    TIME_LABEL(1); 
    int64_t start = INV_Util::now2ms();
    const std::string type = "category";
    this->GetWeightedJson(_return, type, uid, version);
    TLOGINFO(__FILE__<<"-"<<__LINE__<<" used time: " << (INV_Util::now2ms() - start) << " return size: " << _return.size() <<endl);
    ReportCall("ufs.thrift.category", "", this->nodeName_, (!_return.empty()) ? inv::monitor::CS_SUCC:inv::monitor::CS_FAILED, TIME_DIFF(1));
}

void UfsServiceLogic::GetWeightedTags(std::string& _return, const std::string& uid, const std::string& version)
{
    TIME_LABEL(1);
    int64_t start = INV_Util::now2ms();
    const std::string type = "tag";
    this->GetWeightedJson(_return, type, uid, version);
    TLOGINFO(__FILE__<<"-"<<__LINE__<<" used time: " << (INV_Util::now2ms() - start) << " return size: " << _return.size() <<endl);
    ReportCall("ufs.thrift.tag", "", this->nodeName_, (!_return.empty()) ? inv::monitor::CS_SUCC:inv::monitor::CS_FAILED, TIME_DIFF(1));
}

void UfsServiceLogic::GetLdaTopic(std::string& _return, const std::string& uid, const std::string& version)
{
    TIME_LABEL(1);
    int64_t start = INV_Util::now2ms();
    std::string hashName = this->ldaTopicHash_ + version;
    
    // size_t idx = GetIdxOfConns(uid, this->vecTopicRedis_.size(), "");
    // TLOGINFO(__FILE__<<"-"<<__LINE__<<" topic redis index: " << idx <<endl);
    // inv::INV_Redis* redis = this->vecTopicRedis_[idx];

    inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecTopicRedis_, uid, "");
    RedisHashGet(_return, redis, hashName, uid);

    TLOGINFO(__FILE__<<"-"<<__LINE__<<" used time: " << (INV_Util::now2ms() - start) << " return size: " << _return.size() <<endl);
    ReportCall("ufs.thrift.topic", "", this->nodeName_, (!_return.empty()) ? inv::monitor::CS_SUCC:inv::monitor::CS_FAILED, TIME_DIFF(1));
}

int32_t UfsServiceLogic::GetNegativeJson(std::string& json, const std::string& uid, const std::string& version, const std::string& type)
{
    std::string hashName;
    if (type == "impression")
    {
        hashName = this->negativeImpressionHash_ + version;
    }
    else if (type == "dislike")
    {
        hashName = this->negativeDislikeHash_ + version;
    }
    else
    {
        assert(0);
    }
    
    // size_t idx = GetIdxOfConns(uid, this->vecNegativeRedis_.size(), "");
    // TLOGINFO(__FILE__<<"-"<<__LINE__<<" negative redis index: " << idx <<endl);
    // inv::INV_Redis* redis = this->vecNegativeRedis_[idx];

    inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecNegativeRedis_, uid, "");
    json.clear();
    int ret = RedisHashGet(json, redis, hashName, uid);
    return ret;
}

void UfsServiceLogic::GetImpressionTitleTags(std::string& _return, const std::string& uid, const std::string& version)
{
    TIME_LABEL(1);
    int64_t start = INV_Util::now2ms();
    const std::string type = "impression";
    this->GetNegativeJson(_return, uid, version, type);
    TLOGINFO(__FILE__<<"-"<<__LINE__<<" used time: " << (INV_Util::now2ms() - start) << " return size: " << _return.size() <<endl);
    ReportCall("ufs.thrift.negative.impression", "", this->nodeName_, (!_return.empty()) ? inv::monitor::CS_SUCC:inv::monitor::CS_FAILED, TIME_DIFF(1));
}

void UfsServiceLogic::GetDislikeTitleTags(std::string& _return, const std::string& uid, const std::string& version)
{
    TIME_LABEL(1);
    int64_t start = INV_Util::now2ms();
    const std::string type = "dislike";
    this->GetNegativeJson(_return, uid, version, type);
    TLOGINFO(__FILE__<<"-"<<__LINE__<<" used time: " << (INV_Util::now2ms() - start) << " return size: " << _return.size() <<endl);
    ReportCall("ufs.thrift.negative.dislike", "", this->nodeName_, (!_return.empty()) ? inv::monitor::CS_SUCC:inv::monitor::CS_FAILED, TIME_DIFF(1));
}

void UfsServiceLogic::GetLastActionTimeOfCategory(std::string& _return, const std::string& uid, const ActionType::type type)
{
    TIME_LABEL(1);
    int64_t start = INV_Util::now2ms();

    std::string hash;
    if (type == ActionType::TYPE_CLICK)
    {
        hash = this->reformatClickHash_;
    }
    else if (type == ActionType::TYPE_IMPRESSION)
    {
        hash = this->reformatImpressionHash_;
    }
    else
    {
        return;
        // assert(0);
    }

    // size_t idx = GetIdxOfConns(uid, this->vecRedis_.size(), "");
    // inv::INV_Redis* redis = this->vecRedis_[idx];
    inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecRedis_, uid, "");

    _return.clear();
    RedisHashGet(_return, redis, hash, uid);
    TLOGINFO(__FILE__<<"-"<<__LINE__<<" used time: " << (INV_Util::now2ms() - start) << " return size: " << _return.size() <<endl);
    ReportCall("ufs.thrift.reformat", "", this->nodeName_, (!_return.empty()) ? inv::monitor::CS_SUCC:inv::monitor::CS_FAILED, TIME_DIFF(1));
}

void UfsServiceLogic::HdfsFeature(std::string& _return, const std::string& uid, const std::string& hash)
{
    return;
    // size_t idx = GetIdxOfConns(uid, this->vecRedis_.size(), "");
    // inv::INV_Redis* redis = this->vecRedis_[idx];
    // inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecRedis_, uid, "");

    // _return.clear();
    // RedisHashGet(_return, redis, hash, uid);

    // if (_return.empty())
    // {
    //     return;
    // }

    // rapidjson::Document d;
    // d.Parse(_return.c_str());

    // if (d.HasParseError())
    // {
    //     _return.clear();
    //     return;
    // }

    // const auto& its = d.FindMember("ts");
    // if (its == d.MemberEnd())
    // {
    //     _return.clear();
    //     return;
    // }

    // if (time(NULL) - its->value.GetInt() > 24 * 60 * 60)
    // {
    //     _return.clear();
    //     return;
    // }
}

void UfsServiceLogic::GetTimeFeature(std::string& _return, const std::string& uid)
{
    return;
    // TIME_LABEL(1);
    // int64_t start = INV_Util::now2ms();
    // this->HdfsFeature(_return, uid, this->timeFeatureHash_);

    // TLOGINFO(__FILE__<<"-"<<__LINE__<<" used time: " << (INV_Util::now2ms() - start) << " return size: " << _return.size() <<endl);
    // ReportCall("ufs.thrift.feature.time", "", this->nodeName_, (!_return.empty()) ? inv::monitor::CS_SUCC:inv::monitor::CS_FAILED, TIME_DIFF(1));
}

void UfsServiceLogic::GetNetworkFeature(std::string& _return, const std::string& uid)
{
    return;
    // TIME_LABEL(1);
    // int64_t start = INV_Util::now2ms();
    // this->HdfsFeature(_return, uid, this->networkFeatureHash_);

    // TLOGINFO(__FILE__<<"-"<<__LINE__<<" used time: " << (INV_Util::now2ms() - start) << " return size: " << _return.size() <<endl);
    // ReportCall("ufs.thrift.feature.network", "", this->nodeName_, (!_return.empty()) ? inv::monitor::CS_SUCC:inv::monitor::CS_FAILED, TIME_DIFF(1));
}

void UfsServiceLogic::GetUfsStats(std::string& _return, const std::string& uid, const std::string& hash)
{
    // size_t idx = GetIdxOfConns(uid, this->vecRedis_.size(), "");
    // inv::INV_Redis* redis = this->vecRedis_[idx];
    inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecRedis_, uid, "");

    _return.clear();
    RedisHashGet(_return, redis, hash, uid);

    if (_return.empty())
    {
        return;
    }

    rapidjson::Document d;
    d.Parse(_return.c_str());

    if (d.HasParseError())
    {
        _return.clear();
        return;
    }
}

void UfsServiceLogic::GetTagStats(std::string& _return, const std::string& uid)
{
    TIME_LABEL(1);
    int64_t start = INV_Util::now2ms();
    this->GetUfsStats(_return, uid, this->ufsStatsTag_);

    TLOGINFO(__FILE__<<"-"<<__LINE__<<" used time: " << (INV_Util::now2ms() - start) << " return size: " << _return.size() <<endl);
    ReportCall("ufs.thrift.stats.tag", "", this->nodeName_, (!_return.empty()) ? inv::monitor::CS_SUCC:inv::monitor::CS_FAILED, TIME_DIFF(1));
}

void UfsServiceLogic::GetCategoryStats(std::string& _return, const std::string& uid)
{
    TIME_LABEL(1);
    int64_t start = INV_Util::now2ms();
    this->GetUfsStats(_return, uid, this->ufsStatsCategory_);

    TLOGINFO(__FILE__<<"-"<<__LINE__<<" used time: " << (INV_Util::now2ms() - start) << " return size: " << _return.size() <<endl);
    ReportCall("ufs.thrift.stats.category", "", this->nodeName_, (!_return.empty()) ? inv::monitor::CS_SUCC:inv::monitor::CS_FAILED, TIME_DIFF(1));
}

void UfsServiceLogic::GetCreateTime(std::string& _return, const std::string& uid)
{
    TIME_LABEL(1);
    int64_t start = INV_Util::now2ms();
    // size_t idx = GetIdxOfConns(uid, this->vecRedis_.size(), "");
    // inv::INV_Redis* redis = this->vecRedis_[idx];
    inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecRedis_, uid, "");

    _return.clear();
    RedisHashGet(_return, redis, this->newUserHash_, uid);
    TLOGINFO(__FILE__<<"-"<<__LINE__<<" used time: " << (INV_Util::now2ms() - start) << " return size: " << _return.size() <<endl);
    ReportCall("ufs.thrift.newuser", "", this->nodeName_, (!_return.empty()) ? inv::monitor::CS_SUCC:inv::monitor::CS_FAILED, TIME_DIFF(1));
}

void UfsServiceLogic::GetInterest(std::string& _return, const std::string& uid)
{
    TIME_LABEL(1);
    int64_t start = INV_Util::now2ms();
    // size_t idx = GetIdxOfConns(uid, this->vecRedis_.size(), "");
    // inv::INV_Redis* redis = this->vecRedis_[idx];
    inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecRedis_, uid, "");

    _return.clear();
    RedisHashGet(_return, redis, this->interestHash_, uid);
    TLOGINFO(__FILE__<<"-"<<__LINE__<<" used time: " << (INV_Util::now2ms() - start) << " return size: " << _return.size() <<endl);
    ReportCall("ufs.thrift.interest.get", "", this->nodeName_, (!_return.empty()) ? inv::monitor::CS_SUCC:inv::monitor::CS_FAILED, TIME_DIFF(1));
}

int32_t UfsServiceLogic::SetInterest(const std::string& uid, const std::string& jsonArray)
{
    return -1;
    TIME_LABEL(1);
    int64_t start = INV_Util::now2ms();
    // size_t idx = GetIdxOfConns(uid, this->vecRedis_.size(), "");
    // inv::INV_Redis* redis = this->vecRedis_[idx];
    inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecRedis_, uid, "");
    rapidjson::Document d;
    d.Parse(jsonArray.c_str());

    if (d.HasParseError())
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" error json array: " << jsonArray <<endl);
        return -1;
    }

    int ret = RedisHashSet(redis, this->interestHash_, uid, jsonArray);
    TLOGINFO(__FILE__<<"-"<<__LINE__<<" used time: " << (INV_Util::now2ms() - start) <<endl);
    ReportCall("ufs.thrift.interest.set", "", this->nodeName_, (ret == 0) ? inv::monitor::CS_SUCC:inv::monitor::CS_FAILED, TIME_DIFF(1));
    return ret;
}

UfsServiceLogic::SsdbIoRes UfsServiceLogic::SsdbWrapper(inv::INV_Redis* redis, const std::string& hash, const std::string& uid, const std::string& local_ip)
{
    int64_t start = INV_Util::now2ms();
    UfsServiceLogic::SsdbIoRes res;
    res.retCode = RedisHashMGet(res.data, redis, hash, {uid});
    FDLOG("monitor") << "|" << local_ip << "&&" << "ufs.getuserinfo.ssdb"
        << "&&" << (INV_Util::now2ms() - start)  << "&&" << 0 << "&&" << "AVG" << "&&" << 60 << "&&" << "" << endl;

    return res;
}

void UfsServiceLogic::MergeJsonOfClickAndImp(int type, const std::string& clickStr, const std::string& impStr, std::string& out)
{
	//rapidjson::Document click;
    rapidjson::MemoryPoolAllocator<> valueAllocator(m_valueAllocator[type], m_valueAllocatorSize[type]);
    rapidjson::MemoryPoolAllocator<> parseAllocator(m_parseAllocator[type], m_parseAllocatorSize[type]);
    DocumentType click(&valueAllocator, m_parseAllocatorSize[type], &parseAllocator);
	click.Parse(clickStr.c_str());
	if (click.HasParseError() || !click.IsObject()) {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" json parse fail: "<< click.GetErrorOffset()
                << ": " << (rapidjson::GetParseError_En(click.GetParseError())) << endl);

		return;
	}

	if (impStr.empty()) {
		rapidjson::Value* objClick = GetValueByPointer(click, "/data");
		if (objClick && objClick->IsArray()) {
			size_t sizeClick = objClick->GetArray().Size();
			for (size_t i=0; i<sizeClick; ++i) {
				//rapidjson::Value* keyOfClick = GetValueByPointer(click, VideoJsonFieldTableStr[i][0]);
				rapidjson::Value* clickOfClick = GetValueByPointer(click, VideoJsonFieldTableStr[i][0]);
				rapidjson::Value* impOfClick = GetValueByPointer(click, VideoJsonFieldTableStr[i][2]);

                if (clickOfClick) {
                    if (impOfClick == nullptr) {
                        SetValueByPointer(click, VideoJsonFieldTableStr[i][2], 0.0);
                    } else {
                        break;
                    }
                } else {
					SetValueByPointer(click, VideoJsonFieldTableStr[i][1], 0.0);
                }
			}
		}
	} else {
		//rapidjson::Document imp;
        rapidjson::MemoryPoolAllocator<> valueAllocator(m_valueAllocator[type+3], m_valueAllocatorSize[type+3]);
        rapidjson::MemoryPoolAllocator<> parseAllocator(m_parseAllocator[type+3], m_parseAllocatorSize[type+3]);
        DocumentType imp(&valueAllocator, m_parseAllocatorSize[type+3], &parseAllocator);
		imp.Parse(impStr.c_str());
		if (imp.HasParseError() || !imp.IsObject()) {
            TLOGINFO(__FILE__<<"-"<<__LINE__<<" json parse fail: "<< imp.GetErrorOffset()
                    << ": " << (rapidjson::GetParseError_En(imp.GetParseError())) << endl);

			return;
		}

		rapidjson::Value* objClick = GetValueByPointer(click, "/data");
		rapidjson::Value* objImp = GetValueByPointer(imp, "/data");
		if (objClick && objClick->IsArray() &&
				objImp && objImp->IsArray()) {
			size_t sizeClick = objClick->GetArray().Size();
			size_t sizeImp = objImp->GetArray().Size();
			for (size_t i=0; i<sizeClick; ++i) {
				rapidjson::Value* keyOfClick = GetValueByPointer(click, VideoJsonFieldTableStr[i][0]);
				rapidjson::Value* impOfClick = GetValueByPointer(click, VideoJsonFieldTableStr[i][2]);

				if (impOfClick) {
					for (size_t j=0; j<sizeImp; ++j) {
						rapidjson::Value* keyOfImp = GetValueByPointer(imp, VideoJsonFieldTableStr[j][0]);
						if (strcmp(keyOfClick->GetString(), keyOfImp->GetString()) == 0) {
							rapidjson::Value* impOfImp = GetValueByPointer(imp, VideoJsonFieldTableStr[j][2]);
                            if (impOfImp) {
                                SetValueByPointer(click, VideoJsonFieldTableStr[i][2], impOfImp->GetDouble());
                            } else {
                                SetValueByPointer(click, VideoJsonFieldTableStr[i][2], 0.0);
                            }

							break;
						}
					}
				} else {
					bool flag = true;
					for (size_t j=0; j<sizeImp; ++j) {
						rapidjson::Value* keyOfImp = GetValueByPointer(imp, VideoJsonFieldTableStr[j][0]);
						if (strcmp(keyOfClick->GetString(), keyOfImp->GetString()) == 0) {
							rapidjson::Value* impOfImp = GetValueByPointer(imp, VideoJsonFieldTableStr[j][2]);
                            if (impOfImp) {
                                SetValueByPointer(click, VideoJsonFieldTableStr[i][2], impOfImp->GetDouble());
                            } else {
                                SetValueByPointer(click, VideoJsonFieldTableStr[i][2], 0.0);
                            }
							flag = false;

							break;
						}
					}

					if (flag) {
						SetValueByPointer(click, VideoJsonFieldTableStr[i][2], 0.0);
					}
				}
			}
		}

	}

	rapidjson::StringBuffer sb;
	rapidjson::Writer<rapidjson::StringBuffer> writer(sb);
	click.Accept(writer);

	out = sb.GetString();
}

std::string UfsServiceLogic::MergeJsonOfClickAndImp(int type, const std::string* clickStr, const std::string* impStr)
{
	//rapidjson::Document click;
    rapidjson::MemoryPoolAllocator<> valueAllocator(m_valueAllocator[type], m_valueAllocatorSize[type]);
    rapidjson::MemoryPoolAllocator<> parseAllocator(m_parseAllocator[type], m_parseAllocatorSize[type]);
    DocumentType click(&valueAllocator, m_parseAllocatorSize[type], &parseAllocator);
	click.Parse(clickStr->c_str());
	if (click.HasParseError() || !click.IsObject()) {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" json parse fail: "<< click.GetErrorOffset()
                << ": " << (rapidjson::GetParseError_En(click.GetParseError())) << endl);

		return "";
	}

	if (impStr == nullptr || impStr->empty()) {
		rapidjson::Value* objClick = GetValueByPointer(click, "/data");
		if (objClick && objClick->IsArray()) {
			size_t sizeClick = objClick->GetArray().Size();
			for (size_t i=0; i<sizeClick; ++i) {
				//rapidjson::Value* keyOfClick = GetValueByPointer(click, VideoJsonFieldTableStr[i][0]);
				rapidjson::Value* clickOfClick = GetValueByPointer(click, VideoJsonFieldTableStr[i][0]);
				rapidjson::Value* impOfClick = GetValueByPointer(click, VideoJsonFieldTableStr[i][2]);

                if (clickOfClick) {
                    if (impOfClick == nullptr) {
                        SetValueByPointer(click, VideoJsonFieldTableStr[i][2], 0.0);
                    } else {
                        break;
                    }
                } else {
					SetValueByPointer(click, VideoJsonFieldTableStr[i][1], 0.0);
                }
			}
		}
	} else {
		//rapidjson::Document imp;
        rapidjson::MemoryPoolAllocator<> valueAllocator(m_valueAllocator[type+3], m_valueAllocatorSize[type+3]);
        rapidjson::MemoryPoolAllocator<> parseAllocator(m_parseAllocator[type+3], m_parseAllocatorSize[type+3]);
        DocumentType imp(&valueAllocator, m_parseAllocatorSize[type+3], &parseAllocator);
		imp.Parse(impStr->c_str());
		if (imp.HasParseError() || !imp.IsObject()) {
            TLOGINFO(__FILE__<<"-"<<__LINE__<<" json parse fail: "<< imp.GetErrorOffset()
                    << ": " << (rapidjson::GetParseError_En(imp.GetParseError())) << endl);

			return "";
		}

		rapidjson::Value* objClick = GetValueByPointer(click, "/data");
		rapidjson::Value* objImp = GetValueByPointer(imp, "/data");
		if (objClick && objClick->IsArray() &&
				objImp && objImp->IsArray()) {
			size_t sizeClick = objClick->GetArray().Size();
			size_t sizeImp = objImp->GetArray().Size();
			for (size_t i=0; i<sizeClick; ++i) {
				rapidjson::Value* keyOfClick = GetValueByPointer(click, VideoJsonFieldTableStr[i][0]);
				rapidjson::Value* impOfClick = GetValueByPointer(click, VideoJsonFieldTableStr[i][2]);

				if (impOfClick) {
					for (size_t j=0; j<sizeImp; ++j) {
						rapidjson::Value* keyOfImp = GetValueByPointer(imp, VideoJsonFieldTableStr[j][0]);
						if (strcmp(keyOfClick->GetString(), keyOfImp->GetString()) == 0) {
							rapidjson::Value* impOfImp = GetValueByPointer(imp, VideoJsonFieldTableStr[j][2]);
                            if (impOfImp) {
                                SetValueByPointer(click, VideoJsonFieldTableStr[i][2], impOfImp->GetDouble());
                            } else {
                                SetValueByPointer(click, VideoJsonFieldTableStr[i][2], 0.0);
                            }

							break;
						}
					}
				} else {
					bool flag = true;
					for (size_t j=0; j<sizeImp; ++j) {
						rapidjson::Value* keyOfImp = GetValueByPointer(imp, VideoJsonFieldTableStr[j][0]);
						if (strcmp(keyOfClick->GetString(), keyOfImp->GetString()) == 0) {
							rapidjson::Value* impOfImp = GetValueByPointer(imp, VideoJsonFieldTableStr[j][2]);
                            if (impOfImp) {
                                SetValueByPointer(click, VideoJsonFieldTableStr[i][2], impOfImp->GetDouble());
                            } else {
                                SetValueByPointer(click, VideoJsonFieldTableStr[i][2], 0.0);
                            }
							flag = false;

							break;
						}
					}

					if (flag) {
						SetValueByPointer(click, VideoJsonFieldTableStr[i][2], 0.0);
					}
				}
			}
		}

	}

	rapidjson::StringBuffer sb;
	rapidjson::Writer<rapidjson::StringBuffer> writer(sb);
	click.Accept(writer);

	return sb.GetString();
}

void UfsServiceLogic::SetUserInfo(int type, UserInfo& _return, const UfsServiceLogic::SsdbIoRes& res) {
    do {
        if (type == USER_INFO_TAG) {
            _return.__set_weightedTags(res.data[0]);

            break;
        }

        if (type == USER_INFO_CATEGORY) {
            _return.__set_weightedCategories(res.data[0]);

            break;
        }

        if (type == USER_INFO_NEGATIVE) {
            _return.__set_impressionTitleTags(res.data[0]);

            break;
        }

        if (type == USER_INFO_TOPIC) {
            _return.__set_ldaTopic(res.data[0]);

            break;
        }

        if (type == USER_INFO_LAST_CLICK) {
            _return.__set_lastClickTimeOfCategory(res.data[0]);

            break;
        }

        if (type == USER_INFO_LAST_IMPRESSION) {
            _return.__set_lastImpressionTimeOfCategory(res.data[0]);

            break;
        }
        
        if (type == USER_INFO_USER_GMP) {
            _return.__set_userGmp(res.data[0]);

            break;
        }

        if (type == USER_INFO_PROFILE) {
            _return.__set_userprofile(res.data[0]);

            break;
        }

        if (type == USER_INFO_BEHAVIOR) {
            //_return.__set_userprofile(res.data);

            break;
        }

        if (type == USER_INFO_CATEGORY_CLICK_AND_IMP) {
            _return.__set_categoryClickAndImp(res.data[0]);

            break;
        }

        if (type == USER_INFO_NEW_UID) {
#if 0
            if (res.data.size() == 3) {
                _return.__set_publisher(std::move(res.data[0]));
                _return.__set_thridpartyKeyword(std::move(res.data[1]));
                _return.__set_thridpartyTopic(std::move(res.data[2]));
            }
#else
            int size = res.data.size();
            if (size == TQUERY_OVERFLOW) {
                std::string out[TQUERY_FIELDS_SIZE];
                for (int i=0; i < TQUERY_FIELDS_SIZE; ++i) {
                    MergeJsonOfClickAndImp(i, res.data[i], res.data[i+3], out[i]);
                }

                _return.__set_publisher(std::move(out[0]));
                _return.__set_thridpartyKeyword(std::move(out[1]));
                _return.__set_thridpartyTopic(std::move(out[2]));
            } else if (size == TQUERY_FIELDS_SIZE) { /* old style */
                std::string out[TQUERY_FIELDS_SIZE];
                for (int i=0; i < size; ++i) {
                    MergeJsonOfClickAndImp(i, res.data[i], "", out[i]);
                }

                _return.__set_publisher(std::move(out[0]));
                _return.__set_thridpartyKeyword(std::move(out[1]));
                _return.__set_thridpartyTopic(std::move(out[2]));
            } else {
                TLOGINFO(__FILE__<<"-"<<__LINE__<<" type="<< UserInfoTableStr[type].c_str()<<"|size = "<<size <<std::endl);
            }
#endif

            break;
        }
    } while (false);
}

void UfsServiceLogic::SetUserInfo(int type, UserInfo& _return, UfsServiceLogic::SsdbIoRes&& res) {
    do {
        if (type == USER_INFO_TAG) {
            _return.__set_weightedTags(std::move(res.data[0]));

            break;
        }

        if (type == USER_INFO_CATEGORY) {
            _return.__set_weightedCategories(std::move(res.data[0]));

            break;
        }

        if (type == USER_INFO_NEGATIVE) {
            _return.__set_impressionTitleTags(std::move(res.data[0]));

            break;
        }

        if (type == USER_INFO_TOPIC) {
            _return.__set_ldaTopic(std::move(res.data[0]));

            break;
        }

        if (type == USER_INFO_LAST_CLICK) {
            _return.__set_lastClickTimeOfCategory(std::move(res.data[0]));

            break;
        }

        if (type == USER_INFO_LAST_IMPRESSION) {
            _return.__set_lastImpressionTimeOfCategory(std::move(res.data[0]));

            break;
        }
        
        if (type == USER_INFO_USER_GMP) {
            _return.__set_userGmp(std::move(res.data[0]));

            break;
        }

        if (type == USER_INFO_PROFILE) {
            _return.__set_userprofile(std::move(res.data[0]));

            break;
        }

        if (type == USER_INFO_BEHAVIOR) {
            //_return.__set_userprofile(std::move(res.data));

            break;
        }

        if (type == USER_INFO_CATEGORY_CLICK_AND_IMP) {
            _return.__set_categoryClickAndImp(std::move(res.data[0]));

            break;
        }

        if (type == USER_INFO_NEW_UID) {
#if 0
            if (res.data.size() == 3) {
                _return.__set_publisher(std::move(res.data[0]));
                _return.__set_thridpartyKeyword(std::move(res.data[1]));
                _return.__set_thridpartyTopic(std::move(res.data[2]));
            }
#else
            int size = res.data.size();
            if (size == TQUERY_OVERFLOW) {
                std::string out[TQUERY_FIELDS_SIZE];
                for (int i=0; i < TQUERY_FIELDS_SIZE; ++i) {
                    MergeJsonOfClickAndImp(i, res.data[i], res.data[i+3], out[i]);
                }

                _return.__set_publisher(std::move(out[0]));
                _return.__set_thridpartyKeyword(std::move(out[1]));
                _return.__set_thridpartyTopic(std::move(out[2]));
            } else if (size == TQUERY_FIELDS_SIZE) { /* old style */
                std::string out[TQUERY_FIELDS_SIZE];
                for (int i=0; i < size; ++i) {
                    MergeJsonOfClickAndImp(i, res.data[i], "", out[i]);
                }

                _return.__set_publisher(std::move(out[0]));
                _return.__set_thridpartyKeyword(std::move(out[1]));
                _return.__set_thridpartyTopic(std::move(out[2]));
            }
#endif

            break;
        }
    } while (false);
}

void UfsServiceLogic::GetUserInfo(UserInfo& _return, const std::string& uid, const std::string& catVersion,
        const std::string& tagsVersion, const std::string& titleTagsVersion,
        const std::string& ldaTopicVersion)
{
    int64_t start = INV_Util::now2ms();
    TIME_LABEL(1);
    TLOGINFO(__FILE__<<"-"<<__LINE__<< " uid: " << uid << " catVersion: " << catVersion
            << " tagsVersion: " << tagsVersion << " titleTagsVersion: " << titleTagsVersion
            << " ldaTopicVersion: " << ldaTopicVersion << endl);
    ReportIncr("zhizi.allinone.getuserinfo.all");
    FDLOG("monitor") << "|" << this->local_ip_ << "&&" << "ufs.getuserinfo.request_count"
            << "&&" << 1 << "&&" << 0 << "&&" << "ORIGINAL" << "&&" << 60 << "&&" << "" << endl;

#if 0
    std::map<std::string, std::future<UfsServiceLogic::SsdbIoRes>> tasks;
    if (this->takeInTag_ && !tagsVersion.empty())
    {
        /* tagHash_=zhizi.ufs.tag. */
        std::string hash = this->tagHash_ + tagsVersion;
        inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecTagRedis_, uid, "");
        auto fut = std::async(std::launch::async, UfsServiceLogic::SsdbWrapper, redis, hash, uid, this->local_ip_);
        tasks.insert(std::move(std::make_pair("tag", std::move(fut))));
    }
    if (this->takeInCategory_ && !catVersion.empty())
    {
        /*  zhizi.ufs.category. */
        std::string hash = this->categoryHash_ + catVersion;
        inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecCategoryRedis_, uid, "");
        auto fut = std::async(std::launch::async, UfsServiceLogic::SsdbWrapper, redis, hash, uid, this->local_ip_);
        tasks.insert(std::move(std::make_pair("category", std::move(fut))));
    }
    if (this->takeInNegative_ && !titleTagsVersion.empty())
    {
        /* not_implement_yet */
        std::string hash = this->negativeImpressionHash_ + titleTagsVersion;
        inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecNegativeRedis_, uid, "");
        auto fut = std::async(std::launch::async, UfsServiceLogic::SsdbWrapper, redis, hash, uid, this->local_ip_);
        tasks.insert(std::move(std::make_pair("negative", std::move(fut))));
    }
    if (this->takeInTopic_ && !ldaTopicVersion.empty())
    {
        /* zhizi.topic.lda. */
        std::string hash = this->ldaTopicHash_ + ldaTopicVersion;
        inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecTopicRedis_, uid, "");
        auto fut = std::async(std::launch::async, UfsServiceLogic::SsdbWrapper, redis, hash, uid, this->local_ip_);
        tasks.insert(std::move(std::make_pair("topic", std::move(fut))));
    }
    if (this->takeInLastClick_) /* false */
    {
        /* zhizi.ufs.reformat.click */
        std::string hash = this->reformatClickHash_;
        inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecLastClickRedis_, uid, "");
        auto fut = std::async(std::launch::async, UfsServiceLogic::SsdbWrapper, redis, hash, uid, this->local_ip_);
        tasks.insert(std::move(std::make_pair("lastClick", std::move(fut))));
    }
    if (this->takeInLastImpression_) /* false */
    {
        /* zhizi.ufs.reformat.impression */
        std::string hash = this->reformatImpressionHash_;
        inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecLastImpressionRedis_, uid, "");
        auto fut = std::async(std::launch::async, UfsServiceLogic::SsdbWrapper, redis, hash, uid, this->local_ip_);
        tasks.insert(std::move(std::make_pair("lastImpression", std::move(fut))));
    }
    if (this->takeInUserGmp_)
    {
        /* zhizi.user.gmp  */
        std::string hash = this->userGmpHash_;
        inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecUserGmpRedis_, uid, "");
        auto fut = std::async(std::launch::async, UfsServiceLogic::SsdbWrapper, redis, hash, uid, this->local_ip_);
        tasks.insert(std::move(std::make_pair("userGmp", std::move(fut))));
        UfsServiceLogic::SsdbIoRes res(it.second.get());
        if (res.retCode != 0)
        {
            continue;
        }
        TLOGINFO(__FILE__<<"-"<<__LINE__<< " " << it.first << " size: " << res.data.size() << endl);
        if (it.first == "tag")
        {
            _return.__set_weightedTags(res.data);
        }
        else if (it.first == "category")
        {
            _return.__set_weightedCategories(res.data);
        }
        else if (it.first == "negative")
        {
            _return.__set_impressionTitleTags(res.data);
        }
        else if (it.first == "topic")
        {
            _return.__set_ldaTopic(res.data);
        }
        else if (it.first == "lastClick")
        {
            _return.__set_lastClickTimeOfCategory(res.data);
        }
        else if (it.first == "lastImpression")
        {
            _return.__set_lastImpressionTimeOfCategory(res.data);
        }
        else if (it.first == "userGmp")
        {
            _return.__set_userGmp(res.data);
        }
        else if (it.first == "behavior")
        {
            _return.__set_userprofile(res.data);
        }
        else if (it.first == "categoryClickAndImp")
        {
            _return.__set_categoryClickAndImp(res.data);
        }
    }
    if (this->takeInProfile_) /* false */
    {
        std::string hash = this->userProfileHash_;
        inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecProfileRedis_, uid, "");
        auto fut = std::async(std::launch::async, UfsServiceLogic::SsdbWrapper, redis, hash, uid, this->local_ip_);
        tasks.insert(std::move(std::make_pair("profile", std::move(fut))));
    }
    if (this->takeInBehavior_) /* false */
    {
        std::string hash = this->userBehaviorHash_;
        inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecBehaviorRedis_, uid, "");
        auto fut = std::async(std::launch::async, UfsServiceLogic::SsdbWrapper, redis, hash, uid, this->local_ip_);
        tasks.insert(std::move(std::make_pair("behavior", std::move(fut))));
    }
    if (this->takeInCategoryClickAndImp_)
    {
        /* zhizi.ufs.category.clickandimp. */
        std::string hash = this->userCategoryClickAndImpHash_ + "v28";//目前分类都是用v28
        inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecCategoryClickAndImpRedis_, uid, "");
        auto fut = std::async(std::launch::async, UfsServiceLogic::SsdbWrapper, redis, hash, uid, this->local_ip_);
        tasks.insert(std::move(std::make_pair("categoryClickAndImp", std::move(fut))));
    }

    for (auto& it: tasks)
    {
        UfsServiceLogic::SsdbIoRes res(it.second.get());
        if (res.retCode != 0)
        {
            continue;
        }
        TLOGINFO(__FILE__<<"-"<<__LINE__<< " " << it.first << " size: " << res.data.size() << endl);
        if (it.first == "tag")
        {
            _return.__set_weightedTags(res.data);
        }
        UfsServiceLogic::SsdbIoRes res(it.second.get());
        if (res.retCode != 0)
        {
            continue;
        }
        TLOGINFO(__FILE__<<"-"<<__LINE__<< " " << it.first << " size: " << res.data.size() << endl);
        if (it.first == "tag")
        {
            _return.__set_weightedTags(res.data);
        }
        else if (it.first == "category")
        {
            _return.__set_weightedCategories(res.data);
        }
        else if (it.first == "negative")
        {
            _return.__set_impressionTitleTags(res.data);
        }
        else if (it.first == "topic")
        {
            _return.__set_ldaTopic(res.data);
        }
        else if (it.first == "lastClick")
        {
            _return.__set_lastClickTimeOfCategory(res.data);
        }
        else if (it.first == "lastImpression")
        {
            _return.__set_lastImpressionTimeOfCategory(res.data);
        }
        else if (it.first == "userGmp")
        {
            _return.__set_userGmp(res.data);
        }
        else if (it.first == "behavior")
        {
            _return.__set_userprofile(res.data);
        }
        else if (it.first == "categoryClickAndImp")
        {
            _return.__set_categoryClickAndImp(res.data);
        }
        else if (it.first == "category")
        {
            _return.__set_weightedCategories(res.data);
        }
        else if (it.first == "negative")
        {
            _return.__set_impressionTitleTags(res.data);
        }
        else if (it.first == "topic")
        {
            _return.__set_ldaTopic(res.data);
        }
        else if (it.first == "lastClick")
        {
            _return.__set_lastClickTimeOfCategory(res.data);
        }
        else if (it.first == "lastImpression")
        {
            _return.__set_lastImpressionTimeOfCategory(res.data);
        }
        else if (it.first == "userGmp")
        {
            _return.__set_userGmp(res.data);
        }
        else if (it.first == "behavior")
        {
            _return.__set_userprofile(res.data);
        }
        else if (it.first == "categoryClickAndImp")
        {
            _return.__set_categoryClickAndImp(res.data);
        }
    }
#else
    std::atomic<bool> takeIn[USER_INFO_OVERFLOW];
    for (size_t i=0; i<USER_INFO_OVERFLOW; ++i) {
        takeIn[i] = !(bool)m_takeIn[i];
    }

    //static const int threadSize = m_userInfoSize;
    //thread_local ThreadPool g_pool(threadSize);
    std::string request_id = uid ;
    std::vector<std::string> uids = {uid};
    request_id.append(std::to_string(TNOWMS));
    std::unordered_map<int, std::future<SsdbIoRes>> result;
    std::set<int> index;

    if (takeInTag_ && !tagsVersion.empty()) {
        /* tagHash_=zhizi.ufs.tag. */
        std::string hash = this->tagHash_ + tagsVersion;
        inv::INV_Redis* redis = ChooseSsdbConn(this->vecTagRedis_, uid, "");
        index.insert(USER_INFO_TAG);

        result.emplace(USER_INFO_TAG, g_pool->enqueue([=, &takeIn]{
                int64_t start = INV_Util::now2ms();
                UfsServiceLogic::SsdbIoRes res;
                res.request_id = request_id;
                res.type = USER_INFO_TAG;
                takeIn[USER_INFO_TAG] = true;
                //res.retCode = RedisHashGet(res.data, redis, hash, uid);
                res.retCode = RedisHashMGet(res.data, redis, hash, uids);

                FDLOG("hmget") << __FILE__<<"-"<<__LINE__<<" "<< hash<<","<<uid << ", ret=" << res.retCode<<", type="<<res.type<<std::endl;
                FDLOG("monitor") << "|" << local_ip_ << "&&" << "ufs.getuserinfo.ssdb"
                << "&&" << (INV_Util::now2ms() - start)  << "&&" << 0 << "&&" << "AVG" << "&&" << 60 << "&&" << "" << endl;

                return res;
                }));
    }

    if (takeInCategory_ && !catVersion.empty()) {
        /*  zhizi.ufs.category. */
        std::string hash = this->categoryHash_ + catVersion;
        inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecCategoryRedis_, uid, "");
        index.insert(USER_INFO_CATEGORY);

        result.emplace(USER_INFO_CATEGORY, g_pool->enqueue([=, &takeIn]{
                int64_t start = INV_Util::now2ms();
                UfsServiceLogic::SsdbIoRes res;
                res.request_id = request_id;
                res.type = USER_INFO_CATEGORY;
                takeIn[USER_INFO_CATEGORY] = true;
                //res.retCode = RedisHashGet(res.data, redis, hash, uid);
                res.retCode = RedisHashMGet(res.data, redis, hash, uids);
                
                FDLOG("hmget") << __FILE__<<"-"<<__LINE__<<" "<< hash<<","<<uid << ", ret=" << res.retCode<<", type="<<res.type<<std::endl;
                FDLOG("monitor") << "|" << local_ip_ << "&&" << "ufs.getuserinfo.ssdb"
                << "&&" << (INV_Util::now2ms() - start)  << "&&" << 0 << "&&" << "AVG" << "&&" << 60 << "&&" << "" << endl;

                return res;
                }));
    }

    if (takeInNegative_ && !titleTagsVersion.empty()) {
        /* not_implement_yet */
        std::string hash = this->negativeImpressionHash_ + titleTagsVersion;
        inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecNegativeRedis_, uid, "");
        index.insert(USER_INFO_NEGATIVE);

        result.emplace(USER_INFO_NEGATIVE, g_pool->enqueue([=, &takeIn]{
                int64_t start = INV_Util::now2ms();
                UfsServiceLogic::SsdbIoRes res;
                res.request_id = request_id;
                res.type = USER_INFO_NEGATIVE;
                takeIn[USER_INFO_NEGATIVE] = true;
                //res.retCode = RedisHashGet(res.data, redis, hash, uid);
                res.retCode = RedisHashMGet(res.data, redis, hash, uids);

                FDLOG("hmget") << __FILE__<<"-"<<__LINE__<<" "<< hash<<","<<uid << ", ret=" << res.retCode<<", type="<<res.type<<std::endl;
                FDLOG("monitor") << "|" << local_ip_ << "&&" << "ufs.getuserinfo.ssdb"
                << "&&" << (INV_Util::now2ms() - start)  << "&&" << 0 << "&&" << "AVG" << "&&" << 60 << "&&" << "" << endl;

                return res;
                }));
    }

    if (takeInTopic_ && !ldaTopicVersion.empty()) {
        /* zhizi.topic.lda. */
        std::string hash = this->ldaTopicHash_ + ldaTopicVersion;
        inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecTopicRedis_, uid, "");
        index.insert(USER_INFO_TOPIC);

        result.emplace(USER_INFO_TOPIC, g_pool->enqueue([=, &takeIn]{
                int64_t start = INV_Util::now2ms();
                UfsServiceLogic::SsdbIoRes res;
                res.request_id = request_id;
                res.type = USER_INFO_TOPIC;
                takeIn[USER_INFO_TOPIC] = true;
                //res.retCode = RedisHashGet(res.data, redis, hash, uid);
                res.retCode = RedisHashMGet(res.data, redis, hash, uids);

                FDLOG("hmget") << __FILE__<<"-"<<__LINE__<<" "<< hash<<","<<uid << ", ret=" << res.retCode<<", type="<<res.type<<std::endl;
                FDLOG("monitor") << "|" << local_ip_ << "&&" << "ufs.getuserinfo.ssdb"
                << "&&" << (INV_Util::now2ms() - start)  << "&&" << 0 << "&&" << "AVG" << "&&" << 60 << "&&" << "" << endl;

                return res;
                }));
    }

    if (takeInLastClick_) /* false */ {
        /* zhizi.ufs.reformat.click */
        std::string hash = this->reformatClickHash_;
        inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecLastClickRedis_, uid, "");
        index.insert(USER_INFO_LAST_CLICK);

        result.emplace(USER_INFO_LAST_CLICK, g_pool->enqueue([=, &takeIn]{
                int64_t start = INV_Util::now2ms();
                UfsServiceLogic::SsdbIoRes res;
                res.request_id = request_id;
                res.type = USER_INFO_LAST_CLICK;
                takeIn[USER_INFO_LAST_CLICK] = true;
                //res.retCode = RedisHashGet(res.data, redis, hash, uid);
                res.retCode = RedisHashMGet(res.data, redis, hash, uids);

                FDLOG("hmget") << __FILE__<<"-"<<__LINE__<<" "<< hash<<","<<uid << ", ret=" << res.retCode<<", type="<<res.type<<std::endl;
                FDLOG("monitor") << "|" << local_ip_ << "&&" << "ufs.getuserinfo.ssdb"
                << "&&" << (INV_Util::now2ms() - start)  << "&&" << 0 << "&&" << "AVG" << "&&" << 60 << "&&" << "" << endl;

                return res;
                }));
    }

    if (takeInLastImpression_) /* false */ {
        /* zhizi.ufs.reformat.impression */
        std::string hash = this->reformatImpressionHash_;
        inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecLastImpressionRedis_, uid, "");
        index.insert(USER_INFO_LAST_IMPRESSION);

        result.emplace(USER_INFO_LAST_IMPRESSION, g_pool->enqueue([=, &takeIn]{
                int64_t start = INV_Util::now2ms();
                UfsServiceLogic::SsdbIoRes res;
                res.request_id = request_id;
                res.type = USER_INFO_LAST_IMPRESSION;
                takeIn[USER_INFO_LAST_IMPRESSION] = true;
                //res.retCode = RedisHashGet(res.data, redis, hash, uid);
                res.retCode = RedisHashMGet(res.data, redis, hash, uids);

                FDLOG("hmget") << __FILE__<<"-"<<__LINE__<<" "<< hash<<","<<uid << ", ret=" << res.retCode<<", type="<<res.type<<std::endl;
                FDLOG("monitor") << "|" << local_ip_ << "&&" << "ufs.getuserinfo.ssdb"
                << "&&" << (INV_Util::now2ms() - start)  << "&&" << 0 << "&&" << "AVG" << "&&" << 60 << "&&" << "" << endl;

                return res;
                }));
    }

    if (takeInUserGmp_) {
        /* zhizi.user.gmp  */
        std::string hash = this->userGmpHash_;
        inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecUserGmpRedis_, uid, "");
        index.insert(USER_INFO_USER_GMP);

        result.emplace(USER_INFO_USER_GMP, g_pool->enqueue([=, &takeIn]{
                int64_t start = INV_Util::now2ms();
                UfsServiceLogic::SsdbIoRes res;
                res.request_id = request_id;
                res.type = USER_INFO_USER_GMP;
                takeIn[USER_INFO_USER_GMP] = true;
                //res.retCode = RedisHashGet(res.data, redis, hash, uid);
                res.retCode = RedisHashMGet(res.data, redis, hash, uids);

                FDLOG("hmget") << __FILE__<<"-"<<__LINE__<<" "<< hash<<","<<uid << ", ret=" << res.retCode<<", type="<<res.type<<std::endl;
                FDLOG("monitor") << "|" << local_ip_ << "&&" << "ufs.getuserinfo.ssdb"
                << "&&" << (INV_Util::now2ms() - start)  << "&&" << 0 << "&&" << "AVG" << "&&" << 60 << "&&" << "" << endl;

                return res;
                }));
    }

    if (takeInProfile_) /* false */ {
        std::string hash = this->userProfileHash_;
        inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecProfileRedis_, uid, "");
        index.insert(USER_INFO_PROFILE);

        result.emplace(USER_INFO_PROFILE, g_pool->enqueue([=, &takeIn]{
                int64_t start = INV_Util::now2ms();
                UfsServiceLogic::SsdbIoRes res;
                res.request_id = request_id;
                res.type = USER_INFO_PROFILE;
                takeIn[USER_INFO_PROFILE] = true;
                //res.retCode = RedisHashGet(res.data, redis, hash, uid);
                res.retCode = RedisHashMGet(res.data, redis, hash, uids);

                FDLOG("hmget") << __FILE__<<"-"<<__LINE__<<" "<< hash<<","<<uid << ", ret=" << res.retCode<<", type="<<res.type<<std::endl;
                FDLOG("monitor") << "|" << local_ip_ << "&&" << "ufs.getuserinfo.ssdb"
                << "&&" << (INV_Util::now2ms() - start)  << "&&" << 0 << "&&" << "AVG" << "&&" << 60 << "&&" << "" << endl;

                return res;
                }));
    }

    if (takeInBehavior_) /* false */ {
        std::string hash = this->userBehaviorHash_;
        inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecBehaviorRedis_, uid, "");
        index.insert(USER_INFO_BEHAVIOR);

        result.emplace(USER_INFO_BEHAVIOR, g_pool->enqueue([=, &takeIn]{
                int64_t start = INV_Util::now2ms();
                UfsServiceLogic::SsdbIoRes res;
                res.request_id = request_id;
                res.type = USER_INFO_BEHAVIOR;
                takeIn[USER_INFO_BEHAVIOR] = true;
                //res.retCode = RedisHashGet(res.data, redis, hash, uid);
                res.retCode = RedisHashMGet(res.data, redis, hash, uids);

                FDLOG("hmget") << __FILE__<<"-"<<__LINE__<<" "<< hash<<","<<uid << ", ret=" << res.retCode<<", type="<<res.type<<std::endl;
                FDLOG("monitor") << "|" << local_ip_ << "&&" << "ufs.getuserinfo.ssdb"
                << "&&" << (INV_Util::now2ms() - start)  << "&&" << 0 << "&&" << "AVG" << "&&" << 60 << "&&" << "" << endl;

                return res;
                }));
    }

    if (takeInCategoryClickAndImp_) {
        /* zhizi.ufs.category.clickandimp. */
        std::string hash = this->userCategoryClickAndImpHash_ + "v28";//目前分类都是用v28
        inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(this->vecCategoryClickAndImpRedis_, uid, "");
        index.insert(USER_INFO_CATEGORY_CLICK_AND_IMP);

        result.emplace(USER_INFO_CATEGORY_CLICK_AND_IMP, g_pool->enqueue([=, &takeIn]{
                int64_t start = INV_Util::now2ms();
                UfsServiceLogic::SsdbIoRes res;
                res.request_id = request_id;
                res.type = USER_INFO_CATEGORY_CLICK_AND_IMP;
                takeIn[USER_INFO_CATEGORY_CLICK_AND_IMP] = true;
                //res.retCode = RedisHashGet(res.data, redis, hash, uid);
                res.retCode = RedisHashMGet(res.data, redis, hash, uids);

                FDLOG("hmget") << __FILE__<<"-"<<__LINE__<<" "<< hash<<","<<uid << ", ret=" << res.retCode<<", type="<<res.type<<std::endl;
                FDLOG("monitor") << "|" << local_ip_ << "&&" << "ufs.getuserinfo.ssdb"
                << "&&" << (INV_Util::now2ms() - start)  << "&&" << 0 << "&&" << "AVG" << "&&" << 60 << "&&" << "" << endl;

                return res;
                }));
    }

    if (m_takeInNewUid) {
        /* zhizi.ufs.category.clickandimp. */
        inv::INV_Redis* redis = UfsServiceLogic::ChooseSsdbConn(m_vecNewUidRedis, uid, "");
        index.insert(USER_INFO_NEW_UID);

        result.emplace(USER_INFO_NEW_UID, g_pool->enqueue([=, &takeIn]{
                int64_t start = INV_Util::now2ms();
                UfsServiceLogic::SsdbIoRes res;
                res.request_id = request_id;
                res.type = USER_INFO_NEW_UID;
                takeIn[USER_INFO_NEW_UID] = true;
                res.retCode = RedisHashMGet(res.data, redis, uid, g_constFields);

                FDLOG("hmget") << __FILE__<<"-"<<__LINE__<<" "<< uid << ", size="<<g_constFields.size()<<", g_constFields[size]:"<<g_constFields[g_constFields.size()-1]<<",ret=" << res.retCode<<",type="<<res.type<<std::endl;

                FDLOG("monitor") << "|" << local_ip_ << "&&" << "ufs.getuserinfo.ssdb"
                << "&&" << (INV_Util::now2ms() - start)  << "&&" << 0 << "&&" << "AVG" << "&&" << 60 << "&&" << "" << endl;

                return res;
                }));
    }

    int cnt = 0;
    for (auto it = index.begin(); it != index.end(); ++it) {
        SsdbIoRes res = result[*it].get();
        FDLOG("hmget") << __FILE__<<"-"<<__LINE__<<" cnt="<<cnt<<", *it="<<*it<<", flag="<<takeIn[*it]<<std::endl;
        takeIn[*it] = false;
        ++cnt;
        if (res.data.size()) {
            if ((*it == USER_INFO_TAG || *it == USER_INFO_CATEGORY) && res.retCode != 0) {
                cnt = m_userInfoSize;
                break;
            }

            if (res.retCode != 0) {
                continue;
            }

            SetUserInfo(*it, _return, res);
        }
    }

    FDLOG("hmget") << __FILE__<<"-"<<__LINE__<<" loop end" << ", cnt="<<cnt<<std::endl;

    /* because tag and category is require, if them configure with false, then must be set default value which are null-string */
    if (!takeInTag_) {
        _return.__set_weightedTags("");
    }

    if (!takeInTopic_) {
        _return.__set_weightedCategories("");
    }
#endif
    ReportAvg("zhizi.allinone.getuserinfo.time", TIME_DIFF(1));
    FDLOG("monitor") << "|" << this->local_ip_ << "&&" << "ufs.getuserinfo.responsetime"
            << "&&" << (INV_Util::now2ms() - start)  << "&&" << 0 << "&&" << "AVG" << "&&" << 60 << "&&" << "" << endl;

#if defined(DUSER_INFO)
    FDLOG("user_info")
        <<"weightedCategories:"<<_return.weightedCategories
        <<"\nweightedTags:"<<_return.weightedTags
        <<"\nimpressionTitleTags:"<<_return.impressionTitleTags
        <<"\nlastClickTimeOfCategory:"<<_return.lastClickTimeOfCategory
        <<"\nlastImpressionTimeOfCategory:"<<_return.lastImpressionTimeOfCategory
        <<"\nuserGmp:"<<_return.userGmp
        <<"\nuserprofile:"<<_return.userprofile
        <<"\nldaTopic:"<<_return.ldaTopic
        <<"\ncategoryClickAndImp:"<<_return.categoryClickAndImp
        <<"\npublisher:"<<_return.publisher
        <<"\nthridpartyTopic:"<<_return.thridpartyTopic
        <<"\nthridpartyKeyword:"<<_return.thridpartyKeyword
        <<"\n";
#endif

    TLOGINFO(__FILE__<<"-"<<__LINE__<< " uid: " << uid << " time used: " << TIME_DIFF(1) << endl);

    return;
}

int32_t UfsServiceLogic::SetUserGmp(const std::map<std::string, std::string> & kvs)
{
    return 0;
}

inv::INV_Redis* UfsServiceLogic::ChooseSsdbConn(const std::vector<inv::INV_Redis*>& conns,
        const std::string& uid, const std::string& salt)
{
    size_t idx = GetIdxOfConns(uid, conns.size(), salt);
    inv::INV_Redis* redis = conns[idx];
    return redis;
}

