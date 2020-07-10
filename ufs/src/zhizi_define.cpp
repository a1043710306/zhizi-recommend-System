/*************************************************************************
	> File Name: zhizi_define.cpp
	> Author: xyz
	> Mail: xiao13149920@foxmail.com 
	> Created Time: Sun 10 Jun 2018 08:24:58 PM PDT
 ************************************************************************/
#include"zhizi_define.h"

/* content_type define begin */
#define X(a, b, c, d, e) [a]={c,d, e},
const char* const ContentTypeTableStr[][3] = {
    ZHIZI_DEFINE_CONTENT_TYPE_TABLE
};
#undef X

#define X(a, b, c, d, e) [a]=b,
const int ContentTypeTableInt[] = {
    ZHIZI_DEFINE_CONTENT_TYPE_TABLE
};
#undef X
/* content_type define end */

/* link_type define define */
#define X(a, b, c, d) [a]={c,d},
const char* const LinkTypeTableStr[][2] = {
    ZHIZI_DEFINE_LINK_TYPE_TABLE
};
#undef X

#define X(a, b, c, d) [a]=b,
const int LinkTypeTableInt[] = {
    ZHIZI_DEFINE_LINK_TYPE_TABLE
};
#undef X
/* link_type define end */

/* display_type begin */
#define X(a, b, c, d) [a]={c,d},
const char* const DisplayTypeTableStr[][2] = {
    ZHIZI_DEFINE_DISPLAY_TYPE_TABLE
};
#undef X

#define X(a, b, c, d) [a]=b,
const int DisplayTypeTableInt[] = {
    ZHIZI_DEFINE_DISPLAY_TYPE_TABLE
};
#undef X
/* display_type end */

/* strategy begin */
#define X(a, b, c, d) [a]={c,d},
const char* const StrategyTableStr[][2] = {
    ZHIZI_DEFINE_STRATEGY_TABLE
};
#undef X

#define X(a, b, c, d) [a]=b,
const int StrategyTableInt[] = {
    ZHIZI_DEFINE_STRATEGY_TABLE
};
#undef X

/* strategy end */
