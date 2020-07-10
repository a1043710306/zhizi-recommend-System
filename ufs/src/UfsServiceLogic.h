#ifndef UFSSERVICELOGIC_H
#define UFSSERVICELOGIC_H 
#include <future>
#include <string>

#include "rapidjson/writer.h"
#include "rapidjson/stringbuffer.h"
#include "rapidjson/error/en.h"
#include "rapidjson/document.h"
#include "rapidjson/pointer.h"

#include "UfsService_types.h"
#include "ufs_logic.h"
#include"ThreadPool/ThreadPool.h"
extern ThreadPool* g_pool; 

using namespace inv;

#define USER_INFO_TABLE \
    X(USER_INFO_TAG, "tag") \
X(USER_INFO_CATEGORY, "category") \
X(USER_INFO_NEGATIVE, "negative") \
X(USER_INFO_TOPIC, "topic") \
X(USER_INFO_LAST_CLICK, "lastClick") \
X(USER_INFO_LAST_IMPRESSION, "lastImpression") \
X(USER_INFO_USER_GMP, "userGmp") \
X(USER_INFO_PROFILE, "profile") \
X(USER_INFO_BEHAVIOR, "behavior") \
X(USER_INFO_CATEGORY_CLICK_AND_IMP, "categoryClickAndImp")\
X(USER_INFO_NEW_UID, "new_uid") \
X(USER_INFO_OVERFLOW, "overflow")

#define X(a, b) a,
enum USER_INFO {
    USER_INFO_TABLE
};
#undef X

extern const StrLiter UserInfoTableStr[USER_INFO_OVERFLOW + 1];
class UfsServiceLogic
{
    public:
        UfsServiceLogic();
        virtual ~UfsServiceLogic(); // {}
        void init(const std::string& confpath);
        void GetWeightedCategories(std::string& _return, const std::string& uid, const std::string& version);
        void GetWeightedTags(std::string& _return, const std::string& uid, const std::string& version);

        void GetImpressionTitleTags(std::string& _return, const std::string& uid, const std::string& version);
        void GetDislikeTitleTags(std::string& _return, const std::string& uid, const std::string& version);

        void GetLdaTopic(std::string& _return, const std::string& uid, const std::string& version);

        void GetLastActionTimeOfCategory(std::string& _return, const std::string& uid, const ActionType::type type);
        void GetTimeFeature(std::string& _return, const std::string& uid);
        void GetNetworkFeature(std::string& _return, const std::string& uid);
        void GetTagStats(std::string& _return, const std::string& uid);
        void GetCategoryStats(std::string& _return, const std::string& uid);
        void GetCreateTime(std::string& _return, const std::string& uid);
        void GetInterest(std::string& _return, const std::string& uid);
        int32_t SetInterest(const std::string& uid, const std::string& jsonArray);

        void GetUserInfo(UserInfo& _return, const std::string& uid, const std::string& catVersion,
                const std::string& tagsVersion, const std::string& titleTagsVersion,
                const std::string& ldaTopicVersion);
        int32_t SetUserGmp(const std::map<std::string, std::string> & kvs);

        using DocumentType = rapidjson::GenericDocument<rapidjson::UTF8<>, rapidjson::MemoryPoolAllocator<>, rapidjson::MemoryPoolAllocator<>>;

    protected:
        void GetWeightedJson(std::string& _return, const std::string& type, const std::string& uid, const std::string& version);
        void HdfsFeature(std::string& _return, const std::string& uid, const std::string& hash);
        void GetUfsStats(std::string& _return, const std::string& uid, const std::string& hash);
        int32_t GetNegativeJson(std::string& json, const std::string& uid, const std::string& version, const std::string& type);
        static inv::INV_Redis* ChooseSsdbConn(const std::vector<inv::INV_Redis*>& conns,
                const std::string& uid, const std::string& salt);
        
