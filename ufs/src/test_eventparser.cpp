#include "event_parser.h"

#include "gtest/gtest.h"

TEST(eventparser, click)
{
    std::string clickMsg = R"ss({"app":"coolpad","app_lan":"zh_cn","app_ver":"9.03.010","article_click_extra":{"content_id":"48533922","content_type":"news","cpack":{"strategy":"recommendation"}},"event_id":"3","event_time":"1477481831","gate_ip":"192.168.1.73","language":"zh_cn","log_time":"2016-10-26 19:37:11","log_type":"click","model":"Coolpad+8675-A","network":"wifi","product_id":"coolpad","promotion":"coolpad","protocol":"http","scenario":{"channel":"0","desc":"waterfall","position":"1","position_type":"0"},"uid":"866462024552148","upack":{"abtest_ver":"218","ad_configid":"1","biz_configid":"b1","news_configid":"218"}})ss";

    EventParser::Event ev;
    ASSERT_EQ(0, EventParser::ParseClickMsg(ev, clickMsg));
    EXPECT_EQ("coolpad", ev.app);
    EXPECT_EQ("866462024552148", ev.uid);
    EXPECT_EQ("48533922", ev.infoId);
    EXPECT_EQ("218", ev.configId);
    EXPECT_EQ(1477481831, ev.eventTime);
    EXPECT_EQ(1477481831, ev.logTime);
}

TEST(eventparser, impression)
{
    std::string clickMsg = R"ss({"app":"coolpad","app_lan":"zh_cn","app_ver":"9.03.012","article_impression_extra":{"content_id":"48626117","content_type":"news","cpack":{"strategy":"recommendation"},"server_time":"2016-10-26 19:42:04"},"event_id":"2","event_time":"1477482124","gate_ip":"192.168.1.27","language":"zh_cn","log_time":"2016-10-26 19:42:03","log_type":"request","mcc":"CMCC","model":"Coolpad+T2-C01","network":"wifi","osv":"19","product_id":"coolpad","promotion":"coolpad","protocol":"http","scenario":{"channel":"0","desc":"waterfall","position":"1","position_type":"0"},"uid":"99000783026134","upack":{"abtest_ver":"205","ad_configid":"1","biz_configid":"b1","news_configid":"205"}})ss";

    EventParser::Event ev;
    ASSERT_EQ(0, EventParser::ParseImpressionMsg(ev, clickMsg));
    EXPECT_EQ("coolpad", ev.app);
    EXPECT_EQ("99000783026134", ev.uid);
    EXPECT_EQ("48626117", ev.infoId);
    // EXPECT_EQ("205", ev.configId);
    EXPECT_EQ(1477482124, ev.eventTime);
    EXPECT_EQ(1477482123, ev.logTime);
}

TEST(eventparser, dislike)
{
    std::string clickMsg = R"ss({"app":"hotoday","configid":{"ad":"17","biz":"2","news":"75"},"contentid":"1022951051","feedbackid":"4","feedbacktype":"1","language":"Hindi","servertime":"1477481572","uid":"01011610261920544801000171873305"})ss";

    EventParser::Event ev;
    ASSERT_EQ(0, EventParser::ParseDislikeMsg(ev, clickMsg));
    EXPECT_EQ("hotoday", ev.app);
    EXPECT_EQ("01011610261920544801000171873305", ev.uid);
    EXPECT_EQ("1022951051", ev.infoId);
    EXPECT_EQ("75", ev.configId);
    // EXPECT_EQ(1477481831, ev.eventTime);
    EXPECT_EQ(1477481572, ev.logTime);
}

