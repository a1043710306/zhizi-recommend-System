#ifndef EVENT_PARSER_H
#define EVENT_PARSER_H 
#include <string>
#include "util.h"

class EventParser
{
    public:
        struct Event
        {
            std::string uid;
            std::string infoId;
            std::string app;
            std::string configId;
            int32_t eventTime;
            int32_t logTime;
            int32_t sign;
        };
        static int32_t ParseClickMsg(Event& ev, const std::string& kafkaMsg);
        static int32_t ParseImpressionMsg(Event& ev, const std::string& kafkaMsg);
        static int32_t ParseDislikeMsg(Event& ev, const std::string& kafkaMsg);
        static int32_t ParseImcMsg(Event& ev, const std::string& kafkaMsg);
        static void SetTimeZone(int tz)
        {
            assert(tz <= 12 && tz >= -12);
            EventParser::timeZone_ = tz;
        }
    private:
        static int32_t timeZone_;
        static int32_t ParseTime(int32_t& ts, const std::string& timeStr);
};
#endif /* EVENT_PARSER_H */
