#ifndef LOGIC_H
#define LOGIC_H 
#include <string>
#include <vector>
#include <map>
#include <unordered_set>
#include <memory>

// #include <boost/shared_ptr.hpp>
#include <memory>
#include "rapidjson/document.h"     // rapidjson's DOM-style API
#include "inv_mysql.h"
#include "inv_redis.h"

#include "db.h"
#include "kv_cache.h"
#include "ufs_in_mem.h"

class SfuLogic
{
    private:
        typedef rapidjson::GenericDocument<rapidjson::UTF8<>, rapidjson::MemoryPoolAllocator<>, rapidjson::MemoryPoolAllocator<>> DocumentType;
    public:
        SfuLogic(std::shared_ptr<inv::INV_Mysql> conn, int decayPeriod,
                 double defaultDecayRate, const std::map<std::string, double>& decayRate,
                 const std::vector<inv::INV_Redis*>& vecRedis,
                 const std::vector<inv::INV_Redis*>& vecWRedis, const std::string& ufsCategory,
                 const std::string& ufsTag, const std::string& ufsStatsCategory,
                 const std::string& ufsStatsTag, int ufsNum, int timeZone,
                 int cacheNum, int threadNum, int32_t writeCold, const std::string categoryClickAndImpHashName);
        SfuLogic(std::shared_ptr<inv::INV_Mysql> conn, const std::vector<inv::INV_Redis*>& vecRedis,
                const std::string& ufsHash, int timeZone,
                std::shared_ptr<KVCache<std::string, std::vector<std::string>>> cache,
                const std::string& typicalVersion);
        SfuLogic(int decayPeriod, double defaultDecayRate, const std::map<std::string, double>& decayRate,
                const std::vector<inv::INV_Redis*>& vecRedis,
                const std::string& ufsCategory, const std::string& ufsTag,
                const std::string& ufsStatsCategory, const std::string& ufsStatsTag);
        // SfuLogic(const std::vector<inv::INV_Redis*>& vecRedis, const std::string& reformatHash);
        virtual ~SfuLogic();

        void ActivateSync()
        {
            this->uim_->ActivateSync();
        }

        int32_t FeedUfs(const std::string& kafkaMsg, const std::unordered_set<std::string>& black_words,
                const std::unordered_set<std::string>& categoryVersionToBeRemoved,
                const std::unordered_set<std::string>& tagVersionToBeRemoved);
        int32_t FeedImpUfs(const std::string kafkaMsg);

        int32_t UpdateClickTimeOfCategory(const std::string& kafkaMsg);
        int32_t UpdateImpressionTimeOfCategory(const std::string& kafkaMsg);

        int32_t GetSsdbJson(DocumentType* pd, const std::string& type, const std::string& version,
                const std::string& uid);
        int32_t JsonWeight2Map(std::map<std::string, double>& out, const std::string& type, const DocumentType* pd,
                int curTime, const std::string& configId);
        std::vector<std::pair<std::string, double> >
            SortMapByWeight(const std::map<std::string, double>& merged,
                    const std::unordered_set<std::string>& blackWords);
        static int32_t SerializeToSsdbJson(std::string& out, std::vector<std::pair<std::string, double> >& wtags,
                int32_t ts, const std::string& app, const std::string& configId);
    private:
        struct LogEntry
        {
            std::string uid;
            std::string infoId;
            int32_t logTime;
        };
        struct NlpSignal
        {
            std::string keywords;
            std::string tags;
            std::string categories;
        };
    private:
        static bool IsDecimal(const std::string& word);
        int64_t DiffTime(struct timeval* a, struct timeval* b);
        double GetDecayFactor(int32_t current, int32_t create_time, const std::string& configId);
        int32_t JsonDoc2Map(std::map<std::string, std::map<std::string, double> >& out, const rapidjson::Document* pd, int curTime);
        std::map<std::string, std::vector<std::pair<std::string, double> > >
            SortByWeight(const std::map<std::string, std::map<std::string, double> >& merged,
                    const std::unordered_set<std::string>& black_words,
                    const std::unordered_set<std::string>& versionToBeRemoved,
                    const std::string& uid
                    );
        // std::map<std::string, std::vector<std::pair<std::string, double> > >
        //     SortByWeight(const std::map<std::string, std::map<std::string, double> >& merged);
        std::map<std::string, double> JsonWeightArray2Map(const std::string& type, const rapidjson::Value& obj, double decayFactor);

