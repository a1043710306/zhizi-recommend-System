#ifndef UFSSERVICEHANDLER_H
#define UFSSERVICEHANDLER_H 

#include <string>
#include <vector>
#include <map>
#include <boost/shared_ptr.hpp>

#include "UfsService.h"
#include "UfsServiceLogic.h"
#include "UfsService_types.h"

class UfsServiceHandler : public UfsServiceIf
{
    public:
        UfsServiceHandler() {}
        virtual ~UfsServiceHandler() {}
        void init(const std::string& confpath);

        void GetWeightedCategories(std::string& _return, const std::string& uid, const std::string& version);
        void GetWeightedTags(std::string& _return, const std::string& uid, const std::string& version);
        void GetLastActionTimeOfCategory(std::string& _return, const std::string& uid, const ActionType::type type);

        void GetImpressionTitleTags(std::string& _return, const std::string& uid, const std::string& version);
        void GetDislikeTitleTags(std::string& _return, const std::string& uid, const std::string& version);

        void GetLdaTopic(std::string& _return, const std::string& uid, const std::string& version);

        void GetTimeFeature(std::string& _return, const std::string& uid);
        void GetNetworkFeature(std::string& _return, const std::string& uid);
        void GetTagStats(std::string& _return, const std::string& uid);
        void GetCategoryStats(std::string& _return, const std::string& uid);
        void GetCreateTime(std::string& _return, const std::string& uid);
        void GetInterest(std::string& _return, const std::string& uid);
        int32_t SetInterest(const std::string& uid, const std::string& jsonArray);

        int32_t GetSourceCount(const std::string& uid, const std::string& source, const std::string& app);

        void GetUserInfo(UserInfo& _return, const std::string& uid, const std::string& catVersion,
                const std::string& tagsVersion, const std::string& titleTagsVersion,
                const std::string& ldaTopicVersion);
        int32_t SetUserGmp(const std::map<std::string, std::string> & kvs);
    private:
        boost::shared_ptr<UfsServiceLogic>& logicOfTheThread();
        typedef std::map<pthread_t, boost::shared_ptr<UfsServiceLogic>> LogicMap;
        LogicMap _logics;
        std::string _confpath;
};
#endif /* UFSSERVICEHANDLER_H */
