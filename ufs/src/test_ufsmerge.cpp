#include "gtest/gtest.h"

#define private public
#include "ufs_logic.h"
#undef private

bool gRun = false;

TEST(merge, stats)
{
    auto pold = std::shared_ptr<std::map<std::string, std::pair<uint32_t, double>>>(new std::map<std::string, std::pair<uint32_t, double>>({{"a", {10, 20.1}}, {"b", {13, 3.1}}, {"c", {24, 44.3}}, {"d", {56, 2.5}}}));
    std::map<std::string, std::pair<uint32_t, double>> nm {
        {"f", {100, 200.1}},
        {"e", {103, 30.1}},
        {"c", {204, 44.03}},
        {"d", {506, 2.95}},
    };
    // std::map<std::string, int32_t> nn = {
    //     {"a", 1},
    //     {"b", 2},
    //     {"c", 3},
    // };

    SfuLogic::MergeStatsMaps(pold, nm);
    EXPECT_EQ(6, pold->size());
    EXPECT_EQ(100, (*pold)["f"].first);
    EXPECT_DOUBLE_EQ(200.1, (*pold)["f"].second);

    EXPECT_EQ(10, (*pold)["a"].first);
    EXPECT_DOUBLE_EQ(20.1, (*pold)["a"].second);

    EXPECT_EQ(204, (*pold)["c"].first);
    EXPECT_DOUBLE_EQ(44.03, (*pold)["c"].second);
}

TEST(merge, tag)
{
    auto pold = std::shared_ptr<std::map<std::string, double>>(new std::map<std::string, double>({{"a", 20.1}, {"b", 3.1}, {"c", 44.3}, {"d", 2.5}}));
    std::map<std::string, double> nm {
        {"f", 200.1},
        {"e", 30.1},
        {"c", 44.03},
        {"d", 2.95},
    };
    SfuLogic::MergeWeightMaps(pold, nm);
    EXPECT_EQ(6, pold->size());
    EXPECT_DOUBLE_EQ(200.1, (*pold)["f"]);

    EXPECT_DOUBLE_EQ(20.1, (*pold)["a"]);

    EXPECT_DOUBLE_EQ(2.95+2.5, (*pold)["d"]);
}