        static int32_t SerializeToJson(std::string& out, const std::map<std::string, std::vector<std::pair<std::string, double> > >& wtags,
                const std::map<std::string, std::vector<std::string> >& tags, int32_t ts);
        static int32_t JoinWeightTags(rapidjson::Document* pd, std::map<std::string, std::map<std::string, double> >& newWt);
        static int32_t GetWeightedTags(std::map<std::string, std::map<std::string, double> >& ret,
                const std::string& json);
        // static std::map<std::string, double> MergeWeightMaps(const std::map<std::string, double>&m1, const std::map<std::string, double>& m2);
        static int32_t MergeWeightMaps(std::shared_ptr<std::map<std::string, double>>& m1, const std::map<std::string, double>& m2);
        // static std::map<std::string, std::map<std::string, double> > MergeTagMaps(const std::map<std::string, std::map<std::string, double> >&m1,
        //         const std::map<std::string, std::map<std::string, double> >& m2);
        static bool WeightCmp(const std::pair<std::string, double>& a, const std::pair<std::string, double>& b)
        {
            return b.second < a.second;
        }

        static std::string GetWeightTagJson(const std::pair<std::string, std::map<std::string, double> >& wt);
        int32_t GetTimestampFromDateTime(const std::string& date_time, int32_t &timestamp);
        time_t time_to_epoch ( const struct tm *ltm, int utcdiff);

        std::string GetHashName(const std::string& type, const std::string& version);
        inv::INV_Redis* ChooseSsdb(const std::string& uid);

        int32_t WriteBack(const std::string& app);
        int32_t SetUserCategory(const std::string& uid, const std::map<std::string, int32_t>& category, const std::string& app);
        std::string MakeJsonOfCategory(const std::map<std::string, int32_t>& category, const std::string& app);
        int32_t GetCategoryOfUserFromSsdb(std::map<std::string, int32_t>& category, const std::string& uid);
        int32_t GetCategoryOfUser(std::map<std::string, int32_t>& category,
                const std::string& uid);
        int32_t GetCategoryFromDb(std::vector<std::string>& category, const std::string& infoId);
        int32_t GetCategoryOfInfo(std::vector<std::string>& category, const std::string& infoId);
        int32_t ParseClickMsg(std::string& uid, std::string& infoId, std::string& dateTime,
                std::string& app, std::string& configId, const std::string& kafkaMsg, int& contentType);
        int32_t ParseImpressionMsg(std::string& uid, std::string& infoId, std::string& dateTime,
                std::string& app, const std::string& kafkaMsg, int& contentType);
        int32_t UpdateUfs(const std::string& type, const std::string& dbJson, const std::string& uid,
                const std::string& app, const std::unordered_set<std::string>& blackWords,
                const std::unordered_set<std::string>& versionToBeRemoved, const std::string& configId);
        int32_t UpdateUfsCategoryClickOrImp(const std::string& dbJson, const std::string& uid, const int32_t eventTime, const std::string type);


        int32_t UpdateUfsStats(const std::string& type, const std::string& uid,
                const std::map<std::string, std::pair<uint32_t, double>>& statsOfCt);
        std::string GetStatsHashName(const std::string& type);

        static int32_t SerializeStatsJson(std::string& out,
                const std::map<std::string, std::pair<uint32_t, double>>& stats,
                int32_t ts);
        // static std::map<std::string, std::pair<uint32_t, double>>
        static int32_t MergeStatsMaps(std::shared_ptr<std::map<std::string, std::pair<uint32_t, double>>>& oldMap,
                    const std::map<std::string, std::pair<uint32_t, double>>& newMap);
        static int32_t JsonStats2Map(std::map<std::string, std::pair<uint32_t, double>>& statsMap, const rapidjson::Document* pd);
        int32_t GetSsdbUfsStats(rapidjson::Document* pd, const std::string& type, const std::string& uid);

