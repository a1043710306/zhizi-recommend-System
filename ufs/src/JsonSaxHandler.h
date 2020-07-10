#ifndef JSONSAXHANDLER_H
#define JSONSAXHANDLER_H 
#include <cstdlib>
#include <cmath>
#include <string>
#include <map>

#include "rapidjson/reader.h"

// using namespace rapidjson;

class JsonSaxHandler
{
    public:
        JsonSaxHandler(double decayRate, uint32_t decayPeriod)
            :state_(JsonSaxHandler::STATE_INIT),
        waitTs_(false), waitWeight_(false), waitTag_(false),
        decayRate_(decayRate),
        decayFactor_(0.0), decayPeriod_(decayPeriod)
        {
        }
    private:
        enum ParseState
        {
            STATE_INIT = 0,
            STATE_ROOT_OBJECT_OPEN = 1,
            STATE_ROOT_OBJECT_CLOSED = 2,
            STATE_WEIGHT_ARRAY_OPEN = 3,
            STATE_WEIGHT_ARRAY_CLOSED = 4,
            STATE_WEIGHT_OBJECT_OPEN = 5,
            STATE_WEIGHT_OBJECT_CLOSED = 6,
        };

        JsonSaxHandler::ParseState state_;
        bool waitTs_;
        bool waitWeight_;
        bool waitTag_;

        double decayRate_;
        double decayFactor_;
        uint32_t decayPeriod_;

        std::string tag_;
        std::map<std::string, double> resMap_;
    public:

        bool Null(){return false;};
        bool Bool(bool b){return false;};
        bool Int(int i){return false;};
        bool Uint(unsigned i){return false;};
        bool Int64(int64_t i){return false;};
        bool Uint64(uint64_t i){return false;};
        bool Double(double d){return false;};

        bool RawNumber(const char* str, rapidjson::SizeType length, bool copy)
        {
            if (waitTs_)
            {
                int32_t ts = atoi(str);
                if (ts <= 0)
                {
                    ts = time(NULL);
                }
                decayFactor_ = std::pow(decayRate_, (time(NULL) - ts) / decayPeriod_);
                waitTs_ = false;
            }
            else if (waitWeight_)
            {
                double weight = atof(str) * decayFactor_;
                resMap_[tag_] = weight;
                tag_.clear();
                waitWeight_ = false;
            }
            return true;
        };
        bool String(const char* str, rapidjson::SizeType length, bool copy)
        {
            if (waitTag_)
            {
                assert(tag_.empty());
                tag_ = std::string(str, length);
                waitTag_ = false;
            }
            return true;
        };
        bool StartObject()
        {
            if (state_ == JsonSaxHandler::STATE_INIT)
            {
                state_ = JsonSaxHandler::STATE_ROOT_OBJECT_OPEN;
                return true;
            }
            if (state_ == JsonSaxHandler::STATE_WEIGHT_ARRAY_OPEN || state_ == JsonSaxHandler::STATE_WEIGHT_OBJECT_CLOSED)
            {
                state_ = JsonSaxHandler::STATE_WEIGHT_OBJECT_OPEN;
                return true;
            }
            return false;
        };
        bool Key(const char* str, rapidjson::SizeType length, bool copy)
        {
            if (state_ == JsonSaxHandler::STATE_ROOT_OBJECT_OPEN)
            {
                if (std::string(str, length) == "ts")
                {
                    waitTs_ = true;
                }
            }
            if (state_ == JsonSaxHandler::STATE_WEIGHT_OBJECT_OPEN)
            {
                if (std::string(str, length) == "tag")
                {
                    waitTag_ = true;
                }
                else if (std::string(str, length) == "weight")
                {
                    waitWeight_ = true;
                }
            }
            return true;
        };
        bool EndObject(rapidjson::SizeType memberCount)
        {
            if (state_ == JsonSaxHandler::STATE_WEIGHT_OBJECT_OPEN)
            {
                state_ = JsonSaxHandler::STATE_WEIGHT_OBJECT_CLOSED;
                return true;
            }
            if (state_ == JsonSaxHandler::STATE_WEIGHT_ARRAY_CLOSED)
            {
                state_ = JsonSaxHandler::STATE_ROOT_OBJECT_CLOSED;
                return true;
            }
            return false;
        };
        bool StartArray()
        {
            if (state_ == JsonSaxHandler::STATE_ROOT_OBJECT_OPEN)
            {
                state_ = JsonSaxHandler::STATE_WEIGHT_ARRAY_OPEN;
                return true;
            }
            return false;
        };
        bool EndArray(rapidjson::SizeType elementCount)
        {
            if (state_ == JsonSaxHandler::STATE_WEIGHT_ARRAY_OPEN)
            {
                state_ = JsonSaxHandler::STATE_WEIGHT_ARRAY_CLOSED;
                return true;
            }
            if (state_ == JsonSaxHandler::STATE_WEIGHT_OBJECT_CLOSED)
            {
                state_ = JsonSaxHandler::STATE_WEIGHT_ARRAY_CLOSED;
                return true;
            }
            return false;
        };
        const std::map<std::string, double>& GetResult() const
        {
            return resMap_;
        }
};
#endif /* JSONSAXHANDLER_H */

