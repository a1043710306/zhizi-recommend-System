#ifndef UFS_IN_MEM_H
#define UFS_IN_MEM_H 
#include <string>
#include <map>
#include <unordered_set>
#include <memory>
#include<array>

#include "rapidjson/document.h"     // rapidjson's DOM-style API

#include "ssdb_io.h"
#include"db.h"
#include "linked_kv_cache.h"

class UfsInMem {
    public:
        UfsInMem(size_t cacheSize, const std::string& hashCategory,
                 const std::string& hashTag, const std::string& hashStatsCategory,
                 const std::string& hashStatsTag, const std::vector<inv::INV_Redis*>& vecRedis,
                 const std::vector<inv::INV_Redis*>& vecWRedis, int32_t writeCold,
                 const std::string& categoryClickAndImpHashName);
                 //inv::INV_Redis* ssdbSync);
        virtual ~UfsInMem();

        // typedef std::map<int32_t, double> WC;// category as key, weight as value
        typedef std::map<std::string, double> WU;// tag as key, weight as value
        typedef std::map<std::string, std::pair<double, double>> MCG;//mapCategoryClickAndImp, <category,<click,imp>>
        typedef std::map<std::string, std::pair<uint32_t, double>> VS;// version as key

        using NEWCACHE_PAIR = std::pair<std::string, std::array<double, 2>>;
        struct NewCacheCompare : public std::binary_function<NEWCACHE_PAIR, NEWCACHE_PAIR, bool> {
            bool operator()(const NEWCACHE_PAIR& l, const NEWCACHE_PAIR& r) {
                if (l.second[0] > r.second[0]) {
                    return true;
                } else {
                    if (l.second[0] < r.second[0]) {
                        return false; 
                    } 

                    if (l.second[1] > r.second[1]) {
                        return true;
                    }
                }

                return false;
            }
        };

        struct NewCacheCompare2 : public std::binary_function<NEWCACHE_PAIR, NEWCACHE_PAIR, bool> {
            public:
                NewCacheCompare2(int type) : m_type(type) {
                }

                bool operator()(const NEWCACHE_PAIR& l, const NEWCACHE_PAIR& r) {
                    if (m_type == EVENT_CLICK) {
                        if (l.second[0] > r.second[0]) {
                            return true;
                        }

                        return false;
                    }

                    if (m_type == EVENT_IMP) {
                        if (l.second[1] > r.second[1]) {
                            return true;
                        }

                        return false;
                    }

                    return false;
                }
            public:
                int m_type;
        };

        using NEWCACHE = std::map<int, std::unordered_map<std::string, std::array<double, 2>>>;

        int32_t GetUfsWeightedTag(std::shared_ptr<WU>& out, const std::string& version, const std::string& uid, double decayRate, int32_t decayPeriod);
        int32_t SetUfsWeightedTag(const std::string& version, const std::string& uid, const std::shared_ptr<WU>& out,
                const std::string& app, const std::string& configId, uint32_t tagNumLimit,
                const std::unordered_set<std::string>& blackWords);

        int32_t GetUfsWeightedCategory(std::shared_ptr<WU>& out, const std::string& version, const std::string& uid, double decayRate, int32_t decayPeriod);
        int32_t SetUfsWeightedCategory(const std::string& version, const std::string& uid, const std::shared_ptr<WU>& out, const std::string& app, const std::string& configId);

        int32_t GetTagStats(std::shared_ptr<VS>& out, const std::string& uid);
        int32_t SetTagStats(const std::string& uid, const std::shared_ptr<VS>& out);

        int32_t GetCategoryStats(std::shared_ptr<VS>& out, const std::string& uid);
        int32_t SetCategoryStats(const std::string& uid, const std::shared_ptr<VS>& out);

        int32_t GetCategoryClickAndImp(std::shared_ptr<MCG>& out, const std::string& version, const std::string& uid, double decayRate, int32_t decayPeriod);
        int32_t MergeCategoryClickOrImp(const std::string& type, const std::string& version, const std::string& uid, double decayRate, int32_t decayPeriod, int32_t eventTime, std::map<std::string, double>& categoryMerged);
        int32_t SetCategoryClickOrImp(const std::string& type, const std::string& version, const std::string& uid, std::shared_ptr<MCG>& input);

        void ActivateSync()
        {
            this->ssdb_->Start();
        }

    public:
        //using NEWCACHE_PAIR = std::pair<std::string, std::array<double, 2>>;
        //using NEWCACHE = std::map<int, std::unordered_map<std::string, std::array<double, 2>>>;
        int SerializeCacheClickAndImpJson(std::vector<std::string>& out, std::shared_ptr<NEWCACHE>& input);
        int SerializeCacheClickAndImpJson(int type, const std::string& uid, std::vector<std::string>& out, std::shared_ptr<NEWCACHE>& input);
        int SetCacheClickAndImp(std::shared_ptr<NEWCACHE>& out, const std::string& uid, double decayRate, int decayPeriod);
        int SetCacheClickAndImp(int type, std::shared_ptr<NEWCACHE>& out, const std::string& uid, double decayRate, int decayPeriod);
        int GetCacheClickAndImp(std::shared_ptr<NEWCACHE>& out, const std::string& uid, double decayRate, int decayPeriod);
        int GetCacheClickAndImp(int type, std::shared_ptr<NEWCACHE>& out, const std::string& uid, double decayRate, int decayPeriod);
        int MergeNewClickOrImp(int type, const std::string& uid, const std::string& content_id, std::tuple<std::string, std::multimap<std::string, std::string>, std::unordered_set<std::string>>& elem, double decayRate, int decayPeriod, int eventTime);

