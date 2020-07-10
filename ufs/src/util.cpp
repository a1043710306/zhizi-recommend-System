#include <pthread.h>

#include <cassert>
#include "inv_md5.h"
#include "inv_log.h"
#include "util.h"

using namespace inv;
CFunctionGuard::CFunctionGuard(const string &sFunctionName) : m_sFunctionName(sFunctionName)
{
    FDLOG("guard") << pthread_self() << ": enter " << m_sFunctionName << endl;
    gettimeofday(&this->m_Ots, NULL);
    this->m_ddwEnterTime = this->m_Ots.tv_sec * m_Million + this->m_Ots.tv_usec;
}

CFunctionGuard::~CFunctionGuard()
{
    gettimeofday(&this->m_Ots, NULL);
    int64_t ddwCurrent = this->m_Ots.tv_sec * m_Million + this->m_Ots.tv_usec;
    FDLOG("guard") << pthread_self() << ": leave " << m_sFunctionName << ", time used: "<< ddwCurrent - this->m_ddwEnterTime <<" usec" << endl;
}

int32_t RedisHashMGet(std::vector<std::string>& res, inv::INV_Redis* redis, const std::string& hash, const std::vector<std::string>& key)
{
    FUNC_GUARD();
    int32_t tryNum = 3;
    // std::vector<std::string> out;
    while (tryNum--)
    {
        int ret = redis->hashget(hash, key, res);
        if (ret == 0 || ret == -6)
        {
            return 0;
        }
    }
    return -1;
}

int32_t RedisHashGet(std::string& res, inv::INV_Redis* redis, const std::string& hash, const std::string& key)
{
    FUNC_GUARD();
    int32_t tryNum = 3;
    std::vector<std::string> out;
    while (tryNum--)
    {
        int ret = redis->hashget(hash, (std::vector<std::string>(1, key)), out);
        if (ret == -6)
        {
            return 1;
        }
        if (ret == 0)
        {
            if (!out.empty())
            {
                res = *out.begin();
                return 0;
            }
            return 1;
        }
    }
    return -1;
}

int32_t RedisHashMSet(inv::INV_Redis* redis, const std::string& hash, const std::vector<std::string>& key, const std::vector<std::string> &value)
{
    FUNC_GUARD();
    int32_t tryNum = 3;
    while (tryNum--)
    {
        int ret = redis->hashset(hash, key, value);
        if (ret == 0)
        {
            return 0;
        }
    }
    return -1;
}

int32_t RedisHashSet(inv::INV_Redis* redis, const std::string& hash, const std::string& key, const std::string &value)
{
    FUNC_GUARD();
    int32_t tryNum = 3;
    while (tryNum--)
    {
        int ret = redis->hashset(hash, (std::vector<std::string>(1, key)), (std::vector<std::string>(1, value)));
        if (ret == 0)
        {
            return 0;
        }
    }
    return -1;
}

size_t GetIdxOfConns(const std::string& uid, size_t arrSize, const std::string& salt)
{
    assert(arrSize > 0);
    string md5 = inv::INV_MD5::md5bin(uid + salt);
    size_t idx = *((uint32_t*)(md5.c_str())) % arrSize;
    return idx;
}

//from internet
time_t time_to_epoch ( const struct tm *ltm, int utcdiff )
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

int32_t TimestampFromDateTime(int32_t &timestamp, const std::string& date_time, int32_t timeZone)
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
    timestamp = time_to_epoch(&dt, 0 - timeZone);
    // timestamp = mktime(&dt);
    return 0;
}

