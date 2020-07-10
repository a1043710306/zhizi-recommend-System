#include "rapidjson/writer.h"
#include "rapidjson/stringbuffer.h"
#include "rapidjson/error/en.h"
#include "rapidjson/document.h"     // rapidjson's DOM-style API

#include "event_parser.h"

int32_t EventParser::timeZone_ = 8;

int32_t EventParser::ParseImpressionMsg(Event& ev, const std::string& kafkaMsg)
{
    // {"app":"coolpad","app_lan":"zh_cn","app_ver":"other","article_impression_extra":{"content_id":"48621742","content_type":"news","cpack":{"strategy":"recommendation"},"server_time":"2016-10-26 19:42:03"},"event_id":"2","event_time":"1477482123","gate_ip":"192.168.1.28","language":"zh_cn","log_time":"2016-10-26 19:42:03","log_type":"request","mcc":"","model":"Coolpad+7295C","network":"wifi","osv":"","product_id":"coolpad","promotion":"coolpad","protocol":"http","scenario":{"channel":"0","desc":"waterfall","position":"1","position_type":"0"},"uid":"862073022342129","upack":{"abtest_ver":"205","ad_configid":"1","biz_configid":"b1","news_configid":"205"}}
    // {"app":"coolpad","app_lan":"zh_cn","app_ver":"9.03.012","article_impression_extra":{"content_id":"48626117","content_type":"news","cpack":{"strategy":"recommendation"},"server_time":"2016-10-26 19:42:04"},"event_id":"2","event_time":"1477482124","gate_ip":"192.168.1.27","language":"zh_cn","log_time":"2016-10-26 19:42:04","log_type":"request","mcc":"CMCC","model":"Coolpad+T2-C01","network":"wifi","osv":"19","product_id":"coolpad","promotion":"coolpad","protocol":"http","scenario":{"channel":"0","desc":"waterfall","position":"1","position_type":"0"},"uid":"99000783026134","upack":{"abtest_ver":"205","ad_configid":"1","biz_configid":"b1","news_configid":"205"}}
    rapidjson::Document d;
    d.Parse(kafkaMsg.c_str());

    if (d.HasParseError())
    {
        return -1;
    }

    if (!d.IsObject())
    {
        return -1;
    }
    auto& extra = d.FindMember("article_impression_extra")->value;
    if (d.FindMember("product_id") != d.MemberEnd())
    {
        ev.app = d.FindMember("product_id")->value.GetString();
    }
    if (extra.FindMember("content_type")->value.GetString() != std::string("news"))
    {
        return 1;
    }
    if (d.FindMember("uid") == d.MemberEnd())
    {
        return -1;
    }
    ev.uid = d.FindMember("uid")->value.GetString();
    if (extra.FindMember("content_id") == extra.MemberEnd())
    {
        return -1;
    }
    ev.infoId = extra.FindMember("content_id")->value.GetString();
    if (d.FindMember("event_time") == d.MemberEnd())
    {
        ev.eventTime = time(NULL);
    }
    else
    {
        std::string timeStr = d.FindMember("event_time")->value.GetString();
        int ts = 0;
        if (EventParser::ParseTime(ts, timeStr) == 0)
        {
            ev.eventTime = ts;
        }
        else
        {
            ev.eventTime = time(NULL);
        }
    }
    if (d.FindMember("log_time") == d.MemberEnd())
    {
        ev.logTime = time(NULL);
    }
    else
    {
        std::string timeStr = d.FindMember("log_time")->value.GetString();
        int ts = 0;
        if (EventParser::ParseTime(ts, timeStr) == 0)
        {
            ev.logTime = ts;
        }
        else
        {
            ev.logTime = time(NULL);
        }
    }
    return 0;
}

