#include <cstdio>
#include "gtest/gtest.h"

#include "priority_kv_cache.h"

TEST(kvcache, priority)
{
    PriorityKVCache<std::string, int> kv(10);
    for (int i = 0; i < 10; ++i)
    {
        ASSERT_EQ(0, kv.Set(std::to_string(i), i, i));
    }
    ASSERT_EQ(0, kv.Set(std::to_string(10), 10, 10));
    int res;
    ASSERT_EQ(1, kv.Get(res, std::to_string(0)));

    for (int i = 1; i < 11; ++i)
    {
        // fprintf(stdout, "%d\n", i);
        int res;
        ASSERT_EQ(0, kv.Get(res, std::to_string(i)));
        EXPECT_EQ(i, res);
    }
}
