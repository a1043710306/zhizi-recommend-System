#include <string>
#include <cstdio>
#include <time.h>
#include <vector>

#include "util.h"

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

int32_t GetTimestampFromDateTime(const std::string& date_time, int32_t &timestamp, int32_t timeZone)
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

std::string MakeJson(int32_t createTime, const std::string& app)
{
    return "{\"app\":\"" + app +"\",\"createTime\":\"" + std::to_string(createTime) + "\"}";
}

int main(int argc, char** argv)
{
    int timeZone = atoi(argv[1]);
    std::string ip = argv[2];
    int port = atoi(argv[3]);
    std::string hash = argv[4];
    inv::INV_Redis redis;
    redis.init(ip, port, 1000);
    redis.connect();
    char line[512];
    while (NULL != fgets(line, sizeof(line), stdin))
    {
        std::vector<std::string> fields = inv::INV_Util::sepstr<std::string>(line, "&", true);
        if (fields.size() < 3)
        {
            continue;
        }
        std::string uid = fields[2];
        std::string app = fields[1];
        std::string dt = fields[0];
        int32_t createTime;
        if (GetTimestampFromDateTime(dt, createTime, timeZone))
        {
            createTime = time(NULL);
        }
        std::string json = MakeJson(createTime, app);
        RedisHashSet(&redis, hash, uid, json);
    }
    return 0;
}

// gcc -oct -std=c++0x -I/usr/local/include/hiredis -I../../common/inutil/ src/create_time.cpp src/util.cpp ../../common/inutil/libinvutil.a /usr/local/lib/libhiredis.a /usr/local/gcc-4.8.5/lib64/libstdc++.a -pthread -lm
// cat 06.log | ./ct 8 172.31.8.20 8888 zhizi.ufs.newuser
