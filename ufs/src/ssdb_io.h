#ifndef SSDB_IO_H_
#define SSDB_IO_H_ 

#include "util.h"
#include <memory>
#include<string>

class SsdbIo
{
    public:
        explicit SsdbIo(const std::vector<inv::INV_Redis*>& vecRedis, const std::vector<inv::INV_Redis*>& vecWRedis, int32_t cold);
        virtual ~SsdbIo();
        int32_t AddSyncTask(const std::string& hash, const std::string& uid, const std::shared_ptr<std::string>& json);//field as uid
        int32_t GetSsdbJson(std::string& json, const std::string& hash, const std::string& uid);
        int GetSsdbJsons(std::vector<std::string>& jsons, const std::string& uid, const std::vector<std::string>& fields);

    public:
        struct SyncTask
        {
            int32_t ts;
            std::string hash;
            std::string uid;
            std::shared_ptr<std::string> json;
        };

        struct TaskQueue
        {
            std::map<std::string, SyncTask> data;//hash + uid as key
            pthread_mutex_t lock;
        };

        struct ThreadArgs
        {
            inv::INV_Redis* conn;
            TaskQueue* q;
            size_t idx;
            int32_t cold;
        };
        void Start();
    private:
        static size_t DoSync(size_t idx, inv::INV_Redis* conn, const std::vector<SsdbIo::SyncTask>& vecTask,
                std::map<std::string, std::pair<std::vector<std::string>, std::vector<std::string>>>& groupTask);
        static void* WriteSsdb(void* arg);
    private:
        const static uint32_t nap_ = 500*1000;
        const size_t threshhold_;
        std::vector<inv::INV_Redis*> vecRedis_;
        std::vector<inv::INV_Redis*> vecWRedis_;
        std::vector<TaskQueue> vecSync_;
        std::vector<pthread_t> vecThread_;
        //multi thread
        const int32_t cold_;
};

#endif /* SYNC_SSDB_H */
