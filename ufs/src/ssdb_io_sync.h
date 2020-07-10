/*************************************************************************
	> File Name: src/ssdb_io_sync.h
	> Author: xyz 
	> Mail: xiao13149920@foxmail.com 
	> Created Time: Fri 29 Jun 2018 11:47:15 AM CST
 ************************************************************************/

#ifndef SSDB_IO_SYNC_H_
#define SSDB_IO_SYNC_H_

#include"util.h"
#include<memory>
#include<string>
#include<vector>
#include<map>
#include<thread>
#include"db.h"
#include"concurrentqueue/concurrentqueue.h"

extern moodycamel::ConcurrentQueue<std::pair<string, std::vector<std::string>>> g_concurrentQueue;
extern std::vector<std::string> g_constFields;
extern std::vector<std::string> g_constFieldsSet;
extern std::vector<std::string> g_constFieldsImp;
extern std::vector<std::string> g_constFieldsSetImp;
extern std::vector<std::string> g_constFieldsClickAndImp;
extern std::vector<std::string> g_constFieldsSetClickAndImp;

using SsdbIoSyncItem = std::pair<std::string, std::vector<std::string>>;

class SsdbIoSync
{
    public:
        explicit SsdbIoSync(inv::INV_Redis* redis, int type=EVENT_CLICK);
        explicit SsdbIoSync(void);
        virtual ~SsdbIoSync();

    public:
        void Start(void);

    protected:
        void WriteSsdb(void);

    private:
        inv::INV_Redis* m_redis;
        int m_type;
};

#define SSDB_IO_SYNC_TABLE \
    X(SSDB_IO_SYNC_FILE_NAME, "sync_uid") \
X(SSDB_IO_SYNC_UID, "uid|")

#define X(a, b) a,
enum ESSDB_IO_SYNC_TABLE {
    SSDB_IO_SYNC_TABLE
};
#undef X

extern const char* SsdbIoSyncTableStr[];

#endif /* */
