/*************************************************************************
	> File Name: src/ssdb_io_sync.cpp
	> Author: xyz
	> Mail: xiao13149920@foxmail.com 
	> Created Time: Fri 29 Jun 2018 11:47:35 AM CST
 ************************************************************************/
#include"report.h"
#include"ssdb_io_sync.h"
#include<unistd.h>
#include"const_str.h"

using namespace inv;
using namespace inv::monitor;

#define X(a, b) [a]=b,
const char* SsdbIoSyncTableStr[] = {
    SSDB_IO_SYNC_TABLE
};
#undef X

extern bool gRun;
std::vector<std::string> g_constFields; // {"content_publisher", "content_keywords", "extend_topics"}
std::vector<std::string> g_constFieldsSet; // {"content_publisher", "content_keywords", "extend_topics", "update"}
std::vector<std::string> g_constFieldsImp; // {"content_publisher_imp", "content_keywords_imp", "extend_topicsi_imp"}
std::vector<std::string> g_constFieldsSetImp; // {"content_publisher_imp", "content_keywords_imp", "extend_topicsi_imp" "update"}
std::vector<std::string> g_constFieldsClickAndImp; // {"content_publisher", "content_keywords", "extend_topics", "content_publisher_imp", "content_keywords_imp", "extend_topicsi_imp"}
std::vector<std::string> g_constFieldsSetClickAndImp; // {"content_publisher", "content_keywords", "extend_topics", "content_publisher_imp", "content_keywords_imp", "extend_topicsi_imp", "update"}
moodycamel::ConcurrentQueue<std::pair<std::string, std::vector<std::string>>> g_concurrentQueue;

SsdbIoSync::SsdbIoSync(inv::INV_Redis* redis, int type) : 
    m_redis(redis),
    m_type(type) {
    for (int i=0; i< TQUERY_FIELDS_SIZE; ++i) {
        g_constFields.emplace_back(TQueryTable[i].c_str());
        g_constFieldsSet.emplace_back(TQueryTable[i].c_str());
        g_constFieldsClickAndImp.emplace_back(TQueryTable[i].c_str());
        g_constFieldsSetClickAndImp.emplace_back(TQueryTable[i].c_str());
    }

    for (int i=0; i< TQUERY_FIELDS_SIZE; ++i) {
        g_constFieldsImp.emplace_back(TQueryTable[i + TQUERY_FIELDS_SIZE].c_str());
        g_constFieldsSetImp.emplace_back(TQueryTable[i + TQUERY_FIELDS_SIZE].c_str());
        g_constFieldsClickAndImp.emplace_back(TQueryTable[i + TQUERY_FIELDS_SIZE].c_str());
        g_constFieldsSetClickAndImp.emplace_back(TQueryTable[i + TQUERY_FIELDS_SIZE].c_str());
    }
    g_constFieldsSet.emplace_back("update");
    g_constFieldsSetImp.emplace_back("update");
    g_constFieldsSetClickAndImp.emplace_back("update");
}

SsdbIoSync::SsdbIoSync(void) : m_redis(nullptr) {
    for (int i=0; i< TQUERY_FIELDS_SIZE; ++i) {
        g_constFields.emplace_back(TQueryTable[i].c_str());
        g_constFieldsSet.emplace_back(TQueryTable[i].c_str());
        g_constFieldsClickAndImp.emplace_back(TQueryTable[i].c_str());
        g_constFieldsSetClickAndImp.emplace_back(TQueryTable[i].c_str());
    }

    for (int i=0; i< TQUERY_FIELDS_SIZE; ++i) {
        g_constFieldsImp.emplace_back(TQueryTable[i + TQUERY_FIELDS_SIZE].c_str());
        g_constFieldsSetImp.emplace_back(TQueryTable[i + TQUERY_FIELDS_SIZE].c_str());
        g_constFieldsClickAndImp.emplace_back(TQueryTable[i + TQUERY_FIELDS_SIZE].c_str());
        g_constFieldsSetClickAndImp.emplace_back(TQueryTable[i + TQUERY_FIELDS_SIZE].c_str());
    }
    g_constFieldsSet.emplace_back("update");
    g_constFieldsSetImp.emplace_back("update");
    g_constFieldsSetClickAndImp.emplace_back("update");
    m_type = EVENT_CLICK;
}

SsdbIoSync::~SsdbIoSync()
{
}
#if 0
void SsdbIoSync::Start(void)
{
    while (gRun) {
        //using SsdbIoSyncItem = std::pair<std::string, std::vector<std::string>>;
        SsdbIoSyncItem item;
        int ret = 0;
        while (g_concurrentQueue.try_dequeue(item)) {
            item.second.emplace_back(std::to_string(time(NULL)));
            ret = RedisHashMSet(m_redis, item.first, g_constFieldsSet, item.second);
            FDLOG(SsdbIoSyncTableStr[SSDB_IO_SYNC_FILE_NAME])<<EventTypeTableStr[m_type][0].c_str()<<" - "<<SsdbIoSyncTableStr[SSDB_IO_SYNC_UID]<< item.first<<", ret="<<ret<<std::endl;

            TLOGINFO(__FILE__<<"-"<<__LINE__<<" sync task batch" << ", ret: " << ret << ", uid: " << item.first <<endl);

            ReportIncr("zhizi.ufs.write.ssdb." + item.first, 1);
            if (ret != 0)
            {
                ReportIncr("zhizi.ufs.write.ssdb.fail|" + item.first);
            }
            //usleep(20);
        }

        sleep(1);
    }
}
#endif

