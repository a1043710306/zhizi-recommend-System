#include "gtest/gtest.h"

#include "linked_kv_cache.h"

TEST(kvcache, linked)
{
    LinkedKVCache<std::string, int> kv(10);

    for (int i = 0; i < 5; ++i)
    {
        ASSERT_EQ(0, kv.Set(std::to_string(i), i));//0,1,2,3,4
    }

    for (int i = 0; i < 5; ++i)
    {
        int r;
        ASSERT_EQ(0, kv.Get(r, std::to_string(i)));
        EXPECT_EQ(i, r);
    }
    for (int i = 6; i < 10; ++i)
    {
        int r;
        ASSERT_NE(0, kv.Get(r, std::to_string(i)));
    }

    for (int i = 0; i < 10; ++i)
    {
        kv.Set(std::to_string(i+5), i+5);
    }//5,6...14

    for (int i = 0; i < 5; ++i)
    {
        int r;
        ASSERT_NE(0, kv.Get(r, std::to_string(i)));
    }

    int r;
    ASSERT_EQ(0, kv.Get(r, std::to_string(5)));//touch 5
    EXPECT_EQ(5, r);
    kv.Set(std::to_string(20), 20);//6 will be evicted
    EXPECT_NE(0, kv.Get(r, std::to_string(6)));
    EXPECT_EQ(0, kv.Get(r, std::to_string(5)));
    EXPECT_EQ(10, kv.Size());
}
