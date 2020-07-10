#ifndef LINKED_KV_CACHE_H
#define LINKED_KV_CACHE_H 
#include <unordered_map>
#include <vector>
#include <string>
#include <ctime>
#include <cassert>
#include <list>

template<class KType, class VType>
class LinkedKVCache
{
    public:
        explicit LinkedKVCache(size_t num)
            :counter_(0), capacity_(num)
        {
            this->db_.reserve(this->capacity_ + 10);
            // assert(pthread_mutex_init(&this->lock_, NULL) == 0);
        }
        virtual ~LinkedKVCache()
        {
            // pthread_mutex_destroy(&this->lock_);
        }
        LinkedKVCache(LinkedKVCache const &) = delete;
        LinkedKVCache& operator=(LinkedKVCache const &) = delete;

        size_t Size()
        {
            return this->db_.size();
        }

        int32_t Get(VType& val, const KType& key)
        {
            // FUNC_GUARD();
            // pthread_mutex_lock(&this->lock_);
            if (this->db_.count(key) <= 0)// not exist
            {
                // pthread_mutex_unlock(&this->lock_);
                return 1;
            }

            val = this->db_[key].val;
            this->touch(key);

            // pthread_mutex_unlock(&this->lock_);
            return 0;
        }

        int32_t Set(const KType& key, const VType& val)
        {
            // FUNC_GUARD();
            // pthread_mutex_lock(&this->lock_);
            if (this->db_.count(key) > 0)// exist already
            {
                // assert(this->lru_.count(key) > 0);
                this->db_[key].val = val;
                this->touch(key);
                // this->lru_[key] = time(NULL);
                // pthread_mutex_unlock(&this->lock_);
                return 0;
            }
            // assert(this->lru_.count(key) <= 0);

            if (this->db_.size() >= this->capacity_)
            {
                assert(this->db_.size() == this->capacity_);
                const KType& tk = *this->lru_.begin();
                this->db_.erase(tk);
                this->lru_.pop_front();
                --this->counter_;
                // assert(this->db_.size() == this->lru_.size());
            }

            assert(this->db_.size() < this->capacity_);// not full, insert
            this->add(key, val);

            // if (this->lru_.size() < this->capacity_)// not full, insert
            // {
            //     this->db_[key] = val;
            //     this->lru_[key] = time(NULL);
            //     pthread_mutex_unlock(&this->lock_);
            //     return 0;
            // }
            // time_t oldest_time = 2000000000;
            // std::string oldest_key;
            // for (auto it = this->lru_.begin(); it != this->lru_.end(); ++it)
            // {
            //     if (it->second < oldest_time)
            //     {
            //         oldest_time = it->second;
            //         oldest_key = it->first;
            //     }
            // }
            // this->db_.erase(oldest_key);
            // this->db_[key] = val;
            // this->lru_.erase(oldest_key);
            // this->lru_[key] = time(NULL);
            // pthread_mutex_unlock(&this->lock_);
            return 0;
        }

        int Set(const KType& key, VType&& val)
        {
            if (this->db_.count(key) > 0)// exist already
            {
                this->db_[key].val = std::move(val);
                this->touch(key);

                return 0;
            }
            // assert(this->lru_.count(key) <= 0);

            if (this->db_.size() >= this->capacity_)
            {
                assert(this->db_.size() == this->capacity_);
                const KType& tk = *this->lru_.begin();
                this->db_.erase(tk);
                this->lru_.pop_front();
                --this->counter_;
                // assert(this->db_.size() == this->lru_.size());
            }

            assert(this->db_.size() < this->capacity_);// not full, insert
            this->add(key, val);

            return 0;
        }
    private:
        void touch(const KType& key)
        {
            // FUNC_GUARD();
            this->lru_.erase(this->db_[key].it);
            --this->counter_;
            this->lru_.push_back(key);
            ++this->counter_;
            this->db_[key].it = this->lru_.end();
            --(this->db_[key].it);
            assert(this->db_.size() == this->counter_);
        }

        int64_t timediff(const struct timeval& a, const struct timeval& b)
        {
            return (b.tv_sec - a.tv_sec) * 1000000 + (b.tv_usec - a.tv_usec);
        }

        void add(const KType& key, const VType& val)
        {
            // FUNC_GUARD();
            // struct timeval a, b, c, d, e;
            // gettimeofday(&a, NULL);
            this->lru_.push_back(key);
            ++this->counter_;
            // gettimeofday(&b, NULL);
            this->db_[key] = {val, this->lru_.end()};
            // gettimeofday(&c, NULL);
            --(this->db_[key].it);
            // gettimeofday(&d, NULL);
            assert(this->db_.size() == this->counter_);
            // gettimeofday(&e, NULL);
            // FDLOG("fuck") << ", " << timediff(a, b) << ", " << timediff(b, c)  << ", " << timediff(c, d) << ", " << timediff(d, e) << endl;
        }

    private:
        // struct KeyTime
        // {
        //     std::string key;
        //     time_t last_get;
        // };
        struct VnT
        {
            VType val;
            typename std::list<KType>::iterator it;
        };
        std::unordered_map<KType, VnT> db_;
        std::list<KType> lru_;
        // std::map<KType, time_t> lru_;
        // pthread_mutex_t lock_;
        size_t counter_;
        const size_t capacity_;
};

#endif /* LINKED_KV_CACHE_H */
