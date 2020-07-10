#ifndef PRIORITY_KV_CACHE_H
#define PRIORITY_KV_CACHE_H 
#include <map>
#include <vector>
#include <functional>
#include <queue>

#include <string>
#include <ctime>
#include <cassert>
//#include <cstdio>

#include<pthread.h>

template<class KType, class VType>
class PriorityKVCache
{
    private:
        struct KeyTime
        {
            KType key;
            time_t ts;
        };
    public:
        explicit PriorityKVCache(size_t num)
            :pq_([](const KeyTime& a, const KeyTime& b){return b.ts < a.ts;}),
            capacity_(num)
        {
            assert(pthread_mutex_init(&this->lock_, NULL) == 0);
        }
        virtual ~PriorityKVCache()
        {
            pthread_mutex_destroy(&this->lock_);
        }
        PriorityKVCache(PriorityKVCache const &) = delete;
        PriorityKVCache& operator=(PriorityKVCache const &) = delete;

        int32_t Get(VType& val, const KType& key)
        {
            pthread_mutex_lock(&this->lock_);
            if (this->db_.count(key) <= 0)// not exist
            {
                pthread_mutex_unlock(&this->lock_);
                return 1;
            }
            val = this->db_[key];
            pthread_mutex_unlock(&this->lock_);
            return 0;
        }

        int32_t Set(const KType& key, const VType& val, time_t ts)
        {
            pthread_mutex_lock(&this->lock_);
            if (this->db_.count(key) > 0)// exist already
            {
                // assert(this->pq_.count(key) > 0);
                this->db_[key] = val;
                // this->pq_[key] = time(NULL);
                pthread_mutex_unlock(&this->lock_);
                return 0;
            }

            if (this->db_.size() >= this->capacity_)
            {
                assert(this->db_.size() == this->capacity_);
                assert(this->db_.size() == this->pq_.size());

                const KeyTime& t = this->pq_.top();
                KType key2del = t.key;
                // fprintf(stdout, "%s\n", key2del.c_str());
                this->db_.erase(key2del);
                this->pq_.pop();
            }

            this->db_[key] = val;
            this->pq_.push({key, ts});
            pthread_mutex_unlock(&this->lock_);

            // assert(this->pq_.count(key) <= 0);
            // if (this->pq_.size() < this->capacity_)// not full
            // {
            //     this->db_[key] = val;
            //     this->pq_[key] = time(NULL);
            //     pthread_mutex_unlock(&this->lock_);
            //     return 0;
            // }
            // time_t oldest_time = 2000000000;
            // std::string oldest_key;
            // for (auto it = this->pq_.begin(); it != this->pq_.end(); ++it)
            // {
            //     if (it->second < oldest_time)
            //     {
            //         oldest_time = it->second;
            //         oldest_key = it->first;
            //     }
            // }
            // this->db_.erase(oldest_key);
            // this->db_[key] = val;
            // this->pq_.erase(oldest_key);
            // this->pq_[key] = time(NULL);
            // pthread_mutex_unlock(&this->lock_);
            return 0;
        }

        size_t Size()
        {
            return this->db_.size();
        }
    private:
        std::map<KType, VType> db_;
        std::priority_queue<KeyTime, std::vector<KeyTime>, std::function<bool(const KeyTime& a, const KeyTime& b)>> pq_;
        pthread_mutex_t lock_;
        const size_t capacity_;
};
#endif /* PRIORITY_KV_CACHE_H */
