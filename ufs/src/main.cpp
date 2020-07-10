#include <cstdio>
#include <cstdlib>
#include <unordered_set>
#include <queue>

#include "util/inv_argv.h"
#include "util/inv_config.h"
#include "inv_log.h"
#include "report.h"

#include "ufs_logic.h"
#include "kafka_consumer.h"
#include"ssdb_io_sync.h"
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
    // int pollInterval;
    std::string categoryHashName;
    std::string tagHashName;
    std::string categoryClickAndImpHashName;

    std::string ufsStatsTag;
    std::string ufsStatsCategory;

    // std::string ufsUrl;
    int decayPeriod;
    double defaultDecayRate;
    std::map<std::string, double> decayRate;

    int cacheNum;

    std::vector<std::pair<std::string, uint16_t> > brokers;
    std::string topic;
    std::string group;

    std::string dbHost;
    int dbPort;
    std::string dbUser;
    std::string dbPass;
    std::string dbCharset;
    std::string dbName;

    std::vector<std::string> categoryVersionToBeRemoved;
    std::vector<std::string> tagVersionToBeRemoved;
    struct Addr
    {
        std::string host;
        int port;
        int timeout;
    };
    std::vector<Addr> addrs;

    int32_t writeCold;
    std::string ssdbSync;
};

void LoadSetting(setting *pcf, inv::INV_Config* pconfig)
{
    // std::string itv = pconfig->get("/main/[poll_interval]");
    // pcf->pollInterval = atoi(itv.c_str());
    // if (pcf->pollInterval <= 0)
    // {
    //     fprintf(stderr, "/main/[poll_interval] empty\n");
    //     exit(-1);
    // }

    std::string cn = pconfig->get("/main/[cache_num]"); /* 20000 */
    assert(!cn.empty());
    pcf->cacheNum = atoi(cn.c_str());
    assert(pcf->cacheNum > 0);

    pcf->categoryHashName = pconfig->get("/main/[category_hash]"); /* zhizi.ufs.category. */
    if (pcf->categoryHashName.empty())
    {
        fprintf(stderr, "/main/[category_hash] empty\n");
        exit(-1);
    }

    pcf->tagHashName = pconfig->get("/main/[tag_hash]"); /* zhizi.ufs.tag. */
    if (pcf->tagHashName.empty())
    {
        fprintf(stderr, "/main/[tag_hash] empty\n");
        exit(-1);
    }

    pcf->categoryClickAndImpHashName = pconfig->get("/main/[category_click_and_imp_hashname]"); /* zhizi.ufs.category.clickandimp. */
    if (pcf->categoryClickAndImpHashName.empty())
    {
        fprintf(stderr, "/main/[category_click_and_imp_hashname] empty\n");
        exit(-1);
    }

    pcf->ufsStatsTag = pconfig->get("/main/[stats_tag_hash]"); /* zhizi.ufs.stats.tag */
    assert(!pcf->ufsStatsTag.empty());
    pcf->ufsStatsCategory = pconfig->get("/main/[stats_category_hash]"); /*  zhizi.ufs.stats.category */
    assert(!pcf->ufsStatsCategory.empty());

    {
        std::string strSsdbConn = pconfig->get("/main/[ssdb_conn]");  /* 10.10.102.186:8888:1 */
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

    {
        std::string vers = pconfig->get("/main/[category_version_to_be_removed]");
        // assert(!vers.empty());
        pcf->categoryVersionToBeRemoved = inv::INV_Util::sepstr<string>(vers, ",");
    }

    {
        std::string vers = pconfig->get("/main/[tag_version_to_be_removed]");
        // assert(!vers.empty());
        pcf->tagVersionToBeRemoved = inv::INV_Util::sepstr<string>(vers, ",");
    }

    std::string ddr = pconfig->get("/main/[default_decay_rate]");
    pcf->defaultDecayRate = atof(ddr.c_str());
    if (pcf->defaultDecayRate <= 0)
    {
        fprintf(stderr, "/main/[default_decay_rate] empty\n");
        exit(-1);
    }

    {
        std::string dr = pconfig->get("/main/[decay_rate]");
        std::vector<std::string> fields = inv::INV_Util::sepstr<string>(dr, ",", true);
        for (size_t i = 0; i < fields.size(); ++i)
        {
            std::vector<std::string> hpt = inv::INV_Util::sepstr<string>(fields[i], ":", true);
            assert(hpt.size() == 2);
            double rate = atof(hpt[1].c_str());
            assert(rate > 0);
            pcf->decayRate.insert(std::make_pair(hpt[0], rate));
        }
    }

    std::string dp = pconfig->get("/main/[decay_period]");
    pcf->decayPeriod = atoi(dp.c_str());
    if (pcf->decayPeriod <= 0)
    {
        fprintf(stderr, "/main/[decay_period] empty\n");
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

    std::string wc = pconfig->get("/main/[write_cold]");
    pcf->writeCold = atoi(wc.c_str());
    assert(pcf->writeCold > 0);

    pcf->ssdbSync = pconfig->get("/main/[ssdb_sync_conn]");
    if (pcf->dbName.empty())
    {
        fprintf(stderr, "/main/[ssdb_sync_conn] empty\n");
        exit(-1);
    }
}

bool gRun = false;

static void Stop (int sig)
{
    gRun = false;
    fclose(stdin);
}

int32_t LoadBlackWords(TBlackTags* tb, int& last_update, std::unordered_set<std::string>& out)
{
    // std::unordered_set<std::string> words;
    // int last_update = 0;
    if (time(NULL) - last_update < 24*60*60)
    {
        // out = words;
        return 0;
    }
    inv::INV_Mysql::MysqlData records;
    int ret = tb->GetAllBlackWords(records);
    if (ret < 0)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" GetAllBlackWords error: "<<(tb->GetLastErrMsg())<<endl);
    }
    else if (ret > 0)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" GetAllBlackWords error: "<<(tb->GetLastErrMsg())<<endl);
        last_update = time(NULL) - 23*60*60;
    }
    else
    {
        // words.clear();
        out.clear();
        for (size_t i = 0; i < records.size(); ++i)
        {
            // words.insert(records[i]["word"]);
            out.insert(records[i]["word"]);
        }
        last_update = time(NULL);
    }
    // out = words;
    return 0;
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
    int limit;
    int timezone;

    setting cf;
    int threadNum;
};

