#include <cassert>
#include "util/inv_application.h"
#include "UfsServiceHandler.h"

#ifndef  __COMMON_EXCEPTION_CATCH__
#define  __COMMON_EXCEPTION_CATCH__(ERRMSG)   \
    catch ( std::exception &e ) \
{\
    FDLOG("error") << "ERROR: catch exception:" << ERRMSG << ":" << e.what() << endl;\
}\
catch (const std::string& exstr)\
{\
    FDLOG("error") << "ERROR: catch ufs connect exception:" << exstr << ERRMSG << endl;\
}\
catch ( ... )\
{\
    FDLOG("error") << "ERROR: catch unknow exception:" << ERRMSG << endl;\
}
#endif

using namespace std;

std::vector<boost::shared_ptr<Thread>> INVThreadFactory::thrift_threads;
void UfsServiceHandler::init(const string& confpath)
{
    this->_confpath = confpath;
    // 每个线程分配一个logic对象
    for (size_t i = 0; i < INVThreadFactory::thrift_threads.size(); i++)
    {
        pthread_t tid = INVThreadFactory::thrift_threads[i]->getId();
        fprintf(stderr, "start thread: %ld\n", tid);
        _logics[tid].reset(new UfsServiceLogic);
        _logics[tid]->init(confpath);
    }
    return;
}

boost::shared_ptr<UfsServiceLogic>& UfsServiceHandler::logicOfTheThread()
{
    pthread_t self = pthread_self();
    if (_logics.count(self) <= 0)
    {
        assert(0);
    }
    return _logics[self];
}

int32_t UfsServiceHandler::GetSourceCount(const std::string& uid, const std::string& source, const std::string& app)
{
    assert(0);
    return -1;
}

void UfsServiceHandler::GetWeightedCategories(std::string& _return, const std::string& uid, const std::string& version)
{
    try
    {
        return logicOfTheThread()->GetWeightedCategories(_return, uid, version);
    }
    __COMMON_EXCEPTION_CATCH__(__FUNCTION__);
    
    _return = "";
    return;
}

void UfsServiceHandler::GetWeightedTags(std::string& _return, const std::string& uid, const std::string& version)
{
    try
    {
        return logicOfTheThread()->GetWeightedTags(_return, uid, version);
    }
    __COMMON_EXCEPTION_CATCH__(__FUNCTION__);
    
    _return = "";
    return;
}

void UfsServiceHandler::GetImpressionTitleTags(std::string& _return, const std::string& uid, const std::string& version)
{
    try
    {
        return logicOfTheThread()->GetImpressionTitleTags(_return, uid, version);
    }
    __COMMON_EXCEPTION_CATCH__(__FUNCTION__);
    
    _return = "";
    return;
}

void UfsServiceHandler::GetDislikeTitleTags(std::string& _return, const std::string& uid, const std::string& version)
{
    try
    {
        return logicOfTheThread()->GetDislikeTitleTags(_return, uid, version);
    }
    __COMMON_EXCEPTION_CATCH__(__FUNCTION__);
    
    _return = "";
    return;
}

void UfsServiceHandler::GetLdaTopic(std::string& _return, const std::string& uid, const std::string& version)
{
    try
    {
        return logicOfTheThread()->GetLdaTopic(_return, uid, version);
    }
    __COMMON_EXCEPTION_CATCH__(__FUNCTION__);
    
    _return = "";
    return;
}

void UfsServiceHandler::GetLastActionTimeOfCategory(std::string& _return, const std::string& uid, const ActionType::type type)
{
    try
    {
        return logicOfTheThread()->GetLastActionTimeOfCategory(_return, uid, type);
    }
    __COMMON_EXCEPTION_CATCH__(__FUNCTION__);
    
    _return = "";
    return;
}

void UfsServiceHandler::GetTimeFeature(std::string& _return, const std::string& uid)
{
    try
    {
        return logicOfTheThread()->GetTimeFeature(_return, uid);
    }
    __COMMON_EXCEPTION_CATCH__(__FUNCTION__);
    
    _return = "";
    return;
}

void UfsServiceHandler::GetNetworkFeature(std::string& _return, const std::string& uid)
{
    try
    {
        return logicOfTheThread()->GetNetworkFeature(_return, uid);
    }
    __COMMON_EXCEPTION_CATCH__(__FUNCTION__);
    
    _return = "";
    return;
}

void UfsServiceHandler::GetTagStats(std::string& _return, const std::string& uid)
{
    try
    {
        return logicOfTheThread()->GetTagStats(_return, uid);
    }
    __COMMON_EXCEPTION_CATCH__(__FUNCTION__);
    
    _return = "";
    return;
}

void UfsServiceHandler::GetCategoryStats(std::string& _return, const std::string& uid)
{
    try
    {
        return logicOfTheThread()->GetCategoryStats(_return, uid);
    }
    __COMMON_EXCEPTION_CATCH__(__FUNCTION__);
    
    _return = "";
    return;
}

void UfsServiceHandler::GetCreateTime(std::string& _return, const std::string& uid)
{
    try
    {
        return logicOfTheThread()->GetCreateTime(_return, uid);
    }
    __COMMON_EXCEPTION_CATCH__(__FUNCTION__);
    
    _return = "";
    return;
}

void UfsServiceHandler::GetInterest(std::string& _return, const std::string& uid)
{
    try
    {
        return logicOfTheThread()->GetInterest(_return, uid);
    }
    __COMMON_EXCEPTION_CATCH__(__FUNCTION__);
    
    _return = "";
    return;
}

int32_t UfsServiceHandler::SetInterest(const std::string& uid, const std::string& jsonArray)
{
    try
    {
        return logicOfTheThread()->SetInterest(uid, jsonArray);
    }
    __COMMON_EXCEPTION_CATCH__(__FUNCTION__);
    
    return -1;
}

void UfsServiceHandler::GetUserInfo(UserInfo& _return, const std::string& uid, const std::string& catVersion,
        const std::string& tagsVersion, const std::string& titleTagsVersion,
        const std::string& ldaTopicVersion)
{
    try
    {
        return logicOfTheThread()->GetUserInfo(_return, uid, catVersion, tagsVersion, titleTagsVersion, ldaTopicVersion);
    }
    __COMMON_EXCEPTION_CATCH__(__FUNCTION__);
    
    return;
}

int32_t UfsServiceHandler::SetUserGmp(const std::map<std::string, std::string>& kvs)
{
    try
    {
        return logicOfTheThread()->SetUserGmp(kvs);
    }
    __COMMON_EXCEPTION_CATCH__(__FUNCTION__);
    
    return -1;
}

#undef  __COMMON_EXCEPTION_CATCH__
