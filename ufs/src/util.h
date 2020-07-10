#ifndef UTIL_H
#define UTIL_H 

#include "inv_redis.h"
class CFunctionGuard
{
    public:
        explicit CFunctionGuard(const std::string &sFunctionName);
        ~CFunctionGuard();
    private:
        static const int64_t m_Million = 1000000;
        const std::string m_sFunctionName;
        struct timeval m_Ots;
        int64_t m_ddwEnterTime;
};
#define FUNC_GUARD()  CFunctionGuard FuncGuard(__FUNCTION__)

int32_t RedisHashGet(std::string& res, inv::INV_Redis* redis, const std::string& hash, const std::string& key);
int32_t RedisHashSet(inv::INV_Redis* redis, const std::string& hash, const std::string& key, const std::string &value);
int32_t RedisHashMSet(inv::INV_Redis* redis, const std::string& hash, const std::vector<std::string>& key, const std::vector<std::string>& value);
int32_t RedisHashMGet(std::vector<std::string>& res, inv::INV_Redis* redis, const std::string& hash, const std::vector<std::string>& key);
size_t GetIdxOfConns(const std::string& uid, size_t arrSize, const std::string& salt);

int32_t TimestampFromDateTime(int32_t &timestamp, const std::string& date_time, int32_t timeZone);
#endif /* UTIL_H */