        int32_t ProfileWriteBack(const std::string& app, const std::string& version, const std::string& type,
                const std::string& configId, const std::unordered_set<std::string>& blackWords);
        int32_t SetUserProfile(const std::string& uid, const std::string& version,
                const std::map<std::string, double>& profile, const std::string& app, const std::string& type,
                const std::string& configId, const std::unordered_set<std::string>& blackWords);
        int32_t GetUserProfile(std::map<std::string, double>& profile, const std::string& type,
                const std::string& uid, const std::string& version, const std::string& keyDecay);
        int32_t GetSsdbProfile(std::map<std::string, double>& profile, const std::string& type,
                const std::string& uid, const std::string& version, const std::string& keyDecay);

    protected:
        int GetByTQuery(const std::string& content_id, const std::string& tableSuffix, std::map<std::string, std::string>& out);
        int ParseKeywordsFromTContentYYYYMM(const char* keywords, std::multimap<std::string, std::string>& mulMap);
        int ParseTopicsFromTContentExtened(const char* topics, std::unordered_set<std::string>& out);
        int UpdateUfsTQueryClickOrImp(const std::string& uid, const std::string& tableSuffix, const std::string& content_id, double decayRate, int decayPeriod, int eventTime, int type);

    private:
        std::shared_ptr<inv::INV_Mysql> conn_;
        TSignal* signal_;
        TIndex* tableIndex_;
        TQuery* m_tQuery;
        //TIdMap* idMap_;
        // std::string url_;

        int decayPeriod_;
        double defaultDecayRate_;
        std::map<std::string, double> decayRate_;

        std::vector<inv::INV_Redis*> vecRedis_;
        std::vector<inv::INV_Redis*> vecWRedis_;
        // inv::INV_Redis *redis_;
        // inv::INV_Redis *coolpad_redis_;
        std::string ufsHash_;
        std::string ufsCategory_;
        std::string ufsTag_;
        std::string ufsStatsCategory_;
        std::string ufsStatsTag_;

        size_t ufsNum_;
        int timeZone_;

        // std::map<std::string, std::pair<int32_t, std::vector<std::string>>> infoCgCache_;
        std::shared_ptr<KVCache<std::string, std::vector<std::string>>> cache_;//global
        std::string typicalVersion_;
        std::map<std::string, std::map<std::string, int32_t>> reformatCache_;
        std::map<std::string, std::map<std::string, std::map<std::string, std::map<std::string, double>>>> ufsCache_;
    private:
        const size_t jsonBufSize_ = 10*1024*1024;
        char* valueBuffer_;
        char* parseBuffer_;
        // rapidjson::MemoryPoolAllocator<>* valueAllocator_;
        // rapidjson::MemoryPoolAllocator<>* parseAllocator_;
        UfsInMem* uim_;
    public:
        int32_t GetRpcJson(std::string& _return, const std::string& type, const std::string& uid, const std::string& version);
};

#define PARSE_ERROR_TABLE \
    X(PARSE_CONTENT_TYPE, " content_type: ")\
X(PARSE_NO_CONTENT_TYPE, " wrong record no content_type\n") \
X(PARSE_NO_UID, " wrong record no uid\n") \
X(PARSE_NO_CONTENT_ID, " wrong record no content_id\n") \
X(PARSE_NO_EVNET_TIME, " wrong record no event_time\n") \
X(PARSE_NO_LOG_TIME, " wrong record no log_time\n") 

#define X(a, b) a,
enum EPARSE_ERROR_TABLE {
    PARSE_ERROR_TABLE
};
#undef X

extern const char* ParseErrorTableStr[];

#endif /* LOGIC_H */
