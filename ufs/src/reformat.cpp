#include <cstdio>
#include <cstdlib>
#include <unordered_set>
#include <cassert>
#include <queue>

#include "util/inv_argv.h"
#include "util/inv_config.h"
#include "inv_log.h"
#include "report.h"

#include "ufs_logic.h"
#include "kafka_consumer.h"
#include "util.h"

#define INIT_INV_LOGGER(APP, SERVER) do{\
    InvRollLogger::getInstance()->setLogInfo(APP, SERVER, "./");\
    InvRollLogger::getInstance()->logger()->setLogLevel("DEBUG");\
    InvRollLogger::getInstance()->sync(false);\
    InvTimeLogger::getInstance()->setLogInfo("", APP, SERVER, "./");\
}while(0);

using namespace inv;
using namespace inv::monitor;

struct setting
{
    std::string hashName;

    std::vector<std::pair<std::string, uint16_t> > brokers;
    std::string topic;
    std::string group;

    std::string dbHost;
    int dbPort;
    std::string dbUser;
    std::string dbPass;
    std::string dbCharset;
    std::string dbName;

    struct Addr
    {
        std::string host;
        int port;
        int timeout;
    };
    std::vector<Addr> addrs;

    std::string typicalVersion;
    int32_t cacheSize;
};

void LoadSetting(setting *pcf, inv::INV_Config* pconfig)
{
    {
        std::string strSsdbConn = pconfig->get("/main/[ssdb_conn]");
        std::vector<std::string> fields = inv::INV_Util::sepstr<string>(strSsdbConn, ",", true);
        assert(!fields.empty());
        for (size_t i = 0; i < fields.size(); ++i)
        {
            std::vector<std::string> hpt = inv::INV_Util::sepstr<string>(fields[i], ":", true);
            assert(hpt.size() == 3);
            std::string host = hpt[0];
            int port = atoi(hpt[1].c_str());
            assert(port > 0);
            int timeout = atoi(hpt[2].c_str());
            assert(timeout > 0);
            pcf->addrs.push_back({host, port, timeout});
        }
    }

    pcf->typicalVersion = pconfig->get("/main/[typical_version]");
    if (pcf->typicalVersion.empty())
    {
        fprintf(stderr, "/main/[typical_version] empty\n");
        exit(-1);
    }

    std::string cacheSize = pconfig->get("/main/[cache_size]");
    if (cacheSize.empty())
    {
        fprintf(stderr, "/main/[cache_size] empty\n");
        exit(-1);
    }
    pcf->cacheSize = atoi(cacheSize.c_str());
    assert(pcf->cacheSize > 0);

    pcf->hashName = pconfig->get("/main/[hash]");
    if (pcf->hashName.empty())
    {
        fprintf(stderr, "/main/[hash] empty\n");
        exit(-1);
    }

    std::vector<std::string> kafkaServers = pconfig->getDomainLine("/main/kafka/brokers");
    for(size_t i = 0; i < kafkaServers.size(); i++)
    {
        vector<string> kv = inv::INV_Util::sepstr<string>(kafkaServers[i], "=");

        vector<string> servers = inv::INV_Util::sepstr<string>(kv[1], ":");
        string ip = servers[0];
        int port = inv::INV_Util::strto<int>(servers[1]);
        pcf->brokers.push_back(std::make_pair(ip, port));
    }
    pcf->topic = pconfig->get("/main/kafka/[topic]");
    if (pcf->topic.empty())
    {
        fprintf(stderr, "/main/kafka/[topic] empty\n");
        exit(-1);
    }
    pcf->group = pconfig->get("/main/kafka/[group]");
    if (pcf->group.empty())
    {
        fprintf(stderr, "/main/kafka/[group] empty\n");
        exit(-1);
    }

    pcf->dbHost= pconfig->get("/main/mysql/[host]");
    if (pcf->dbHost.empty())
    {
        fprintf(stderr, "/main/mysql/[host] empty\n");
        exit(-1);
    }
    std::string port = pconfig->get("/main/mysql/[port]");
    pcf->dbPort = atoi(port.c_str());
    if (pcf->dbPort <= 0)
    {
        fprintf(stderr, "/main/mysql/[port] error\n");
        exit(-1);
    }

    pcf->dbUser = pconfig->get("/main/mysql/[user]");
    if (pcf->dbUser.empty())
    {
        fprintf(stderr, "/main/mysql/[user] empty\n");
        exit(-1);
    }
    pcf->dbPass = pconfig->get("/main/mysql/[pass]");
    if (pcf->dbPass.empty())
    {
        fprintf(stderr, "/main/mysql/[pass] empty\n");
        exit(-1);
    }
    pcf->dbName = pconfig->get("/main/mysql/[db]");
    if (pcf->dbName.empty())
    {
        fprintf(stderr, "/main/mysql/[db] empty\n");
        exit(-1);
    }
    pcf->dbCharset = pconfig->get("/main/mysql/[charset]");
    if (pcf->dbCharset.empty())
    {
        fprintf(stderr, "/main/mysql/[charset] empty\n");
        exit(-1);
    }
}

