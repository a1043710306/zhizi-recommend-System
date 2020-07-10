/*************************************************************************
	> File Name: ufs_json_define.cpp
	> Author: xyz
	> Mail: xiao13149920@foxmail.com 
	> Created Time: Tuesday, July 24, 2018 PM04:03:35 HKT
 ************************************************************************/
#include"ufs_json_define.h"
#include"rapidjson/pointer.h"

//#define X(a, b, c, d) [a]={b, c, d},
#define X(a, b, c, d) [a]={rapidjson::Pointer(b), rapidjson::Pointer(c), rapidjson::Pointer(d)},
const rapidjson::Pointer VideoJsonFieldTableStr[][3] = {
	VIDEO_JSON_FIELD_TABLE
};
#undef X