// #include <cassert>
// 
// #include "JsonSaxHandler.h"
// #include "rapidjson/reader.h"
// #include "rapidjson/document.h"
// 
// std::string json = R"xxjson({"config":"102","ts":1472037966,"app":"coolpad","weighted":[{"tag":"男人","weight":10.27},{"tag":"丰胸方法","weight":7.083},{"tag":"胸部","weight":5.378},{"tag":"劫匪","weight":4.273},{"tag":"硬币","weight":4.254},{"tag":"乳腺增生","weight":4.056},{"tag":"婴儿","weight":3.984},{"tag":"手术","weight":3.76},{"tag":"稀土","weight":3.461},{"tag":"宝马车","weight":2.917},{"tag":"双腿","weight":2.804},{"tag":"陈夫人","weight":2.748},{"tag":"子宫颈扩张","weight":2.746},{"tag":"女人","weight":2.745},{"tag":"眼神","weight":2.702},{"tag":"大胸","weight":2.689},{"tag":"眼睛","weight":2.502},{"tag":"军事对抗","weight":2.47},{"tag":"wifi网络","weight":2.41},{"tag":"母亲会","weight":2.322},{"tag":"35b","weight":2.299},{"tag":"车子","weight":2.291},{"tag":"车主","weight":2.252},{"tag":"的姐","weight":2.246},{"tag":"大箱子","weight":2.222},{"tag":"不法之徒","weight":2.093},{"tag":"乘客","weight":2.073},{"tag":"不雅照","weight":2.028},{"tag":"女儿","weight":1.989},{"tag":"司令","weight":1.909},{"tag":"自卑","weight":1.888},{"tag":"海参","weight":1.867},{"tag":"肿块","weight":1.801},{"tag":"航母","weight":1.634},{"tag":"开着","weight":1.605},{"tag":"陈家","weight":1.584},{"tag":"病房","weight":1.573},{"tag":"路虎","weight":1.533},{"tag":"回收","weight":1.531},{"tag":"生肖","weight":1.518},{"tag":"上校","weight":1.478},{"tag":"胎儿","weight":1.477},{"tag":"一块钱","weight":1.463},{"tag":"相亲","weight":1.461},{"tag":"照片","weight":1.449},{"tag":"医生","weight":1.441},{"tag":"精制","weight":1.429},{"tag":"连衣裙","weight":1.427},{"tag":"aim","weight":1.415},{"tag":"司机","weight":1.409},{"tag":"子宫穿孔","weight":1.387},{"tag":"15战机","weight":1.367},{"tag":"资源","weight":1.366},{"tag":"胀痛","weight":1.35},{"tag":"儿童","weight":1.349},{"tag":"小黄","weight":1.33},{"tag":"市委","weight":1.285},{"tag":"窗子","weight":1.275},{"tag":"菊花","weight":1.272},{"tag":"娱乐圈","weight":1.262},{"tag":"收藏人","weight":1.244},{"tag":"结婚","weight":1.244},{"tag":"手臂","weight":1.225},{"tag":"大人","weight":1.202},{"tag":"气息","weight":1.2},{"tag":"格斗导弹","weight":1.199},{"tag":"溺水","weight":1.166},{"tag":"下垂","weight":1.141},{"tag":"内衣","weight":1.134},{"tag":"世界强国","weight":1.118},{"tag":"一个人","weight":1.114},{"tag":"丰满","weight":1.102},{"tag":"惨剧","weight":1.09},{"tag":"子墨","weight":1.089},{"tag":"一间房","weight":1.074},{"tag":"纪念章","weight":1.065},{"tag":"垂直起降战斗机","weight":1.058},{"tag":"35战斗机","weight":1.051},{"tag":"课本","weight":1.051},{"tag":"军事技术","weight":1.046},{"tag":"老公","weight":1.043},{"tag":"买了","weight":1.042},{"tag":"路由器","weight":1.035},{"tag":"闪婚","weight":1.034},{"tag":"破解工具","weight":1.034},{"tag":"将车","weight":1.024},{"tag":"儿子","weight":1.014},{"tag":"bra","weight":1.013},{"tag":"怀孕","weight":1.011},{"tag":"无线网络","weight":0.983},{"tag":"修车","weight":0.979},{"tag":"医院","weight":0.973},{"tag":"皱眉","weight":0.964},{"tag":"咖啡厅","weight":0.961},{"tag":"日本方面","weight":0.956},{"tag":"大国","weight":0.941},{"tag":"市中心医院","weight":0.938},{"tag":"药物流产","weight":0.934},{"tag":"行李箱","weight":0.928},{"tag":"教练","weight":0.926},{"tag":"玻璃纤维","weight":0.924},{"tag":"脚趾","weight":0.922},{"tag":"发病率","weight":0.919},{"tag":"闺蜜","weight":0.915},{"tag":"长裤","weight":0.91},{"tag":"沙发上","weight":0.891},{"tag":"经脉","weight":0.89},{"tag":"湿了","weight":0.881},{"tag":"溺亡","weight":0.862},{"tag":"抬起","weight":0.86},{"tag":"扫描工具","weight":0.86},{"tag":"乳腺癌","weight":0.859},{"tag":"救援小组","weight":0.854},{"tag":"乳腺疾病","weight":0.851},{"tag":"母亲","weight":0.848},{"tag":"刮匙","weight":0.841},{"tag":"黑客软件","weight":0.84},{"tag":"抢救","weight":0.836},{"tag":"李庄村","weight":0.834},{"tag":"妊娠并发症","weight":0.831},{"tag":"双手","weight":0.819},{"tag":"男友","weight":0.817},{"tag":"部队","weight":0.815},{"tag":"橘子汁","weight":0.815},{"tag":"阿姨","weight":0.814},{"tag":"先进武器","weight":0.811},{"tag":"市中医院","weight":0.781},{"tag":"产下","weight":0.78},{"tag":"大姐","weight":0.772},{"tag":"出口量","weight":0.772},{"tag":"日方","weight":0.766},{"tag":"卫星制导","weight":0.765},{"tag":"明星","weight":0.761},{"tag":"穿着","weight":0.761},{"tag":"中国海空军","weight":0.755},{"tag":"豪车","weight":0.755},{"tag":"嫁给","weight":0.75},{"tag":"布料","weight":0.749},{"tag":"逃跑","weight":0.745},{"tag":"陈老","weight":0.742},{"tag":"护士","weight":0.731},{"tag":"女司机","weight":0.727},{"tag":"卧室","weight":0.727},{"tag":"故作","weight":0.72},{"tag":"雪纺","weight":0.719},{"tag":"挺胸","weight":0.716},{"tag":"真相","weight":0.716},{"tag":"玻璃窗","weight":0.705},{"tag":"练车","weight":0.705},{"tag":"引产","weight":0.702},{"tag":"说法","weight":0.702},{"tag":"不介意","weight":0.694},{"tag":"二老","weight":0.687},{"tag":"上车","weight":0.687},{"tag":"子宫内壁","weight":0.685},{"tag":"脖子","weight":0.681},{"tag":"无线设备","weight":0.68},{"tag":"点了","weight":0.68},{"tag":"铜材","weight":0.67},{"tag":"床上","weight":0.669},{"tag":"惊呼","weight":0.663},{"tag":"美国军事","weight":0.66},{"tag":"制成","weight":0.653},{"tag":"眼角","weight":0.652},{"tag":"楼梯","weight":0.649},{"tag":"美女","weight":0.638},{"tag":"载机","weight":0.632},{"tag":"绑定","weight":0.632},{"tag":"收钱","weight":0.632},{"tag":"塞进","weight":0.629},{"tag":"妹子","weight":0.619},{"tag":"生产","weight":0.618},{"tag":"作战飞机","weight":0.617},{"tag":"照顾好","weight":0.616},{"tag":"纪念币","weight":0.615},{"tag":"性意识","weight":0.613},{"tag":"人肉","weight":0.605},{"tag":"自信","weight":0.595},{"tag":"两个月","weight":0.594},{"tag":"嫁了","weight":0.591},{"tag":"看着","weight":0.59},{"tag":"厨房","weight":0.59},{"tag":"两栖攻击舰","weight":0.589},{"tag":"擦拭","weight":0.578},{"tag":"帅哥","weight":0.569},{"tag":"产妇","weight":0.566},{"tag":"额头上","weight":0.561},{"tag":"条规","weight":0.559},{"tag":"中国战机","weight":0.559},{"tag":"副作用","weight":0.558},{"tag":"面额","weight":0.553},{"tag":"如同","weight":0.553},{"tag":"摆着","weight":0.553},{"tag":"价格表","weight":0.552},{"tag":"子宫破裂","weight":0.545},{"tag":"发育不良","weight":0.538},{"tag":"打死","weight":0.535},{"tag":"密码","weight":0.532},{"tag":"孕酮","weight":0.532},{"tag":"被困","weight":0.531},{"tag":"平胸","weight":0.529},{"tag":"护卫舰","weight":0.523},{"tag":"罩杯","weight":0.509},{"tag":"搜出","weight":0.508},{"tag":"情敌","weight":0.506},{"tag":"蕾丝","weight":0.5},{"tag":"压抑","weight":0.498},{"tag":"不理","weight":0.497},{"tag":"毒素","weight":0.497},{"tag":"行李架","weight":0.495},{"tag":"子宫内膜","weight":0.494},{"tag":"波霸","weight":0.493},{"tag":"竟是","weight":0.491},{"tag":"调理","weight":0.486},{"tag":"月经紊乱","weight":0.483},{"tag":"材质","weight":0.478},{"tag":"挣钱","weight":0.474},{"tag":"航空母舰","weight":0.472},{"tag":"网络地址","weight":0.47},{"tag":"发热","weight":0.47},{"tag":"在乎","weight":0.47},{"tag":"往上提","weight":0.469},{"tag":"披肩发","weight":0.461},{"tag":"航空兵","weight":0.46},{"tag":"航行自由","weight":0.454},{"tag":"中国","weight":0.451},{"tag":"突然爆炸","weight":0.451},{"tag":"比基尼","weight":0.451},{"tag":"贤妻良母","weight":0.45},{"tag":"工业产品","weight":0.45},{"tag":"的钱","weight":0.45},{"tag":"嫉妒","weight":0.448},{"tag":"不孕","weight":0.448},{"tag":"计较","weight":0.443},{"tag":"背包","weight":0.443},{"tag":"孕妇","weight":0.442},{"tag":"贬值","weight":0.439},{"tag":"包装材料","weight":0.437},{"tag":"一瞬","weight":0.436},{"tag":"雷达","weight":0.435},{"tag":"货币","weight":0.432},{"tag":"所含","weight":0.431},{"tag":"妈妈","weight":0.429},{"tag":"治好","weight":0.426},{"tag":"知情","weight":0.423},{"tag":"漂亮","weight":0.408},{"tag":"足月","weight":0.396},{"tag":"红杏出墙","weight":0.39},{"tag":"一只手","weight":0.384},{"tag":"断奶","weight":0.376},{"tag":"焦躁","weight":0.371},{"tag":"荷尔蒙","weight":0.367},{"tag":"按摩","weight":0.357},{"tag":"吃过","weight":0.356},{"tag":"相夫教子","weight":0.355},{"tag":"比你","weight":0.327},{"tag":"羊水","weight":0.325},{"tag":"胚胎","weight":0.324},{"tag":"撕裂伤","weight":0.32},{"tag":"大出血","weight":0.311},{"tag":"胎盘","weight":0.306}]})xxjson";
// 
// std::map<std::string, double> DomParse(const std::string& json, double rate, uint32_t period)
// {
//     std::map<std::string, double> out;
//     rapidjson::Document d;
// 
//     d.Parse(json.c_str());
//     assert(!d.HasParseError());
// 
//     double decayFactor = std::pow(rate, (time(NULL) - d.FindMember("ts")->value.GetInt()) / period);
//     const rapidjson::Value& obj = d.FindMember("weighted")->value;
//     for (rapidjson::Value::ConstValueIterator itr = obj.Begin();
//             itr != obj.End(); ++itr)
//     {
//         std::string tagKey = itr->GetObject().FindMember("tag")->value.GetString();
//         double tagWeight = itr->GetObject().FindMember("weight")->value.GetDouble() * decayFactor;
//         out.insert(std::make_pair(tagKey, tagWeight));
//     }
//     return out;
// }
// 
// int main(int argc, char** argv)
// {
//     double decayRate = atof(argv[1]);
//     uint32_t decayPeriod = atoi(argv[2]);
// 
//     JsonSaxHandler jsh(decayRate, decayPeriod);
//     rapidjson::Reader reader;
// 
//     rapidjson::StringStream ss(json.c_str());
//     bool ret = reader.Parse<rapidjson::kParseNumbersAsStringsFlag>(ss, jsh);
// 
//     assert(ret);
//     printf("res:%d, %s\n", ret, json.c_str());
//     
//     std::map<std::string, double> dom = DomParse(json, decayRate, decayPeriod);
//     assert(dom.size() == jsh.GetResult().size());
//     for (const auto& it: jsh.GetResult())
//     {
//         assert(dom.count(it.first) == 1);
//         assert(abs(dom[it.first] - it.second) < 0.0000001);
//         printf("%s:\t%lf\n", it.first.c_str(), it.second);
//     }
// 
//     return 0;
// }
