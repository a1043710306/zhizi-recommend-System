#ifndef INFO_SIGNAL_CACHE_H
#define INFO_SIGNAL_CACHE_H 

#include <string>
#include <map>
#include<pthread.h>

#include "priority_kv_cache.h"
#include "db.h"

class InfoSignalCache
{
    public:
        InfoSignalCache(TSignal* signal, size_t capacity, int timeZone);
        virtual ~InfoSignalCache()
        {
            pthread_mutex_destroy(&this->conn_lock_);
        };
        InfoSignalCache(InfoSignalCache const &) = delete;
        InfoSignalCache& operator=(InfoSignalCache const &) = delete;

    public:
        int GetTags(std::map<std::string, std::map<std::string, double>>& out, const std::string& infoId);
        int GetCategories(std::map<std::string, std::map<std::string, double>>& out, const std::string& infoId);
        int GetTitleTags(std::map<std::string, std::map<std::string, double>>& out, const std::string& infoId);
    private:
        static int32_t GetWeightedTags(std::map<std::string, std::map<std::string, double> >& ret,
                const std::string& json);
        int GetSignal(inv::INV_Mysql::MysqlData& records, const std::string& infoId);
    private:
        TSignal* signal_;
        pthread_mutex_t conn_lock_;

        PriorityKVCache<std::string, std::map<std::string, std::map<std::string, double>>> cache_;
        int timeZone_;
};
#endif /* INFO_SIGNAL_CACHE_H */
