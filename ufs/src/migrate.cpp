#include <string>
#include <vector>

#include <thrift/protocol/TCompactProtocol.h>
#include <thrift/protocol/TBinaryProtocol.h>
#include <thrift/transport/TBufferTransports.h>

#include "util/inv_redis.h"
#include "util/inv_lzo.h"
#include "Algorithm_types.h"
#include "util.h"

#include "rapidjson/writer.h"
#include "rapidjson/stringbuffer.h"
#include "rapidjson/error/en.h"

template<typename ThriftStruct>
bool String2Thrift(const std::string& buff, ThriftStruct *ts) {
    if( !ts ) {
        return false;
    }

    try {
        boost::shared_ptr<apache::thrift::transport::TMemoryBuffer> buffer(new apache::thrift::transport::TMemoryBuffer());
        buffer->write((const uint8_t*)buff.data(), buff.size());
        boost::shared_ptr<apache::thrift::transport::TTransport> trans(buffer);
        apache::thrift::protocol::TCompactProtocol protocol(trans);
        ts->read(&protocol);
        return true;
    }
    catch(apache::thrift::transport::TTransportException &e) {
        // FDLOG("error") << "String2Thrift:" << ts->ascii_fingerprint << " len:" << buff.length() << " err:" << e.what() << endl;
        // cout << "String2Thrift1:" << ts->ascii_fingerprint << " len:" << buff.length() << " err:" << e.what() << endl;

    }
    catch(apache::thrift::protocol::TProtocolException &e ) {
        // FDLOG("error") << "String2Thrift:" << ts->ascii_fingerprint << " len:" << buff.length() << " err:" << e.what() << endl;

        // cout << "String2Thrift2:" << ts->ascii_fingerprint << " len:" << buff.length() << " err:" << e.what() << endl;
    }

    return false;
}
                            

std::set<int32_t> interest2categories(const std::string& interest)
{
    std::map<std::string, std::vector<std::string>> i2c;
    i2c["时事"] = {"国内", "社会", "国际", "舆评"};
    i2c["娱乐"] = {"八卦", "影视", "音乐"};
    i2c["财经"] = {"财经"};
    i2c["军事"] = {"军事"};
    i2c["文化"] = {"教育", "历史", "文艺"};
    i2c["健康"] = {"生活百科", "健康"};
    i2c["时尚"] = {"创意", "家居", "搭配", "时尚"};
    i2c["美食"] = {"美食"};
    i2c["女性"] = {"情感", "美容", "美体", "母婴", "星座"};
    i2c["旅游"] = {"旅游"};
    i2c["两性"] = {"两性"};
    i2c["搞笑"] = {"搞笑"};
    i2c["科技"] = {"互联网", "科学", "数码其他"};
    i2c["汽车"] = {"汽车"};
    i2c["体育"] = {"篮球", "足球", "体育综合"};
    i2c["美图"] = {"美女", "美图"};
    i2c["读书"] = {"小说"};
    i2c["地方"] = {"地方"};

    std::map<std::string, int32_t> c2id = {
        {"地方", 1},
        {"搞笑", 2},
        {"国际", 3},
        {"国内", 4},
        {"家居", 5},
        {"健康", 6},
        {"教育", 7},
        {"军事", 8},
        {"科技", 9},
        {"两性", 10},
        {"旅游", 11},
        {"美女", 12},
        {"美食", 13},
        {"母婴", 14},
        {"女性", 15},
        {"汽车", 16},
        {"人文", 17},
        {"社会", 18},
        {"生活", 19},
        {"时尚", 20},
        {"数码", 21},
        {"体育", 22},
        {"头条", 23},
        {"星座", 24},
        {"游戏", 25},
        {"娱乐", 26},
        {"电商", 27},
        {"财经", 28},
        {"创意", 29},
        {"音频", 30},
        {"视频", 31},
        {"周末测试分类", 33},
        {"舆评", 34},
        {"历史", 35},
        {"文艺", 36},
        {"情感", 37},
        {"美容", 38},
        {"美体", 39},
        {"互联网", 40},
        {"科学", 41},
        {"数码其他", 42},
        {"生活百科", 43},
        {"搭配", 44},
        {"手游", 45},
        {"端游", 46},
        {"页游", 47},
        {"篮球", 48},
        {"足球", 49},
        {"体育综合", 50},
        {"八卦", 51},
        {"影视", 52},
        {"音乐", 53},
        {"美图", 54},
        {"小说", 55},
        {"活动", 57},
        {"专题", 58},
        {"场景", 74},
        {"博客", 60},
        {"美容美体", 61},
        {"书籍", 62},
        {"人物", 63},
        {"工作", 64},
        {"手机应用", 65},
        {"社交媒体", 66},
        {"电脑", 67},
        {"板球", 68},
        {"舞蹈", 69},
        {"学校", 70},
        {"高等教育", 71},
        {"职业教育", 72},
        {"留学", 73},
        {"历史上的今天", 75},
        {"股票", 76},
        {"Video", 77},
        {"每日一知", 78},
        {"健身", 79},
        {"奇闻", 80},
        {"政治", 81},
        {"商业", 82},
        {"宗教", 83},
        {"心理", 84},
    };

    std::set<int32_t> res;
    if (i2c.count(interest) <= 0)
    {
        if (c2id.count(interest) > 0)
        {
            res.insert(c2id.find(interest)->second);
        }
        return res;
    }

    for (const auto& it: i2c.find(interest)->second)
    {
        res.insert(c2id.find(it)->second);
    }
    return res;
}

