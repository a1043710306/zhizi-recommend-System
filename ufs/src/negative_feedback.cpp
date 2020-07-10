#include <cstdio>
#include <cstdlib>
#include <unordered_set>
#include <queue>

#include "util/inv_argv.h"
#include "util/inv_config.h"
#include "inv_log.h"
#include "report.h"

#include "db.h"
#include "kafka_consumer.h"
#include "util.h"
#include "event_parser.h"
#include "common_weight.h"
#include "info_signal_cache.h"

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
    std::string hashName;
    int decayPeriod;
    double defaultDecayRate;
    std::map<std::string, double> decayRate;

    int cacheNum;
    int infoCacheSize;

    std::vector<std::pair<std::string, uint16_t> > brokers;
    std::string topic;
    std::string group;

    std::string dbHost;
    int dbPort;
    std::string dbUser;
    std::string dbPass;
    std::string dbCharset;
    std::string dbName;

    std::vector<std::string> versionToBeRemoved;
    struct Addr
    {
        std::string host;
        int port;
        int timeout;
    };
    std::vector<Addr> addrs;

    int32_t writeCold;
    int32_t timeZone;

    std::string feedbackType;
};

void LoadSetting(setting *pcf, inv::INV_Config* pconfig)
{
    std::string cn = pconfig->get("/main/[cache_num]");
    assert(!cn.empty());
    pcf->cacheNum = atoi(cn.c_str());
    assert(pcf->cacheNum > 0);

    std::string ics = pconfig->get("/main/[info_cache_size]");
    assert(!ics.empty());
    pcf->infoCacheSize = atoi(ics.c_str());
    assert(pcf->infoCacheSize > 0);

    pcf->hashName = pconfig->get("/main/[hash]");
    if (pcf->hashName.empty())
    {
        fprintf(stderr, "/main/[hash] empty\n");
        exit(-1);
    }

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

    {
        std::string vers = pconfig->get("/main/[version_to_be_removed]");
        pcf->versionToBeRemoved = inv::INV_Util::sepstr<string>(vers, ",");
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

    std::string tz = pconfig->get("/main/[timezone]");
    pcf->timeZone = atoi(tz.c_str());
    // assert(pcf->writeCold > 0);

    pcf->feedbackType = pconfig->get("/main/[feedback_type]");
    assert(pcf->feedbackType == "impression" || pcf->feedbackType == "dislike");
}

bool gRun = false;

static void Stop (int sig)
{
    gRun = false;
    fclose(stdin);
}

int32_t LoadBlackWords(TBlackTags* tb, pthread_mutex_t* lock, int& last_update, std::unordered_set<std::string>& out)
{
    if (time(NULL) - last_update < 24*60*60)
    {
        return 0;
    }
    pthread_mutex_lock(lock);
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
        out.clear();
        for (size_t i = 0; i < records.size(); ++i)
        {
            out.insert(records[i]["word"]);
        }
        last_update = time(NULL);
    }
    pthread_mutex_unlock(lock);
    return 0;
}

struct QueueOfThread
{
    std::queue<std::string> queue;
    pthread_mutex_t lock;
};

struct thread_param
{
    // inv::INV_Mysql* conn;

    pthread_t thread_id;
    int thread_no;

    std::queue<std::string>* q;
    pthread_mutex_t* lock;

    TBlackTags* tb;
    pthread_mutex_t* btLock;

    InfoSignalCache* infoCache;

    int limit;

    setting cf;
    int threadNum;
};

