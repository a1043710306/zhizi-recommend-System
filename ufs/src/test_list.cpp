// #include <cstdio>
// #include <cassert>
#include <list>
#include "gtest/gtest.h"
// #include <sys/time.h>

TEST(list, tail)
// int main()
{
    // std::list<int> l;
    // for (int i = 0; i < 1000000; ++i)
    // {
    //     struct timeval b,e,h;
    //     gettimeofday(&b, NULL);
    //     l.push_back(i);
    //     std::list<int>::iterator it = l.end();
    //     --it;
    //     gettimeofday(&e, NULL);
    //     assert(*it == i);
    //     // printf("%zu, %d\n", l.size(), (e.tv_sec - b.tv_sec) * 1000000 + (e.tv_usec - b.tv_usec));
    //     (l.size() == l.size());
    //     gettimeofday(&h, NULL);
    //     printf("%zu, %d, %d\n", l.size(), (e.tv_sec - b.tv_sec) * 1000000 + (e.tv_usec - b.tv_usec), (h.tv_sec - b.tv_sec) * 1000000 + (h.tv_usec - b.tv_usec));
    //     // printf("-----, %d\n", (h.tv_sec - b.tv_sec) * 1000000 + (h.tv_usec - b.tv_usec));
    // }
    // return 0;
    std::list<int> l = {1,2,3,4,5};
    l.push_back(6);
    std::list<int>::iterator it = l.end();
    --it;
    EXPECT_EQ(*it, 6);
    l.push_back(7);
    EXPECT_EQ(*it, 6);
    l.pop_front();
    EXPECT_EQ(*it, 6);
    l.push_back(8);
    l.push_back(9);
    EXPECT_EQ(*it, 6);
}

// prove list is efficient enough, but printf is very slow
