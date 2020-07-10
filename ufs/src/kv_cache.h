#ifndef KV_CACHE_H
#define KV_CACHE_H 
#include <map>
#include <vector>
#include <string>
#include <ctime>
#include <cassert>

#include<pthread.h>

template<class KType, class VType>
class KVCache
{
    public:
        explicit KVCache(size_t num)
            :num_(num)
        {
            assert(pthread_mutex_init(&this->lock_, NULL) == 0);
        }
        virtual ~KVCache()
        {
            pthread_mutex_destroy(&this->lock_);
        }
        KVCache(KVCache const &) = delete;
        KVCache& operator=(KVCache const &) = delete;

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

        int32_t Set(const KType& key, const VType& val)
        {
            pthread_mutex_lock(&this->lock_);
            if (this->db_.count(key) > 0)// exist already
            {
                assert(this->lru_.count(key) > 0);
                this->db_[key] = val;
                this->lru_[key] = time(NULL);
                pthread_mutex_unlock(&this->lock_);
                return 0;
            }
            assert(this->lru_.count(key) <= 0);
            if (this->lru_.size() < this->num_)// not full
            {
                this->db_[key] = val;
                this->lru_[key] = time(NULL);
                pthread_mutex_unlock(&this->lock_);
                return 0;
            }
            time_t oldest_time = 2000000000;
            std::string oldest_key;
            for (auto it = this->lru_.begin(); it != this->lru_.end(); ++it)
            {
                if (it->second < oldest_time)
                {
                    oldest_time = it->second;
                    oldest_key = it->first;
                }
            }
            this->db_.erase(oldest_key);
            this->db_[key] = val;
            this->lru_.erase(oldest_key);
            this->lru_[key] = time(NULL);
            pthread_mutex_unlock(&this->lock_);
            return 0;
        }
    private:
        // struct KeyTime
        // {
        //     std::string key;
        //     time_t last_get;
        // };
        std::map<KType, VType> db_;
        std::map<KType, time_t> lru_;
        pthread_mutex_t lock_;
        const size_t num_;
};
#endif /* KV_CACHE_H */
