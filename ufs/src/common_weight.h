#ifndef COMMON_WEIGHT_H
#define COMMON_WEIGHT_H 
#include <string>
#include <map>
#include <unordered_set>
#include <memory>

#include "rapidjson/document.h"     // rapidjson's DOM-style API

#include "ssdb_io.h"
#include "linked_kv_cache.h"

class CommonWeight
{
    public:
        CommonWeight(size_t cacheSize, const std::string& hashName,
                const std::vector<inv::INV_Redis*>& vecRedis,
                const std::vector<inv::INV_Redis*>& vecWRedis, int32_t writeCold);
        virtual ~CommonWeight()
        {
            if (this->ssdb_ != NULL)
            {
                delete this->ssdb_;
            }
        };
        typedef std::map<std::string, double> WU;// tag as key, weight as value

        int32_t GetWeightedTag(std::shared_ptr<WU>& out, const std::string& version, const std::string& uid,
                double decayRate, int32_t decayPeriod);
        int32_t SetWeightedTag(const std::string& version, const std::string& uid, const std::shared_ptr<WU>& out,
                const std::string& app, const std::string& configId, uint32_t tagNumLimit,
                const std::unordered_set<std::string>& blackWords);

        void ActivateSync()
        {
            this->ssdb_->Start();
        }
        
        static int32_t MergeWeightMaps(std::shared_ptr<std::map<std::string, double>>& m1,
                const std::map<std::string, double>& m2, int sign);
    private:
        typedef rapidjson::GenericDocument<rapidjson::UTF8<>, rapidjson::MemoryPoolAllocator<>, rapidjson::MemoryPoolAllocator<>> DocumentType;
        int32_t SerializeJson(std::shared_ptr<std::string>& out, std::vector<std::pair<std::string, double> >& wtags,
                const std::string& app, const std::string& configId);

        static int32_t JsonWeightArray2Map(std::shared_ptr<WU>& out, const rapidjson::Value& obj, double decayFactor);
        int32_t JsonWeight2Map(std::shared_ptr<WU>& out, const std::string& json,
                double decayRate, int32_t decayPeriod);

        static double GetDecayFactor(int32_t current, int32_t createTime, double rate, int32_t period);
        static bool IsDecimal(const std::string& word);

        std::string GetHashName(const std::string& type, const std::string& version);

        static std::vector<std::pair<std::string, double> >
            SortMapByWeight(const std::shared_ptr<WU>& merged, const std::unordered_set<std::string>& blackWords,
                    uint32_t tagNumLimit);

        static bool WeightCmp(const std::pair<std::string, double>& a, const std::pair<std::string, double>& b)
        {
            return b.second < a.second;
        }
    private:
        uint32_t nap_ = 50*1000;
        size_t cacheSize_;

        const std::string hash_;

        LinkedKVCache<std::string, std::shared_ptr<WU>> cache_;

        const size_t jsonBufSize_ = 10*1024*1024;
        char* valueBuffer_;
        char* parseBuffer_;

        SsdbIo* ssdb_;
};
#endif /* COMMON_WEIGHT_H */
