#ifndef KAFKA_CONSUMER_H
#define KAFKA_CONSUMER_H 
#include <vector>
#include <string>

extern "C" {
#include "rdkafka.h"  /* for Kafka driver */
}

class KafkaConsumer
{
    public:
        KafkaConsumer(const std::vector<std::pair<std::string, uint16_t> >& brokers,
                const std::string& group, const std::string& topic);
        virtual ~KafkaConsumer();
        int32_t Init();
        // void SetLogCallback();
        int32_t FetchMsg(std::string& msg);
        // void SetConsumeCallback();
        // int32_t Run();
        std::string GetLastErrMsg() const 
        {
            return this->errmsg_;
        }
    private:
        std::string errmsg_;

        bool inited_;
        std::vector<std::pair<std::string, uint16_t> > brokers_;
        std::string group_;
        std::string topic_;

        rd_kafka_t* rk_;
        rd_kafka_conf_t* conf_;
        rd_kafka_topic_conf_t* topic_conf_;
        rd_kafka_resp_err_t err_;
        rd_kafka_topic_partition_list_t* topics_;

        static void RebalanceCb(rd_kafka_t *rk,
                rd_kafka_resp_err_t err,
                rd_kafka_topic_partition_list_t *partitions,
                void *opaque);
};

#endif /* KAFKA_CONSUMER_H*/
