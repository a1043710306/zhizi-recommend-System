#include <stdio.h>
#include <map>

#include "util.h"
#include "db.h"

int32_t TIdMap::GetNewId(inv::INV_Mysql::MysqlData& records, const std::string& oldId)
{
    // std::string sql = "SELECT * FROM " + this->m_strTable + " WHERE content_id='" + infoId + "' AND cp_version='" + ver + "';";
    std::string sql = "SELECT * FROM " + this->m_strTable + " WHERE old_id='" + oldId + "';";

    try
    {
        records = this->m_pConn->queryRecord(sql);
        if (records.size() <= 0)
        {
            this->m_strLastErrorMsg = "no record";
            return 1;
        }
    }
    catch (inv::INV_Mysql_Exception &e)
    {
        this->m_strLastErrorMsg = e.what();
        return -1;
    }
    return 0;
}

int32_t TSignal::GetRecordsByInfoId(inv::INV_Mysql::MysqlData& records, const std::vector<std::string>& infoIds)
{
    // FUNC_GUARD();
    std::string sql = "SELECT * FROM " + this->m_strTable + " WHERE content_id IN (";// + infoId + "' ORDER BY cp_version DESC LIMIT 1;";
    for (std::vector<std::string>::const_iterator it = infoIds.begin(); it != infoIds.end(); ++it)
    {
        sql += "'" + *it + "',";
    }
    sql += "'') ORDER BY content_id, cp_version";

    try
    {
        records = this->m_pConn->queryRecord(sql);
        if (records.size() <= 0)
        {
            this->m_strLastErrorMsg = "no record";
            return 1;
        }
    }
    catch (inv::INV_Mysql_Exception &e)
    {
        this->m_strLastErrorMsg = e.what();
        return -1;
    }
    return 0;
}

int32_t TSignal::GetRecordsByInfoId(inv::INV_Mysql::MysqlData& records, const std::string& infoId)
{
    FUNC_GUARD();
    // std::string sql = "SELECT * FROM " + this->m_strTable + " WHERE content_id='" + infoId + "' AND cp_version='" + ver + "';";
    std::string sql = "SELECT * FROM " + this->m_strTable + " WHERE content_id='" + infoId + "' ORDER BY cp_version DESC LIMIT 1;";

    try
    {
        records = this->m_pConn->queryRecord(sql);
        if (records.size() <= 0)
        {
            this->m_strLastErrorMsg = "no record";
            return 1;
        }
    }
    catch (inv::INV_Mysql_Exception &e)
    {
        this->m_strLastErrorMsg = e.what();
        return -1;
    }
    return 0;
}

int32_t TSignal::GetRecordsByInfoId(inv::INV_Mysql::MysqlData& records, const std::string& infoId, const std::string& tableSuffix)
{
    FUNC_GUARD();
    std::string sql = "SELECT * FROM " + this->m_strTable + "_" + tableSuffix +" WHERE content_id='" + infoId + "' ORDER BY cp_version DESC LIMIT 1;";
    try
    {
        records = this->m_pConn->queryRecord(sql);
        if (records.size() <= 0)
        {
            this->m_strLastErrorMsg = sql+"no record";
            return 1;
        }
    }
    catch (inv::INV_Mysql_Exception &e)
    {
        this->m_strLastErrorMsg = e.what();
        return -1;
    }
    return 0;
}

int32_t TBlackTags::GetAllBlackWords(inv::INV_Mysql::MysqlData& records)
{
    // FUNC_GUARD();
    // std::string sql = "SELECT * FROM " + this->m_strTable + " WHERE content_id='" + infoId + "' AND cp_version='" + ver + "';";
    std::string sql = "SELECT word FROM " + this->m_strTable + " WHERE status='1'";

    try
    {
        records = this->m_pConn->queryRecord(sql);
        if (records.size() <= 0)
        {
            this->m_strLastErrorMsg = "no record";
            return 1;
        }
    }
    catch (inv::INV_Mysql_Exception &e)
    {
        this->m_strLastErrorMsg = e.what();
        return -1;
    }
    return 0;
}

int32_t TIndex::GetTabelIndexByContentId(inv::INV_Mysql::MysqlData& records, const std::string& contentId)
{
    //SELECT table_name FROM t_index WHERE content_id;
    std::string sql = "SELECT table_name FROM " + this->m_strTable + " WHERE content_id='"+ contentId +"'";

    try
    {
        records = this->m_pConn->queryRecord(sql);
        if (records.size() <= 0)
        {
            this->m_strLastErrorMsg = sql+"no record";
            return 1;
        }
    }
    catch (inv::INV_Mysql_Exception &e)
    {
        this->m_strLastErrorMsg = e.what();
        return -1;
    }
    return 0;
}

/* TQuery begin ***/
const StrLiter SELECT_TOPICS_FROM_T_CONTENT_EXTEND_BY_CONTENT_ID = "SELECT topics as extend_topics from t_content_extend where content_id='%s'";
const StrLiter SELECT_KEYWORDS_AND_PUBLISHER_FROM_T_CONTENT_YYYY_MM_BY_CONTENT_ID = "SELECT keywords as content_keywords, publisher as content_publisher from t_content_%s where content_id='%s'";

#define X(a, b) [a]=StrLiter(b),
const StrLiter TQueryTable[TQUERY_OVERFLOW + 1] = {
        TQUERY_TABLE
};
#undef X

#define X(a, b, c) [a]={StrLiter(b), StrLiter(c)},
const StrLiter EventTypeTableStr[3][2] ={
    EVENT_TYPE_TABLE
};
#undef X

int TQuery::Query(inv::INV_Mysql::MysqlData& records, const char* sql) {
    try
    {
        records = this->m_pConn->queryRecord(sql);
        if (records.size() <= 0)
        {
            m_strLastErrorMsg=sql;
            m_strLastErrorMsg+="no record";

            return 1;
        }
    } catch (inv::INV_Mysql_Exception &e) {
        this->m_strLastErrorMsg = e.what();

        return -1;
    }

    return 0;
}

int TQuery::Query(inv::INV_Mysql::MysqlData& records, const std::string& sql) {
    try
    {
        records = this->m_pConn->queryRecord(sql);
        if (records.size() <= 0)
        {
            m_strLastErrorMsg =sql + "no record";

            return 1;
        }
    } catch (inv::INV_Mysql_Exception &e) {
        this->m_strLastErrorMsg = e.what();

        return -1;
    }

    return 0;
}
/* TQuery end ***/

// int main()
// {
//     boost::shared_ptr<inv::INV_Mysql> p(new inv::INV_Mysql);
//     p->init("192.168.1.2", "monty", "123", "db_mta", "utf8", 3306);
// 
//     TSignal ts(p);
// 
//     inv::INV_Mysql::MysqlData records;
//     int ret = ts.GetRecordsByInfoId(records, "12");
// 
//     printf("result: %d, %s\n", ret, ts.GetLastErrMsg().c_str());
//     if (ret == 0)
//     {
//         for (std::vector<std::map<std::string, std::string> >::const_iterator i = records.data().begin() ; i != records.data().end(); ++i)
//         {
//             for (std::map<std::string, std::string>::const_iterator it = i->begin(); it != i->end(); ++it)
//             {
//                 printf("%s:%s\n", it->first.c_str(), it->second.c_str());
//             }
//             printf("\n");
//         }
//     }
//     return 0;
// }

//gcc -I../../../common/inutil/ -I/usr/include/mysql/ tsignal.cpp /usr/local/inutil/libinvutil.a -L/usr/local/mysql/lib/mysql -lmysqlclient -lstdc++
