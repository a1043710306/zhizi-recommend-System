#include "gtest/gtest.h"
#include "util.h"

TEST(uid, hash)
{
    EXPECT_EQ(2, GetIdxOfConns("860842021187434", 6, ""));
    EXPECT_EQ(5, GetIdxOfConns("99000561479260", 6, ""));
    EXPECT_EQ(1, GetIdxOfConns("01011601271758151201000050707908", 6, ""));
    EXPECT_EQ(4, GetIdxOfConns("+8613986143687", 6, ""));
    EXPECT_EQ(3, GetIdxOfConns("01011510251327321201000100715300", 6, ""));
    EXPECT_EQ(0, GetIdxOfConns("866288028654170", 6, ""));

    EXPECT_EQ(2, GetIdxOfConns("860842021187434", 6, "ufs.salt&*("));
    EXPECT_EQ(2, GetIdxOfConns("99000561479260", 6, "ufs.salt&*("));
    EXPECT_EQ(0, GetIdxOfConns("01011601271758151201000050707908", 6, "ufs.salt&*("));
    EXPECT_EQ(5, GetIdxOfConns("+8613986143687", 6, "ufs.salt&*("));
    EXPECT_EQ(1, GetIdxOfConns("01011510251327321201000100715300", 6, "ufs.salt&*("));
    EXPECT_EQ(1, GetIdxOfConns("866288028654170", 6, "ufs.salt&*("));
}
