#ifndef TSIGNAL_H
#define TSIGNAL_H

// #include <boost/shared_ptr.hpp>
#include <memory>
//#include "inv_mysql.h"
#include "util/inv_mysql.h"
#include"const_str.h"

class TBlackTags
{
    public:
        explicit TBlackTags(std::shared_ptr<inv::INV_Mysql> pConn)
            : m_strTable("t_tags_blacklist"), m_pConn(pConn)
        {
        }
        virtual ~TBlackTags()
        {
        }

        //int32_t GetRecordsByInfoId(inv::INV_Mysql::MysqlData& records, const std::string& infoId, const std::string& ver);
        int32_t GetAllBlackWords(inv::INV_Mysql::MysqlData& records);

        const std::string& GetLastErrMsg() const
        {
            return this->m_strLastErrorMsg;
        }

    private:
        const std::string m_strTable;
        std::shared_ptr<inv::INV_Mysql> m_pConn;
        std::string m_strLastErrorMsg;
};

class TSignal
{
    public:
        explicit TSignal(std::shared_ptr<inv::INV_Mysql> pConn)
            : m_strTable("t_signal"), m_pConn(pConn)
        {
        }
        virtual ~TSignal()
        {
        }

        //int32_t GetRecordsByInfoId(inv::INV_Mysql::MysqlData& records, const std::string& infoId, const std::string& ver);
        int32_t GetRecordsByInfoId(inv::INV_Mysql::MysqlData& records, const std::string& infoId);
        int32_t GetRecordsByInfoId(inv::INV_Mysql::MysqlData& records, const std::vector<std::string>& infoIds);
        int32_t GetRecordsByInfoId(inv::INV_Mysql::MysqlData& records, const std::string& infoId, const std::string& tableSuffix);

        const std::string& GetLastErrMsg() const
        {
            return this->m_strLastErrorMsg;
        }

    private:
        const std::string m_strTable;
        std::shared_ptr<inv::INV_Mysql> m_pConn;
        std::string m_strLastErrorMsg;
};

class TIdMap
{
    public:
        explicit TIdMap(std::shared_ptr<inv::INV_Mysql> pConn)
            : m_strTable("t_id_map"), m_pConn(pConn)
        {
        }
        virtual ~TIdMap()
        {
        }

        int32_t GetNewId(inv::INV_Mysql::MysqlData& records, const std::string& oldId);

        const std::string& GetLastErrMsg() const
        {
            return this->m_strLastErrorMsg;
        }

    private:
        const std::string m_strTable;
        std::shared_ptr<inv::INV_Mysql> m_pConn;
        std::string m_strLastErrorMsg;
};

class TIndex
{
    public:
        explicit TIndex(std::shared_ptr<inv::INV_Mysql> pConn)
            : m_strTable("t_index"), m_pConn(pConn)
        {
        }
        virtual ~TIndex()
        {
        }

        int32_t GetTabelIndexByContentId(inv::INV_Mysql::MysqlData& records, const std::string& contentId);

        const std::string& GetLastErrMsg() const
        {
            return this->m_strLastErrorMsg;
        }

    private:
        const std::string m_strTable;
        std::shared_ptr<inv::INV_Mysql> m_pConn;
        std::string m_strLastErrorMsg;
};

/* TQuery begin ***/
class TQuery
{
    public:
        explicit TQuery(std::shared_ptr<inv::INV_Mysql> pConn)
            : m_pConn(pConn) {
                m_strLastErrorMsg.clear();
        }

        virtual ~TQuery() {
        }

        int Query(inv::INV_Mysql::MysqlData& records, const char* sql);
        int Query(inv::INV_Mysql::MysqlData& records, const std::string& sql);

        const std::string& GetLastErrMsg() const
        {
            return m_strLastErrorMsg;
        }

    private:
        std::shared_ptr<inv::INV_Mysql> m_pConn;
        std::string m_strLastErrorMsg;
};

extern const StrLiter SELECT_TOPICS_FROM_T_CONTENT_EXTEND_BY_CONTENT_ID;
extern const StrLiter SELECT_KEYWORDS_AND_PUBLISHER_FROM_T_CONTENT_YYYY_MM_BY_CONTENT_ID; 

#if 0
#define TQUERY_TABLE \
    X(TQUERY_CONTENT_PUBLISHER, "content_publisher") \
X(TQUERY_CONTENT_KEYWORDS, "content_keywords") \
X(TQUERY_EXTEND_TOPICS, "extend_topics") \
X(TQUERY_UFS_TS, "ufs_ts") \
X(TQUERY_OVERFLOW, "overflow")
#else
#define TQUERY_TABLE \
    X(TQUERY_CONTENT_PUBLISHER, "content_publisher") \
X(TQUERY_CONTENT_KEYWORDS, "content_keywords") \
X(TQUERY_EXTEND_TOPICS, "extend_topics") \
X(TQUERY_CONTENT_PUBLISHER_IMP, "content_publisher_imp") \
X(TQUERY_CONTENT_KEYWORDS_IMP, "content_keywords_imp") \
X(TQUERY_EXTEND_TOPICS_IMP, "extend_topics_imp") \
X(TQUERY_OVERFLOW, "overflow")
#endif

#define X(a, b) a,
enum ETQUERY_TABLE {
    TQUERY_TABLE
};
#undef X

constexpr int TQUERY_FIELDS_SIZE = (TQUERY_OVERFLOW>>1);
extern const StrLiter TQueryTable[TQUERY_OVERFLOW + 1]; 

#define KAFKA_CONTENT_TYPE_TABLE \
    X(KAFKA_CONTENT_TYPE_NEWS, "news") \
    X(KAFKA_CONTENT_TYPE_SHORT_VIDEO, "short_video") \
    X(KAFKA_CONTENT_TYPE_BIG, "big_image") 

/* TQuery end ***/
#define EVENT_TYPE_TABLE \
    X(EVENT_DEFAL, "default", "default-reformat") \
    X(EVENT_CLICK, "click", "click-reformat") \
X(EVENT_IMP, "imp", "impression-reformat")

#define X(a, b, c) a,
enum EventType {
    EVENT_TYPE_TABLE
};
#undef X

extern const StrLiter EventTypeTableStr[3][2];


/* event type */


#endif