int32_t FeedUfs(const std::string& kafkaMsg, InfoSignalCache* infoCache,
        CommonWeight* cw, const std::unordered_set<std::string>& blackWords,
        const std::string& feedbackType, int32_t limit, int decayPeriod,
        double defaultDecayRate, const std::map<std::string, double>& decayRate,
        const std::unordered_set<std::string>& versionToBeRemoved)
{
    FUNC_GUARD();
    TLOGINFO(__FILE__<<"-"<<__LINE__<<" kafka msg: "<<kafkaMsg<<endl);
    if (kafkaMsg.empty())
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" message error"<<endl);
        return -1;
    }

    EventParser::Event ev;
    if (feedbackType == "dislike")
    {
        int ret = EventParser::ParseDislikeMsg(ev, kafkaMsg);
        if (ret != 0)
        {
            TLOGINFO(__FILE__<<"-"<<__LINE__<<" ParseClickMsg fail"<<endl);
            return -1;
        }
    }
    else if (feedbackType == "impression")
    {
        // int ret = EventParser::ParseImpressionMsg(ev, kafkaMsg);
        int ret = EventParser::ParseImcMsg(ev, kafkaMsg);
        if (ret != 0)
        {
            TLOGINFO(__FILE__<<"-"<<__LINE__<<" ParseImpressionMsg fail"<<endl);
            return -1;
        }
    }
    TLOGINFO(__FILE__<<"-"<<__LINE__<<" infoid: "<<ev.infoId<<", uid: "<<ev.uid<<endl);

    int32_t span = time(NULL) - ev.eventTime;
    if (ev.eventTime > 1400000000 && span >= 0)
    {
        ReportMax("zhizi.ufs.click.till.process.max", span);
        ReportMin("zhizi.ufs.click.till.process.min", span);
        ReportAvg("zhizi.ufs.click.till.process.avg", span);
    }

    std::map<std::string, std::map<std::string, double> > cwt;
    int ret = infoCache->GetTitleTags(cwt, ev.infoId);
    if (ret != 0)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" GetTitleTags fail: "<<ev.infoId<<endl);
        return -1;
    }

    for (const auto& vw: cwt)
    {
        if (versionToBeRemoved.count(vw.first) > 0)
        {
            continue;
        }
        std::string version = vw.first;
        std::string keyDecay = feedbackType + "_" + version + "_" + ev.configId;
        std::map<std::string, double> merged(vw.second);

        double rate = defaultDecayRate;//GET_CONF()->get_conf_float("decay_rate", 0.995);
        if (decayRate.count(keyDecay) > 0)
        {
            rate = decayRate.find(keyDecay)->second;
        }

        std::shared_ptr<std::map<std::string, double>> history(new std::map<std::string, double>);
        cw->GetWeightedTag(history, version, ev.uid, rate, decayPeriod);
        CommonWeight::MergeWeightMaps(history, merged, ev.sign);
        cw->SetWeightedTag(version, ev.uid, history, ev.app, ev.configId, limit, blackWords);
    }
    return 0;
}