int32_t EventParser::ParseClickMsg(Event& ev, const std::string& kafkaMsg)
{
    // {"app":"coolpad","app_lan":"zh_cn","app_ver":"9.03.010","article_click_extra":{"content_id":"48533922","content_type":"news","cpack":{"strategy":"recommendation"}},"event_id":"3","event_time":"1477481831","gate_ip":"192.168.1.73","language":"zh_cn","log_time":"2016-10-26 19:37:11","log_type":"click","model":"Coolpad+8675-A","network":"wifi","product_id":"coolpad","promotion":"coolpad","protocol":"http","scenario":{"channel":"0","desc":"waterfall","position":"1","position_type":"0"},"uid":"866462024552148","upack":{"abtest_ver":"218","ad_configid":"1","biz_configid":"b1","news_configid":"218"}}
    // {"app":"emui","app_lan":"zh_cn","app_ver":"unknown","article_click_extra":{"content_id":"48651885","content_type":"news","cpack":{"strategy":"recommendation"}},"event_id":"3","event_time":"1477481781","gate_ip":"192.168.1.102","language":"zh_cn","log_time":"2016-10-26 19:36:21","log_type":"uad","product_id":"emui","promotion":"emui","protocol":"http","scenario":{"channel":"0","desc":"waterfall","position":"1","position_type":"0"},"uid":"01011509102153001201000004395105","upack":{"abtest_ver":"166","ad_configid":"a1","biz_configid":"b4","news_configid":"166"}}
    rapidjson::Document d;
    d.Parse(kafkaMsg.c_str());

    if (d.HasParseError())
    {
        return -1;
    }

    if (!d.IsObject())
    {
        return -1;
    }
    auto& extra = d.FindMember("article_click_extra")->value;
    if (d.FindMember("app") != d.MemberEnd())
    {
        ev.app = d.FindMember("app")->value.GetString();
    }
    if (extra.FindMember("content_type")->value.GetString() != std::string("news"))
    {
        return 1;
    }
    if (d.FindMember("uid") == d.MemberEnd())
    {
        return -1;
    }
    ev.uid = d.FindMember("uid")->value.GetString();
    if (extra.FindMember("content_id") == extra.MemberEnd())
    {
        return -1;
    }
    ev.infoId = extra.FindMember("content_id")->value.GetString();
    if (d.FindMember("event_time") == d.MemberEnd())
    {
        ev.eventTime = time(NULL);
    }
    else
    {
        std::string timeStr = d.FindMember("event_time")->value.GetString();
        int ts = 0;
        if (EventParser::ParseTime(ts, timeStr) == 0)
        {
            ev.eventTime = ts;
        }
        else
        {
            ev.eventTime = time(NULL);
        }
    }

    if (d.FindMember("log_time") == d.MemberEnd())
    {
        ev.logTime = time(NULL);
    }
    else
    {
        std::string timeStr = d.FindMember("log_time")->value.GetString();
        int ts = 0;
        if (EventParser::ParseTime(ts, timeStr) == 0)
        {
            ev.logTime = ts;
        }
        else
        {
            ev.logTime = time(NULL);
        }
    }

    ev.configId = "0";
    const auto& upack = d.FindMember("upack");
    if (upack != d.MemberEnd())
    {
        const auto& ab = upack->value.FindMember("abtest_ver");
        if (ab != upack->value.MemberEnd())
        {
            ev.configId = ab->value.GetString();
        }
    }
    return 0;
}

