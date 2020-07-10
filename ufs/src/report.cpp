#include <cstdio>
#include "report.h"

using namespace inv::monitor;
int main(int argc, char** argv)
{
    char metric[256];
    uint64_t value;
    scanf("%s\t%lu", metric, &value);
    ReportAvg(metric, value);
    return 0;
}
