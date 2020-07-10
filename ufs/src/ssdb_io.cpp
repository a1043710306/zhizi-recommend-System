#include "inv_log.h"
#include "report.h"

#include "ssdb_io.h"

using namespace inv;
using namespace inv::monitor;

extern bool gRun;

SsdbIo::SsdbIo(const std::vector<inv::INV_Redis*>& vecRedis,
        const std::vector<inv::INV_Redis*>& vecWRedis, int32_t cold)
    :threshhold_(2000), vecRedis_(vecRedis), vecWRedis_(vecWRedis),
    vecSync_(vecRedis_.size()), vecThread_(vecRedis_.size()), cold_(cold)
{
    assert(this->vecRedis_.size() > 0);
    assert(this->vecRedis_.size() == this->vecWRedis_.size());
    for (auto& it: this->vecSync_)
    {
        assert(pthread_mutex_init(&(it.lock), NULL) == 0);
    }
}

SsdbIo::~SsdbIo()
{
    for (const auto& it: this->vecThread_)
    {
        pthread_join(it, NULL);
    }
    for (auto& it: this->vecSync_)
    {
        pthread_mutex_destroy(&(it.lock));
    }
}

int32_t SsdbIo::AddSyncTask(const std::string& hash, const std::string& uid, const std::shared_ptr<std::string>& json)
{
    FUNC_GUARD();
    size_t idx = GetIdxOfConns(uid, this->vecRedis_.size(), "");
    TLOGINFO(__FILE__<<"-"<<__LINE__<<" uid: " << uid << " assigned to " << idx <<endl);

    bool hit = false;
    int32_t ts = time(NULL);
    ReportIncr("zhizi.ufs.synctask.set");
    TaskQueue& tq = this->vecSync_[idx];
    pthread_mutex_lock(&tq.lock);
    const std::string key = hash + "|" + uid;
    if (tq.data.count(key) > 0)
    {
        ts = tq.data[key].ts;
        hit = true;
    }
    tq.data[key] = SsdbIo::SyncTask{ts, hash, uid, json};
    size_t qsize = tq.data.size();
    pthread_mutex_unlock(&tq.lock);

    if (hit)
    {
        ReportIncr("zhizi.ufs.synctask.hit");
    }
    if (qsize >= this->threshhold_)
    {
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" qsize: " << qsize << " for " << idx <<endl);
        return 1;
    }
    return 0;
}

void SsdbIo::Start()
{
    assert(this->vecWRedis_.size() == this->vecSync_.size());
    assert(this->vecWRedis_.size() == this->vecThread_.size());

    for (size_t i = 0; i < this->vecWRedis_.size(); ++i)
    {
        ThreadArgs *pargs = new ThreadArgs{this->vecWRedis_[i], &(this->vecSync_[i]), i, this->cold_};
        pthread_create(&this->vecThread_[i], NULL, SsdbIo::WriteSsdb, pargs);
    }
}

void* SsdbIo::WriteSsdb(void* arg)
{
    assert(gRun);
    ThreadArgs *pargs = (ThreadArgs*)arg;

    std::map<std::string, std::pair<std::vector<std::string>, std::vector<std::string>>> groupTask;
    while (gRun)
    {
        std::vector<SsdbIo::SyncTask> tmp;
        const size_t tmpCapacity = 100;
        pthread_mutex_lock(&pargs->q->lock);
        size_t qsize = pargs->q->data.size();
        auto it = pargs->q->data.cbegin();
        while (it != pargs->q->data.cend())
        {
            if (tmp.size() >= tmpCapacity)
            {
                break;
            }
            if (time(NULL) - it->second.ts < pargs->cold)
            {
                ++it;
                continue;
            }
            tmp.push_back(it->second);
            it = pargs->q->data.erase(it);
        }
        size_t aq = pargs->q->data.size();
        pthread_mutex_unlock(&pargs->q->lock);

        TLOGINFO(__FILE__<<"-"<<__LINE__<<" qsize from " << qsize << " to " << aq <<endl);
        size_t ret = SsdbIo::DoSync(pargs->idx, pargs->conn, tmp, groupTask);
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" synced " << ret << " of " << tmp.size() <<endl);
        if (tmp.size() < tmpCapacity)
        {
            TLOGINFO(__FILE__<<"-"<<__LINE__<<" will sleep, drain queue: " << pargs->idx <<endl);
            usleep(SsdbIo::nap_);
        }
    }
    while (true)
    {
        std::vector<SsdbIo::SyncTask> tmp;
        const size_t tmpCapacity = 100;
        pthread_mutex_lock(&pargs->q->lock);
        size_t qsize = pargs->q->data.size();
        auto it = pargs->q->data.cbegin();
        while (it != pargs->q->data.cend())
        {
            if (tmp.size() >= tmpCapacity)
            {
                break;
            }
            tmp.push_back(it->second);
            it = pargs->q->data.erase(it);
        }
        size_t aq = pargs->q->data.size();
        pthread_mutex_unlock(&pargs->q->lock);

        TLOGINFO(__FILE__<<"-"<<__LINE__<<" qsize from " << qsize << " to " << aq <<endl);
        size_t ret = SsdbIo::DoSync(pargs->idx, pargs->conn, tmp, groupTask);
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" synced " << ret << " of " << tmp.size() <<endl);
        if (tmp.size() < tmpCapacity)
        {
            TLOGINFO(__FILE__<<"-"<<__LINE__<<" will exit thread: " << pargs->idx <<endl);
            break;
        }
    }
    return NULL;
}