void* consume_msg(void* arg)
{
    thread_param* param = (thread_param*)arg;

    std::unordered_set<std::string> categoryVersionToBeRemoved;
    for (const auto& ver:param->cf.categoryVersionToBeRemoved)
    {
        categoryVersionToBeRemoved.insert(ver);
    }
    std::unordered_set<std::string> tagVersionToBeRemoved;
    for (const auto& ver:param->cf.tagVersionToBeRemoved)
    {
        tagVersionToBeRemoved.insert(ver);
    }
    std::shared_ptr<inv::INV_Mysql> sp_conn(param->conn);

    std::vector<inv::INV_Redis*> conns;
    std::vector<inv::INV_Redis*> connsForWrite;
    for (const auto& it: param->cf.addrs)
    {
        {
            inv::INV_Redis* redis = new inv::INV_Redis;
            redis->init(it.host, it.port, it.timeout);
            redis->connect();
            conns.push_back(redis);
        }

        {
            inv::INV_Redis* redis = new inv::INV_Redis;
            /* ssdb_conn=10.10.102.186:8888:1 */
            redis->init(it.host, it.port, it.timeout);
            redis->connect();
            connsForWrite.push_back(redis);
        }
    }


    SfuLogic sfu(sp_conn, param->cf.decayPeriod, param->cf.defaultDecayRate,
            param->cf.decayRate, conns, connsForWrite, param->cf.categoryHashName,
            param->cf.tagHashName, param->cf.ufsStatsCategory,
            param->cf.ufsStatsTag, param->limit, param->timezone,
            param->cf.cacheNum, param->threadNum, param->cf.writeCold, param->cf.categoryClickAndImpHashName);
            //param->cf.cacheNum, param->threadNum, param->cf.writeCold, param->cf.categoryClickAndImpHashName, ssdbSyncConn);
    TBlackTags tb(sp_conn);
    sfu.ActivateSync();

    std::unordered_set<std::string> black_words;
    int last_update = 0;
    while (gRun)
    {
        LoadBlackWords(&tb, last_update, black_words);

        std::vector<std::string> tmp;
        const size_t tmpCapacity = 100;
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

        // size_t aq = qsize;
        // if (qsize > 0)
        // //if (!param->q->empty())
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
        //     sleep(1);
        //     continue;
        // }
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" qsize from " << qsize << " to " << aq <<endl);

        for (const auto& msg: tmp)
        {
            ReportIncr("zhizi.ufs.main.count");
            if (param->cf.topic == EventTypeTableStr[EVENT_CLICK][1].C_STR())
            {
                TIME_LABEL(1);
                int ret = sfu.FeedUfs(msg, black_words, categoryVersionToBeRemoved, tagVersionToBeRemoved);
                ReportIncr("zhizi.ufs.main.time", TIME_DIFF(1));
                if (ret)
                {
                    TLOGINFO(__FILE__<<"-"<<__LINE__<<" FeedUfs error"<<endl);
                }
            } else if(param->cf.topic == EventTypeTableStr[EVENT_IMP][1].C_STR()) {
                TIME_LABEL(1);
                int ret = sfu.FeedImpUfs(msg);
                ReportIncr("zhizi.ufs.main.time", TIME_DIFF(1));
                if (ret)
                {
                    TLOGINFO(__FILE__<<"-"<<__LINE__<<" FeedImpUfs error"<<endl);
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

        // if (msg.empty())
        // {
        //     continue;
        // }
        if (param->cf.topic == "click-reformat")
        {
            int ret = sfu.FeedUfs(msg, black_words, categoryVersionToBeRemoved, tagVersionToBeRemoved);
            if (ret)
            {
                TLOGINFO(__FILE__<<"-"<<__LINE__<<" FeedUfs error"<<endl);
            }
        }
        else if(param->cf.topic == "impression-reformat")
        {
            int ret = sfu.FeedImpUfs(msg);
            if (ret)
            {
                TLOGINFO(__FILE__<<"-"<<__LINE__<<" FeedImpUfs error"<<endl);
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
    if (qsize > 1000)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" idx: " << idx << " queue size: " << qsize <<endl);
        sleep(1);
    }
    return 0;
}

int main(int argc, char** argv)
{
    INIT_INV_LOGGER("INV", "ufs");
    inv::INV_Argv ag;
    ag.decode(argc, argv);
    string confPath = ag.getValue("config");
    if (confPath.empty())
    {
        fprintf(stderr, "usage: %s --thread_num=8 --limit=200 --timezone=8 --config=config.conf\n", argv[0]);
        exit(1);
    }

    string slimit = ag.getValue("limit");
    if (slimit.empty())
    {
        fprintf(stderr, "usage: %s --thread_num=8 --limit=200 --timezone=8 --config=config.conf\n", argv[0]);
        exit(1);
    }
    int limit = atoi(slimit.c_str());
    if (limit < 0)
    {
        fprintf(stderr, "usage: %s --thread_num=8 --limit=200 --timezone=8 --config=config.conf\n", argv[0]);
        exit(1);
    }
    inv::INV_Config config;
    config.parseFile(confPath);

    setting cf;
    LoadSetting(&cf, &config);

    /* 00=10.10.20.14:9092
     * 01=10.10.20.15:9092
     * 02=10.10.20.16:9092
     *
     * topic = click-reformat
     * group = zhizi.ufs.eus
     * */
    KafkaConsumer consumer(cf.brokers, cf.group, cf.topic);
    if (consumer.Init() != 0)
    {
        fprintf(stderr, "kafka client init error: %s\n", consumer.GetLastErrMsg().c_str());
        exit(1);
    }
    string tn = ag.getValue("thread_num");
    if (tn.empty())
    {
        fprintf(stderr, "usage: %s --thread_num=8 --limit=200 --timezone=8 --config=config.conf\n", argv[0]);
        exit(1);
    }
    int threadNum  = atoi(tn.c_str());
    assert(threadNum > 0);

    string tz = ag.getValue("timezone");
    if (tz.empty())
    {
        fprintf(stderr, "usage: %s --limit=200 --timezone=8 --config=config.conf\n", argv[0]);
        exit(1);
    }
    int timezone = atoi(tz.c_str());

    // std::queue<std::string> global_queue;
    // pthread_mutex_t queue_lock;
    // assert(pthread_mutex_init(&queue_lock, NULL) == 0);
    gRun = true;
    signal(SIGINT, Stop);
    signal(SIGTERM, Stop);
    signal(SIGIO, SIG_IGN);

    std::string strSsdbSyncConn = cf.ssdbSync;  /* 10.10.102.186:8888:1 */
    std::vector<std::string> ssdbSyncs = inv::INV_Util::sepstr<string>(strSsdbSyncConn, ":", true);
    std::string ssdbSyncHost = ssdbSyncs[0];
    int ssdbSyncPort = atoi(ssdbSyncs[1].c_str());
    int ssdbSyncTimeout = atoi(ssdbSyncs[2].c_str());
    inv::INV_Redis ssdbSyncConn;
    ssdbSyncConn.init(ssdbSyncHost, ssdbSyncPort, ssdbSyncTimeout);
    ssdbSyncConn.connect();
    //SsdbIoSync ssdbSync(&ssdbSyncConn);
    std::shared_ptr<SsdbIoSync> ssdbIoSync(nullptr);
    if (cf.topic == EventTypeTableStr[EVENT_CLICK][1].C_STR()) {
        ssdbIoSync.reset(new SsdbIoSync(&ssdbSyncConn, EVENT_CLICK));
    } else if(cf.topic == EventTypeTableStr[EVENT_IMP][1].C_STR()) {
        ssdbIoSync.reset(new SsdbIoSync(&ssdbSyncConn, EVENT_IMP));
    } else {
        exit(1);
    }

    std::thread ssdbSyncThread = std::thread([&](){
            ssdbIoSync->Start();
            });

    std::vector<QueueOfThread*> allQueue;
    for (int i = 0; i < threadNum; ++i)
    {
        QueueOfThread* t = new QueueOfThread;
        assert(pthread_mutex_init(&(t->lock), NULL) == 0);
        allQueue.push_back(t);
    }

    std::vector<pthread_t> vecThread;
    for (int i = 0; i < threadNum; ++i)
    {
        // boost::shared_ptr<inv::INV_Mysql> conn(new inv::INV_Mysql);
        inv::INV_Mysql* conn = new inv::INV_Mysql;
        /*host = db-mta-us-east-1.cxleyzgw272j.us-east-1.rds.amazonaws.com
         *
         * port = 3306
         *
         * user = ufs_r
         * pass = ee23536d529a1ef2e17e9d9e612355f3
         *
         * db = db_mta
         * charset = utf8
         * */
        conn->init(cf.dbHost, cf.dbUser, cf.dbPass, cf.dbName, cf.dbCharset, cf.dbPort);
        // TBlackTags tb(conn);

        // inv::INV_Redis* redis = new inv::INV_Redis;
        // redis->init(cf.redisHost, cf.redisPort, cf.redisTimeout);
        // redis->connect();

        // inv::INV_Redis* coolpad_redis = new inv::INV_Redis;
        // coolpad_redis->init(cf.coolpadRedisHost, cf.coolpadRedisPort, cf.coolpadRedisTimeout);
        // coolpad_redis->connect();

        thread_param* param = new thread_param;
        // param->redis = redis;
        // param->coolpad_redis = coolpad_redis;
        param->conn = conn;

        param->thread_no = i;
        param->q = &(allQueue[i]->queue);
        param->lock = &(allQueue[i]->lock);
        // param->q = &global_queue;
        // param->lock = &queue_lock;
        // param->last_update = 0;

        param->limit = limit;
        param->timezone = timezone;
        param->cf = cf;
        param->threadNum = threadNum;

        int rc = pthread_create(&param->thread_id, NULL, &consume_msg, param);
        if (rc != 0)
        {
            fprintf(stderr, "pthread_create fail: %d\n", i);
            return -1;
        }
        vecThread.push_back(param->thread_id);
    }


    // std::unordered_set<std::string> versionToBeRemoved{"tg_v4", "cg_v3"};
    // std::vector<std::string> black_words;
    // std::unordered_set<std::string> black_words;
    while (gRun)
    {
        std::string msg;
        if (consumer.FetchMsg(msg))
        {
            TLOGINFO(__FILE__<<"-"<<__LINE__<<" FetchMsg error: "<<consumer.GetLastErrMsg()<<endl);
            continue;
        }
        // int ret = sfu.FeedUfs(msg, black_words, versionToBeRemoved);
        // if (ret)
        // {
        //     TLOGINFO(__FILE__<<"-"<<__LINE__<<" FeedUfs error"<<endl);
        // }
        // pthread_mutex_lock(&queue_lock);
        // global_queue.push(msg);
        // pthread_mutex_unlock(&queue_lock);
        // if (global_queue.size() > 10000)
        // {
        //     TLOGINFO(__FILE__<<"-"<<__LINE__<<" queue size: " << global_queue.size() <<endl);
        //     sleep(1);
        // }
        HandoutMsg(allQueue, msg);
    }
    TLOGINFO(__FILE__<<"-"<<__LINE__<<": will exit"<<endl);
    for (const auto& i: vecThread)
    {
        pthread_join(i, NULL);
    }
    for (const auto& i: allQueue)
    {
        pthread_mutex_destroy(&(i->lock));
    }

    ssdbSyncThread.join();

    return 0;
}