struct QueueOfThread
{
    std::queue<std::string> queue;
    pthread_mutex_t lock;
};

struct thread_param
{
    // inv::INV_Redis* redis;
    // inv::INV_Redis* coolpad_redis;
    inv::INV_Mysql* conn;

    pthread_t thread_id;
    int thread_no;

    std::queue<std::string>* q;
    pthread_mutex_t* lock;

    // int last_update;
    int timezone;

    setting cf;
    std::shared_ptr<KVCache<std::string, std::vector<std::string>>> global_cache;
};

bool gRun;

static void Stop (int sig)
{
    gRun = false;
    fclose(stdin);
}

void* consume_msg(void* arg)
{
    thread_param* param = (thread_param*)arg;

    std::shared_ptr<inv::INV_Mysql> sp_conn(param->conn);

    std::vector<inv::INV_Redis*> conns;
    for (const auto& it: param->cf.addrs)
    {
        inv::INV_Redis* redis = new inv::INV_Redis;
        redis->init(it.host, it.port, it.timeout);
        redis->connect();
        conns.push_back(redis);
    }
    SfuLogic sfu(sp_conn, conns, param->cf.hashName, param->timezone,
            param->global_cache, param->cf.typicalVersion);

    while (gRun)
    {
        std::vector<std::string> tmp;
        const size_t tmpCapacity = 1000;
        pthread_mutex_lock(param->lock);
        size_t qsize = param->q->size();
        while (true)
        {
            if (tmp.size() >= tmpCapacity)
            {
                break;
            }
            if (param->q->size() <= 0)
            {
                break;
            }
            std::string msg = param->q->front();
            param->q->pop();
            tmp.push_back(msg);
        }
        size_t aq = param->q->size();
        pthread_mutex_unlock(param->lock);

        // if (qsize > 0)
        // {
        //     msg = param->q->front();
        //     param->q->pop();
        //     aq = param->q->size();
        //     pthread_mutex_unlock(param->lock);
        // }
        // else
        // {
        //     pthread_mutex_unlock(param->lock);
        //     sleep(1);
        //     continue;
        // }
        // if (msg.empty())
        // {
        //     // sleep(1);
        //     continue;
        // }
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" qsize from " << qsize << " to " << aq <<endl);
        for (const auto& msg: tmp)
        {
            ReportIncr("zhizi.ufs.category." + param->cf.topic);
            if (param->cf.topic == "click-reformat")
            {
                TIME_LABEL(1);
                int ret = sfu.UpdateClickTimeOfCategory(msg);
                ReportIncr("zhizi.ufs.category.UpdateClickTimeOfCategory", TIME_DIFF(1));
                if (ret)
                {
                    TLOGINFO(__FILE__<<"-"<<__LINE__<<" UpdateClickTimeOfCategory error"<<endl);
                }
            }
            else if (param->cf.topic == "request-reformat")
            {
                TIME_LABEL(1);
                int ret = sfu.UpdateImpressionTimeOfCategory(msg);
                ReportIncr("zhizi.ufs.category.UpdateImpressionTimeOfCategory", TIME_DIFF(1));
                if (ret)
                {
                    TLOGINFO(__FILE__<<"-"<<__LINE__<<" UpdateImpressionTimeOfCategory error"<<endl);
                }
            }
            else
            {
                assert(0);
            }
        }
        if (tmp.size() < tmpCapacity)
        {
            TLOGINFO(__FILE__<<"-"<<__LINE__<<" will sleep, drain queue: " << param->thread_no <<endl);
            sleep(1);
        }
    }
    while (true)
    {
        pthread_mutex_lock(param->lock);
        std::string msg;
        size_t qsize = param->q->size();
        if (qsize > 0)
        {
            msg = param->q->front();
            param->q->pop();
            pthread_mutex_unlock(param->lock);
        }
        else
        {
            pthread_mutex_unlock(param->lock);
            break;
        }

        TLOGINFO(__FILE__<<"-"<<__LINE__<<" exiting queue size: " << qsize << endl);
        ReportIncr("zhizi.ufs.category." + param->cf.topic);

        // if (msg.empty())
        // {
        //     continue;
        // }

        if (param->cf.topic == "click-reformat")
        {
            TIME_LABEL(1);
            int ret = sfu.UpdateClickTimeOfCategory(msg);
            ReportIncr("zhizi.ufs.category.UpdateClickTimeOfCategory", TIME_DIFF(1));
            if (ret)
            {
                TLOGINFO(__FILE__<<"-"<<__LINE__<<" UpdateClickTimeOfCategory error"<<endl);
            }
        }
        else if (param->cf.topic == "request-reformat")
        {
            TIME_LABEL(1);
            int ret = sfu.UpdateImpressionTimeOfCategory(msg);
            ReportIncr("zhizi.ufs.category.UpdateImpressionTimeOfCategory", TIME_DIFF(1));
            if (ret)
            {
                TLOGINFO(__FILE__<<"-"<<__LINE__<<" UpdateImpressionTimeOfCategory error"<<endl);
            }
        }
        else
        {
            assert(0);
        }
    }
    TLOGINFO(__FILE__<<"-"<<__LINE__<<": will exit thread "<< param->thread_no <<endl);
    return NULL;
}