size_t SsdbIo::DoSync(size_t idx, inv::INV_Redis* conn, const std::vector<SsdbIo::SyncTask>& vecTask,
    std::map<std::string, std::pair<std::vector<std::string>, std::vector<std::string>>>& groupTask)
{
    for (const auto& t: vecTask)
    {
        const std::string hash = t.hash;
        if (groupTask.count(hash) <= 0)
        {
            groupTask.insert(std::make_pair(hash, std::make_pair(std::vector<std::string>(), std::vector<std::string>())));
        }

        std::vector<std::string>& uids = groupTask.find(hash)->second.first;
        uids.push_back(t.uid);
        std::vector<std::string>& jsons = groupTask.find(hash)->second.second;
        jsons.push_back(*(t.json));
    }

    // size_t count = 0;
    auto it = groupTask.begin();
    // for (const auto& it: groupTask)
    while (it != groupTask.end())
    {
        assert(it->second.first.size() == it->second.second.size());
        if (it->second.first.size() < 50)
        {
            ++it;
            continue;
        }
        int ret = RedisHashMSet(conn, it->first, it->second.first, it->second.second);
        TLOGINFO(__FILE__<<"-"<<__LINE__<<" sync task batch: " << it->second.first.size()
                << ", ret: " << ret << ", idx: " << idx <<endl);
        ReportIncr("zhizi.ufs.write.ssdb." + std::to_string(idx), it->second.first.size());
        // if (ret == 0)
        // {
        //     it = groupTask.erase(it);
        //     // count += it->second.first.size();
        //     continue;
        // }
        if (ret != 0)
        {
            ReportIncr("zhizi.ufs.write.ssdb.fail." + std::to_string(idx), it->second.first.size());
        }
        it = groupTask.erase(it);
        // else
        // {
        // if (it->second.first.size() >= 200)
        // {
        //     TLOGINFO(__FILE__<<"-"<<__LINE__<<" block, will delete: " << it->second.first.size() <<endl);
        //     it = groupTask.erase(it);
        //     continue;
        // }
        // }
        // ++it;
    }
    return 0;
}

int32_t SsdbIo::GetSsdbJson(std::string& json, const std::string& hash, const std::string& uid)
{
    FUNC_GUARD();
    json.clear();
    size_t idx = GetIdxOfConns(uid, this->vecRedis_.size(), "");
    inv::INV_Redis* conn = this->vecRedis_[idx];
    int ret = RedisHashGet(json, conn, hash, uid);
    if (ret < 0)
    {
        return -1;
    }
    TLOGINFO(__FILE__<<"-"<<__LINE__<<" get json ok: " << hash << ":" << uid << " , " << json.size() <<endl);
    return 0;
}

int SsdbIo::GetSsdbJsons(std::vector<std::string>& jsons, const std::string& uid, const std::vector<std::string>& fields) {
    FUNC_GUARD();
    jsons.clear();
    size_t idx = GetIdxOfConns(uid, this->vecRedis_.size(), "");
    inv::INV_Redis* conn = this->vecRedis_[idx];
    int ret = RedisHashMGet(jsons, conn, uid, fields);
    if (ret < 0)
    {
        return -1;
    }
    TLOGINFO(__FILE__<<"-"<<__LINE__<<" get json ok: " << "uid-"<<uid << ", size-" << jsons.size() <<endl);

    return 0;
}