std::string MakeJson(std::set<int32_t>& ac)
{
    rapidjson::StringBuffer s;
    rapidjson::Writer<rapidjson::StringBuffer> writer(s);
    writer.SetMaxDecimalPlaces(3);
    writer.StartObject();
    for (const auto& k: ac)
    {
        writer.Key(std::to_string(k).c_str());
        writer.Double(1.0);
    }
    writer.EndObject();

    assert(writer.IsComplete());
    return s.GetString();
}

int parse(std::string& json, const std::string& rs)
{
    std::string drs;
    int ret = inv::INV_Lzo::getInstance()->lzo_decompress(rs, drs);
    if (ret != 0)
    {
        fprintf(stderr, "lzo_decompress error: %d\n", ret);
        return -1;
    }
    inveno::UserReaded readed;
    ret = String2Thrift(drs, &readed);
    if (ret == false)
    {
        fprintf(stderr, "String2Thrift error: %d\n", ret);
        return -1;
    }
    // for (const auto& it: readed.cou_types)
    // {
    //     fprintf(stdout, "key: --%s||\n", it.first.c_str());
    //     for (const auto& jt: it.second)
    //     {
    //         fprintf(stdout, "v: %s\n", jt.first.c_str());
    //     }
    // }
    if (readed.cou_types.count("") <= 0)
    {
        return 0;
    }
    std::set<int32_t> ac;
    for (const auto& it: readed.cou_types[""])
    {
        std::set<int32_t> tmp = interest2categories(it.first);
        if (tmp.empty())
        {
            fprintf(stdout, "not found: %s\n", it.first.c_str());
        }
        for (const auto& jt: tmp)
        {
            ac.insert(jt);
        }
    }
    if (!ac.empty())
    {
        json = MakeJson(ac);
    }
    // json = "[";
    // for (const auto& k: ac)
    // {
    //     json += "\"" + std::to_string(k) + "\",";
    // }
    // if (json[json.length() - 1] == ',')
    // {
    //     json[json.length() - 1] = ']';
    // }
    // else
    // {
    //     json += "]";
    // }

    // fprintf(stdout, "%s\n", json.c_str());
    return 0;
}

//exe ip port hash addr
int main(int argc, char** argv)
{
    int cursor = 0;
    const std::string match = "userreaded_*";
    inv::INV_Redis redis;
    redis.init(argv[1], atoi(argv[2]), 1000);
    redis.connect();

    const std::string hash = argv[3];
    const std::string addrs = argv[4];
    std::vector<inv::INV_Redis*> ufsConns;

    {
        std::vector<std::string> fields = inv::INV_Util::sepstr<string>(addrs, ",", true);
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
            // pcf->addrs.push_back({host, port, timeout});
            inv::INV_Redis* conn = new inv::INV_Redis;
            conn->init(host, port, 1000);
            conn->connect();
            ufsConns.push_back(conn);
        }
    }

    while (true)
    {
        std::vector<std::string> keys;
        cursor = redis.scan(cursor, keys, match);
        if (cursor <= 0)
        {
            fprintf(stderr, "error cursor: %d\n", cursor);
            break;
        }
        std::vector<std::string> vals;
        int ret = redis.mget(keys, vals);
        if (ret != 0)
        {
            fprintf(stderr, "redis mget: %d\n", ret);
            continue;
        }
        // for (const auto& it: vals)
        assert(vals.size() == keys.size());
        for (size_t i = 0; i < vals.size(); i++)
        {
            std::string uid = keys[i].substr(std::string("userreaded_").length());
            std::string json;
            parse(json, vals[i]);
            if (json.empty())
            {
                continue;
                // fprintf(stdout, "%s\t%s\t%s\n", uid.c_str(), keys[i].c_str(), json.c_str());
            }
            size_t idx = GetIdxOfConns(uid, ufsConns.size(), "");
            inv::INV_Redis* conn = ufsConns[idx];
            RedisHashSet(conn, hash, uid, json);
        }
    }

    return 0;
}

// gcc -omigrate -std=c++0x -I/usr/local/include/hiredis -I../../common/inutil/ -Igen-cpp src/migrate.cpp gen-cpp/Algorithm_types.cpp gen-cpp/Info_types.cpp gen-cpp/Enterprise_types.cpp gen-cpp/FeederInfo_types.cpp gen-cpp/Common_types.cpp gen-cpp/Personal_types.cpp /usr/local/thrift-0.9.2/lib/libthrift.a ../../common/inutil/libinvutil.a /usr/local/lib/libhiredis.a /usr/local/lib/liblzo2.a /usr/local/gcc-4.8.5/lib64/libstdc++.a -pthread -lm