        struct SsdbIoRes
        {
            std::string request_id;
            int type;
            int retCode;
            std::vector<std::string> data;
        };

        void MergeJsonOfClickAndImp(int type, const std::string& clickStr, const std::string& impStr, std::string& out);
        std::string MergeJsonOfClickAndImp(int type, const std::string* clickStr, const std::string* impStr);
        void SetUserInfo(int type, UserInfo& _return, const UfsServiceLogic::SsdbIoRes& res);
        void SetUserInfo(int type, UserInfo& _return, UfsServiceLogic::SsdbIoRes&& res);
        static UfsServiceLogic::SsdbIoRes SsdbWrapper(inv::INV_Redis* redis, const std::string& hash, const std::string& uid, const std::string& local_ip);
#if 0
        {
            int64_t start = INV_Util::now2ms();
            UfsServiceLogic::SsdbIoRes res;
            res.retCode = RedisHashGet(res.data, redis, hash, uid);
            FDLOG("monitor") << "|" << local_ip << "&&" << "ufs.getuserinfo.ssdb"
                << "&&" << (INV_Util::now2ms() - start)  << "&&" << 0 << "&&" << "AVG" << "&&" << 60 << "&&" << "" << endl;
            return res;
        }
#endif

        std::string categoryHash_;
        std::string tagHash_;

        std::string negativeImpressionHash_;
        std::string negativeDislikeHash_;

        std::string ldaTopicHash_;

        std::string ufsStatsCategory_;
        std::string ufsStatsTag_;

        std::string reformatClickHash_;
        std::string reformatImpressionHash_;

        std::string timeFeatureHash_;
        std::string networkFeatureHash_;

        std::string newUserHash_;
        std::string interestHash_;

        std::string userGmpHash_;
        std::string userProfileHash_;
        std::string userBehaviorHash_;
        std::string userCategoryClickAndImpHash_;

        std::string local_ip_;

        uint32_t decayPeriod_;
        double defaultDecayRate_;
        std::map<std::string, double> decayRate_;

        std::vector<inv::INV_Redis*> vecRedis_;// reference
        // std::vector<inv::INV_Redis*> vecNegativeRedis_;
        // std::vector<inv::INV_Redis*> vecTopicRedis_;
        std::string nodeName_;

        SfuLogic* logic_;

        bool takeInTag_;
        bool takeInCategory_;
        bool takeInNegative_;
        bool takeInTopic_;
        bool takeInLastClick_;
        bool takeInLastImpression_;
        bool takeInUserGmp_;
        bool takeInProfile_;
        bool takeInBehavior_;
        bool takeInCategoryClickAndImp_;
        bool m_takeInNewUid;
        std::vector<inv::INV_Redis*> vecTagRedis_;
        std::vector<inv::INV_Redis*> vecCategoryRedis_;
        std::vector<inv::INV_Redis*> vecNegativeRedis_;
        std::vector<inv::INV_Redis*> vecTopicRedis_;
        std::vector<inv::INV_Redis*> vecLastClickRedis_;
        std::vector<inv::INV_Redis*> vecLastImpressionRedis_;
        std::vector<inv::INV_Redis*> vecUserGmpRedis_;
        std::vector<inv::INV_Redis*> vecProfileRedis_;
        std::vector<inv::INV_Redis*> vecBehaviorRedis_;
        std::vector<inv::INV_Redis*> vecCategoryClickAndImpRedis_;
        std::vector<inv::INV_Redis*> m_vecNewUidRedis;
        std::atomic<bool> m_takeIn[USER_INFO_OVERFLOW];
        int m_userInfoSize;
        std::vector<char*>  m_valueAllocator;
        std::vector<char*>  m_parseAllocator;
        int m_valueAllocatorSize[TQUERY_OVERFLOW];
        int m_parseAllocatorSize[TQUERY_OVERFLOW];
        int m_getUserInfoCount;
};

#endif /* UFSSERVICELOGIC_H */