void* consume_msg(void* arg)
{
    thread_param* param = (thread_param*)arg;

    std::unordered_set<std::string> versionToBeRemoved;
    for (const auto& ver:param->cf.versionToBeRemoved)
    {
        versionToBeRemoved.insert(ver);
    }
    // std::shared_ptr<inv::INV_Mysql> sp_conn(param->conn);

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
            redis->init(it.host, it.port, it.timeout);
            redis->connect();
            connsForWrite.push_back(redis);
        }
    }
    // SfuLogic sfu(sp_conn, param->cf.decayPeriod, param->cf.defaultDecayRate,
    //         param->cf.decayRate, conns, connsForWrite, param->cf.categoryHashName,
    //         param->cf.tagHashName, param->cf.ufsStatsCategory,
    //         param->cf.ufsStatsTag, param->limit, param->timezone,
    //         param->cf.cacheNum, param->threadNum, param->cf.writeCold);

    // common_weight in mem
    // info cache

    CommonWeight cw(param->cf.cacheNum/param->threadNum, param->cf.hashName, conns, connsForWrite, param->cf.writeCold);
    // TBlackTags tb(sp_conn);
    cw.ActivateSync();

    std::unordered_set<std::string> blackWords;
    int last_update = 0;
    while (gRun)
    {
        LoadBlackWords(param->tb, param->btLock, last_update, blackWords);

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

        TLOGINFO(__FILE__<<"-"<<__LINE__<<" qsize from " << qsize << " to " << aq <<endl);

        for (const auto& msg: tmp)
        {
            ReportIncr("zhizi.ufs.main.count");
            TIME_LABEL(1);
            int ret = FeedUfs(msg, param->infoCache, &cw, blackWords, param->cf.feedbackType, param->limit,
                    param->cf.decayPeriod, param->cf.defaultDecayRate, param->cf.decayRate, versionToBeRemoved);
            ReportIncr("zhizi.ufs.main.time", TIME_DIFF(1));
            if (ret)
            {
                TLOGINFO(__FILE__<<"-"<<__LINE__<<" FeedUfs error"<<endl);
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

        // int ret = FeedUfs(msg, black_words, categoryVersionToBeRemoved, versionToBeRemoved);
        int ret = FeedUfs(msg, param->infoCache, &cw, blackWords, param->cf.feedbackType, param->limit,
                param->cf.decayPeriod, param->cf.defaultDecayRate, param->cf.decayRate, versionToBeRemoved);
        if (ret)
        {
            TLOGINFO(__FILE__<<"-"<<__LINE__<<" FeedUfs error"<<endl);
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
    size_t idx = GetIdxOfConns(uid, allQueue.size(), "negative");// same uid go to same queue, same thread
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
    INIT_INV_LOGGER("INV", "negative");
    inv::INV_Argv ag;
    ag.decode(argc, argv);
    string confPath = ag.getValue("config");
    if (confPath.empty())
    {
        fprintf(stderr, "usage: %s --thread_num=8 --limit=200 --config=config.conf\n", argv[0]);
        exit(1);
    }

    string slimit = ag.getValue("limit");
    if (slimit.empty())
    {
        fprintf(stderr, "usage: %s --thread_num=8 --limit=200 --config=config.conf\n", argv[0]);
        exit(1);
    }
    int limit = atoi(slimit.c_str());
    if (limit < 0)
    {
        fprintf(stderr, "usage: %s --thread_num=8 --limit=200 --config=config.conf\n", argv[0]);
        exit(1);
    }
    inv::INV_Config config;
    config.parseFile(confPath);

    setting cf;
    LoadSetting(&cf, &config);

    KafkaConsumer consumer(cf.brokers, cf.group, cf.topic);
    if (consumer.Init() != 0)
    {
        fprintf(stderr, "kafka client init error: %s\n", consumer.GetLastErrMsg().c_str());
        exit(1);
    }
    string tn = ag.getValue("thread_num");
    if (tn.empty())
    {
        fprintf(stderr, "usage: %s --thread_num=8 --limit=200 --config=config.conf\n", argv[0]);
        exit(1);
    }
    int threadNum  = atoi(tn.c_str());
    assert(threadNum > 0);

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


    inv::INV_Mysql* signalConn = new inv::INV_Mysql;
    signalConn->init(cf.dbHost, cf.dbUser, cf.dbPass, cf.dbName, cf.dbCharset, cf.dbPort);
    InfoSignalCache* pcache = new InfoSignalCache((new TSignal(std::shared_ptr<inv::INV_Mysql>(signalConn))), cf.infoCacheSize, cf.timeZone);

    inv::INV_Mysql* conn = new inv::INV_Mysql;
    conn->init(cf.dbHost, cf.dbUser, cf.dbPass, cf.dbName, cf.dbCharset, cf.dbPort);
    std::shared_ptr<inv::INV_Mysql> sp_conn(conn);
    TBlackTags tb(sp_conn);

    pthread_mutex_t btLock;
    assert(pthread_mutex_init(&btLock, NULL) == 0);

    EventParser::SetTimeZone(cf.timeZone);

    std::vector<pthread_t> vecThread;
    for (int i = 0; i < threadNum; ++i)
    {
        thread_param* param = new thread_param;
        // param->conn = conn;

        param->tb = &tb;
        param->btLock = &btLock;

        param->thread_no = i;
        param->q = &(allQueue[i]->queue);
        param->lock = &(allQueue[i]->lock);

        param->limit = limit;
        param->cf = cf;
        param->threadNum = threadNum;

        param->infoCache = pcache;

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
            continue;
        }
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
    pthread_mutex_destroy(&btLock);
    return 0;
}