int32_t HandoutMsg(const std::vector<QueueOfThread*> allQueue, const std::string& msg)
{
    rapidjson::Document d;
    d.Parse(msg.c_str());

    if (d.HasParseError())
    {
        return -1;
    }

    if (!d.IsObject())
    {
        return -1;
    }
    if (d.FindMember("uid") == d.MemberEnd())
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" wrong record, no uid: " <<endl);
        return -1;
    }
    const std::string uid = d.FindMember("uid")->value.GetString();
    size_t idx = GetIdxOfConns(uid, allQueue.size(), "salt");// same uid go to same queue, same thread
    QueueOfThread *ptr = allQueue[idx];
    pthread_mutex_lock(&(ptr->lock));
    ptr->queue.push(msg);
    size_t qsize = ptr->queue.size();
    pthread_mutex_unlock(&(ptr->lock));
    if (qsize > 10000)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" idx: " << idx << " queue size: " << qsize <<endl);
        sleep(1);
    }
    return 0;
}

int main(int argc, char** argv)
{
    inv::INV_Argv ag;
    ag.decode(argc, argv);
    string confPath = ag.getValue("config");
    if (confPath.empty())
    {
        fprintf(stderr, "usage: %s --thread_num=8 --timezone=8 --config=config.conf\n", argv[0]);
        exit(1);
    }

    string tz = ag.getValue("timezone");
    if (tz.empty())
    {
        fprintf(stderr, "usage: %s --thread_num=8 --timezone=8 --config=config.conf\n", argv[0]);
        exit(1);
    }
    int timezone = atoi(tz.c_str());
    assert(timezone >= 0);

    string tn = ag.getValue("thread_num");
    if (tn.empty())
    {
        fprintf(stderr, "usage: %s --thread_num=8 --timezone=8 --config=config.conf\n", argv[0]);
        exit(1);
    }
    int threadNum  = atoi(tn.c_str());
    assert(threadNum > 0);

    inv::INV_Config config;
    config.parseFile(confPath);

    setting cf;
    LoadSetting(&cf, &config);
    if (cf.topic == "click-reformat")
    {
        INIT_INV_LOGGER("INV", "ClickReformat");
    }
    else if (cf.topic == "request-reformat")
    {
        INIT_INV_LOGGER("INV", "ImpressionReformat");
    }
    else
    {
        assert(0);
    }

    KafkaConsumer consumer(cf.brokers, cf.group, cf.topic);
    if (consumer.Init() != 0)
    {
        fprintf(stderr, "kafka client init error: %s\n", consumer.GetLastErrMsg().c_str());
        exit(1);
    }

    gRun = true;
    signal(SIGINT, Stop);
    signal(SIGTERM, Stop);
    signal(SIGIO, SIG_IGN);

    std::vector<QueueOfThread*> allQueue;
    for (int i = 0; i < threadNum; ++i)
    {
        QueueOfThread* t = new QueueOfThread;
        assert(pthread_mutex_init(&(t->lock), NULL) == 0);
        allQueue.push_back(t);
    }

    std::vector<pthread_t> vecThread;
    std::shared_ptr<KVCache<std::string, std::vector<std::string>>> infoCategoryCache(new KVCache<std::string, std::vector<std::string>>(cf.cacheSize));
    for (int i = 0; i < threadNum; ++i)
    {
        inv::INV_Mysql* conn = new inv::INV_Mysql;
        conn->init(cf.dbHost, cf.dbUser, cf.dbPass, cf.dbName, cf.dbCharset, cf.dbPort);

        thread_param* param = new thread_param;
        param->conn = conn;

        param->thread_no = i;
        param->q = &(allQueue[i]->queue);
        param->lock = &(allQueue[i]->lock);

        param->timezone = timezone;
        param->cf = cf;
        param->global_cache = infoCategoryCache;

        int rc = pthread_create(&param->thread_id, NULL, &consume_msg, param);
        if (rc != 0)
        {
            fprintf(stderr, "pthread_create fail: %d\n", i);
            return -1;
        }
        vecThread.push_back(param->thread_id);
    }

    while (gRun)
    {
        std::string msg;
        if (consumer.FetchMsg(msg))
        {
            TLOGINFO(__FILE__<<"-"<<__LINE__<<" FetchMsg error: "<<consumer.GetLastErrMsg()<<endl);
            // sleep(cf.pollInterval);
            continue;
        }
        HandoutMsg(allQueue, msg);
    }

    TLOGINFO(__FILE__<<"-"<<__LINE__<<": exit by kill"<<endl);
    for (const auto& i: vecThread)
    {
        pthread_join(i, NULL);
    }
    for (const auto& i: allQueue)
    {
        pthread_mutex_destroy(&(i->lock));
    }
    return 0;
}