void SsdbIoSync::Start(void)
{
    int ret = 0;
    if (m_type == EVENT_CLICK) {
        while (gRun) {
            //using SsdbIoSyncItem = std::pair<std::string, std::vector<std::string>>;
            SsdbIoSyncItem item;
            while (g_concurrentQueue.try_dequeue(item)) {
                size_t size = item.second.size();
                if (size == TQUERY_OVERFLOW) {
                    item.second.emplace_back(std::to_string(time(NULL)));
                    ret = RedisHashMSet(m_redis, item.first, g_constFieldsSetClickAndImp, item.second);
#if 0
                    FDLOG(SsdbIoSyncTableStr[SSDB_IO_SYNC_FILE_NAME])<<EventTypeTableStr[m_type][0].c_str()<<" - "<<SsdbIoSyncTableStr[SSDB_IO_SYNC_UID]<< item.first
                        <<"\n\n==[0]"<<item.second[0]
                        <<"\n\n==[1]"<<item.second[1]
                        <<"\n\n==[2]"<<item.second[2]
                        <<"\n\n==[3]"<<item.second[3]
                        <<"\n\n==[4]"<<item.second[4]
                        <<"\n\n==[5]"<<item.second[5]
                        <<std::endl;
#endif
                } else if (size == TQUERY_FIELDS_SIZE) {
                    item.second.emplace_back(std::to_string(time(NULL)));
                    ret = RedisHashMSet(m_redis, item.first, g_constFieldsSet, item.second);
#if 0
                    FDLOG(SsdbIoSyncTableStr[SSDB_IO_SYNC_FILE_NAME])<<EventTypeTableStr[m_type][0].c_str()<<" - "<<SsdbIoSyncTableStr[SSDB_IO_SYNC_UID]<< item.first
                        <<"\n\n==[0]"<<item.second[0]
                        <<"\n\n==[1]"<<item.second[1]
                        <<"\n\n==[2]"<<item.second[2]
                        <<std::endl;
#endif
                }
                FDLOG(SsdbIoSyncTableStr[SSDB_IO_SYNC_FILE_NAME])<<EventTypeTableStr[m_type][0].c_str()<<" - "<<SsdbIoSyncTableStr[SSDB_IO_SYNC_UID]<< item.first<<", ret="<<ret<<std::endl;

                TLOGINFO(__FILE__<<"-"<<__LINE__<<" sync task batch" << ", ret: " << ret << ", uid: " << item.first <<endl);

                ReportIncr("zhizi.ufs.write.ssdb." + item.first, 1);
                if (ret != 0)
                {
                    ReportIncr("zhizi.ufs.write.ssdb.fail|" + item.first);
                }
                //usleep(20);
            }

            sleep(1);
        }
    } 

    if (m_type == EVENT_IMP) {
        while (gRun) {
            //using SsdbIoSyncItem = std::pair<std::string, std::vector<std::string>>;
            SsdbIoSyncItem item;
            while (g_concurrentQueue.try_dequeue(item)) {
                //item.second.emplace_back(std::to_string(time(NULL)));
                ret = RedisHashMSet(m_redis, item.first, g_constFieldsImp, item.second);

#if 0
                FDLOG(SsdbIoSyncTableStr[SSDB_IO_SYNC_FILE_NAME])<<EventTypeTableStr[m_type][0].c_str()<<" - "<<SsdbIoSyncTableStr[SSDB_IO_SYNC_UID]<< item.first
                    <<"\n\n==[0]"<<item.second[0]
                    <<"\n\n==[1]"<<item.second[1]
                    <<"\n\n==[2]"<<item.second[2]
                    <<std::endl;
#endif
                FDLOG(SsdbIoSyncTableStr[SSDB_IO_SYNC_FILE_NAME])<<EventTypeTableStr[m_type][0].c_str()<<" - "<<SsdbIoSyncTableStr[SSDB_IO_SYNC_UID]<< item.first<<", ret="<<ret<<std::endl;

                TLOGINFO(__FILE__<<"-"<<__LINE__<<" sync task batch" << ", ret: " << ret << ", uid: " << item.first <<endl);

                ReportIncr("zhizi.ufs.write.ssdb." + item.first, 1);
                if (ret != 0)
                {
                    ReportIncr("zhizi.ufs.write.ssdb.fail|" + item.first);
                }
                //usleep(20);
            }

            sleep(1);
        }
    } 

}