    protected:
        using DocumentType = rapidjson::GenericDocument<rapidjson::UTF8<>, rapidjson::MemoryPoolAllocator<>, rapidjson::MemoryPoolAllocator<>>;
        int ParseJsonsClickAndImp(std::shared_ptr<NEWCACHE>& out, std::vector<std::string>& jsons, double decayRate, int decayPeriod);
        int ParseJsonsClickAndImp(int type, std::shared_ptr<NEWCACHE>& out, std::vector<std::string>& jsons, double decayRate, int decayPeriod);
        int ParseCacheFromSsdb(const DocumentType& d, std::unordered_map<std::string, std::array<double,2>>& out, double decayFactor, bool tsFlag=false);
        int ParseCacheFromSsdb(int type, DocumentType& d, std::unordered_map<std::string, std::array<double,2>>& out, double decayFactor, std::unordered_map<std::string, std::array<double, 2>>* outOfImp = nullptr);


    private:
        //typedef rapidjson::GenericDocument<rapidjson::UTF8<>, rapidjson::MemoryPoolAllocator<>, rapidjson::MemoryPoolAllocator<>> DocumentType;
        int32_t SerializeStatsJson(std::shared_ptr<std::string>& out, const std::shared_ptr<VS>& stats);
        int32_t SerializeUfsJson(std::shared_ptr<std::string>& out, std::vector<std::pair<std::string, double> >& wtags,
                const std::string& app, const std::string& configId);
        int32_t SerializeCategoryClickAndImpJson(std::shared_ptr<std::string>& out, const std::shared_ptr<MCG>& input);

        int32_t JsonStats2Map(std::shared_ptr<VS>& statsMap, const std::string& json);
        static int32_t JsonWeightArray2Map(std::shared_ptr<WU>& out, const rapidjson::Value& obj, double decayFactor);
        int32_t JsonWeight2Map(std::shared_ptr<WU>& out, const std::string& json,
                double decayRate, int32_t decayPeriod);
        int32_t JsonCategoryClickAndImp2Map(std::shared_ptr<MCG>& out, const std::string& json,
                double decayRate, int32_t decayPeriod);

        static double GetDecayFactor(int32_t current, int32_t createTime, double rate, int32_t period);
        static bool IsDecimal(const std::string& word);

        std::string GetUfsHashName(const std::string& type, const std::string& version);
        std::string GetStatsHashName(const std::string& type);
        std::string GetCategoryClickAndImpHashName(const std::string& version);

        static std::vector<std::pair<std::string, double> >
            SortMapByWeight(const std::shared_ptr<WU>& merged, const std::unordered_set<std::string>& blackWords, uint32_t tagNumLimit);

        static bool WeightCmp(const std::pair<std::string, double>& a, const std::pair<std::string, double>& b)
        {
            return b.second < a.second;
        }
        
    private:
        uint32_t nap_ = 50*1000;
        size_t cacheSize_;

        const std::string hashCategory_;
        const std::string hashTag_;
        const std::string hashStatsCategory_;
        const std::string hashStatsTag_;
        const std::string hashCategoryClickAndImp_;
        /*
        typedef std::map<std::string, double> WU;// tag as key, weight as value
        typedef std::map<std::string, std::pair<double, double>> MCG;//mapCategoryClickAndImp, <category,<click,imp>>
        typedef std::map<std::string, std::pair<uint32_t, double>> VS;// version as key
         * */

        LinkedKVCache<std::string, std::shared_ptr<WU>> tagCache_;
        LinkedKVCache<std::string, std::shared_ptr<WU>> categoryCache_;
        LinkedKVCache<std::string, std::shared_ptr<VS>> tagStatsCache_;
        LinkedKVCache<std::string, std::shared_ptr<VS>> categoryStatsCache_;
        LinkedKVCache<std::string, std::shared_ptr<MCG>> categoryClickAndImpCache_;
        
        /*
         ssdb format:
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

        cache format:
        LinkKVCache<std::string, std::shared_ptr<VALUE>>;
        the key is the uid, VAULE is kinds of publisher, topics, keywords and so on
        key - 01011806162319325201000275201304
        value - key-value again 
            the key is a type like: PUBLISHER, TOPICS, KEYWORD
            the value is array which is two elems double
            so the value full foramt like: 
            std::map<TYPE, std::map<std::string. std::array<double, 2>>>
            01011806162319325201000275201304
        */

        //using NEWCACHE = std::map<int, std::map<std::string, std::array<double, 2>>>;
        //using NEWCACHE = std::map<int, std::unordered_map<std::string, std::array<double, 2>>>;
        LinkedKVCache<std::string, std::shared_ptr<NEWCACHE>>  m_cacheClickAndImp;

        SsdbIo* ssdb_;
        //SsdbIoSync* m_ssdbSync;
        
        const size_t jsonBufSize_ = 10*1024*1024;
        char* valueBuffer_;
        char* parseBuffer_;

};

#endif /* UFS_IN_MEM_H */