int32_t EventParser::ParseDislikeMsg(Event& ev, const std::string& kafkaMsg)
{
    // {"app":"hotoday","configid":{"ad":"17","biz":"2","news":"75"},"contentid":"1022961379","feedbackid":"5","feedbacktype":"1","language":"Hindi","servertime":"1477481537","uid":"01011609241414134801000007747204"}
    // {"app":"hotoday","configid":{"ad":"4","biz":"2","news":"71"},"contentid":"1022847588","feedbackid":"1","feedbacktype":"1","language":"Hindi","servertime":"1477481554","uid":"01011610261927334801000171901907"}
    // {"app":"noticias","configid":{"ad":"24","biz":"4","news":"69"},"contentid":"1022863109","feedbackid":"5","feedbacktype":"1","language":"Spanish","servertime":"1477481565","uid":"01011610261916245201000171851901"}
    // {"app":"hotoday","configid":{"ad":"17","biz":"2","news":"75"},"contentid":"1022951051","feedbackid":"4","feedbacktype":"1","language":"Hindi","servertime":"1477481572","uid":"01011610261920544801000171873305"}
    rapidjson::Document d;
    d.Parse(kafkaMsg.c_str());

    if (d.HasParseError())
    {
        return -1;
    }

    if (!d.IsObject())
    {
        return -1;
    }
    if (d.FindMember("app") != d.MemberEnd())
    {
        ev.app = d.FindMember("app")->value.GetString();
    }
    if (d.FindMember("uid") == d.MemberEnd())
    {
        return -1;
    }
    ev.uid = d.FindMember("uid")->value.GetString();
    if (d.FindMember("contentid") == d.MemberEnd())
    {
        return -1;
    }
    ev.infoId = d.FindMember("contentid")->value.GetString();

    if (d.FindMember("servertime") == d.MemberEnd())
    {
        ev.logTime = time(NULL);
    }
    else
    {
        std::string timeStr = d.FindMember("servertime")->value.GetString();
        int ts = 0;
        if (EventParser::ParseTime(ts, timeStr) == 0)
        {
            ev.logTime = ts;
        }
        else
        {
            ev.logTime = time(NULL);
        }
    }

    ev.configId = "0";
    const auto& configid = d.FindMember("configid");
    if (configid != d.MemberEnd())
    {
        const auto& ab = configid->value.FindMember("news");
        if (ab != configid->value.MemberEnd())
        {
            ev.configId = ab->value.GetString();
        }
    }
    return 0;
}

int32_t EventParser::ParseTime(int32_t& ts, const std::string& timeStr)
{
    if (timeStr.length() < 10)
    {
        return -1;
    }
    if (timeStr.length() == 10)
    {
        ts = atoi(timeStr.c_str());
        return 0;
    }
    if (timeStr.length() == 13)
    {
        ts = atol(timeStr.c_str()) / 1000;
        return 0;
    }
    return TimestampFromDateTime(ts, timeStr, EventParser::timeZone_);
}


int32_t EventParser::ParseImcMsg(Event& ev, const std::string& kafkaMsg)
{
    // {"uid":"01011611200559125101000116692700","infoId":"1027501936","app":"mata","configId":"86","eventTime":"1480306693","logTime":"1480306692","sign":"1"}
    // {"uid":"01011611110532135101000053768307","infoId":"1027308485","app":"mata","configId":"86","eventTime":"1480223658","logTime":"1480268724","sign":"-1"}
    rapidjson::Document d;
    d.Parse(kafkaMsg.c_str());

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
        return -1;
    }
    ev.uid = d.FindMember("uid")->value.GetString();
    if (d.FindMember("infoId") == d.MemberEnd())
    {
        return -1;
    }
    ev.infoId = d.FindMember("infoId")->value.GetString();
    if (d.FindMember("sign") == d.MemberEnd())
    {
        return -1;
    }
    ev.sign = atoi(d.FindMember("sign")->value.GetString());

    if (d.FindMember("app") != d.MemberEnd())
    {
        ev.app = d.FindMember("app")->value.GetString();
    }
    if (d.FindMember("eventTime") != d.MemberEnd())
    {
        ev.eventTime = atoi(d.FindMember("eventTime")->value.GetString());
    }
    if (d.FindMember("logTime") != d.MemberEnd())
    {
        ev.logTime = atoi(d.FindMember("logTime")->value.GetString());
    }
    if (d.FindMember("configId") != d.MemberEnd())
    {
        ev.configId = d.FindMember("configId")->value.GetString();
    }
    return 0;
}

