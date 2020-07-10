/*************************************************************************
	> File Name: zhizi_define.h
	> Author: xyz
	> Mail: xiao13149920@foxmail.com 
	> Created Time: Sun 10 Jun 2018 08:07:52 PM PDT
 ************************************************************************/
#ifndef __ZHIZI_DEFINE_H__
#define __ZHIZI_DEFINE_H__
#include<stdlib.h>

template<typename T>
inline void FREE(T** t) {
    if (*t != nullptr) {
        free(*t);
        *t = nullptr;
    }
}

template<typename T>
inline void DELETE(T** t) {
    if (*t != nullptr) {
        delete *t;
        *t = nullptr;
    }
}

template<typename T>
inline void DELETES(T** t) {
    if (*t != nullptr) {
        delete[] *t;
        *t = nullptr;
    }
}
/* content_type define begin */
#define ZHIZI_DEFINE_CONTENT_TYPE_TABLE \
    X(CONTENT_TYPE_NEWS, 0x00000001, "0x00000001", "新闻", "news") \
X(CONTENT_TYPE_SHORT_VIDEO, 0x00000002, "0x00000002", "短视频", "short_video") \
X(CONTENT_TYPE_LONG_VIDEO, 0x00000004, "0x00000004", "长视频", "long_video") \
X(CONTENT_TYPE_TOPIC, 0x00000008, "0x00000008", "话题", "") \
X(CONTENT_TYPE_SPECIAL_ISSUE, 0x00000010, "0x00000010", "专题", "") \
X(CONTENT_TYPE_GIF, 0x00000020, "0x00000020", "动态GIF图", "") \
X(CONTENT_TYPE_BANNER, 0x00000040, "0x00000040", "banner", "") \
X(CONTENT_TYPE_BIG_IMAGE, 0x00000080, "0x00000080", "美图（单张大图形式）", "") \
X(CONTENT_TYPE_IMAGE_SET, 0x00000100, "0x00000100", "图集", "") \
X(CONTENT_TYPE_ADVERTISEMENT_HARD, 0x00000200, "0x00000200", "硬文广告", "") \
X(CONTENT_TYPE_ADVERTISEMENT_SOFT, 0x00000400, "0x00000400", "软文广告", "") \
X(CONTENT_TYPE_ADVERTISEMENT_THIRD_PARTY, 0x00000800, "0x00000800", "第三方广告", "") \
X(CONTENT_TYPE_AUDIO, 0x00001000, "0x00001000", "音频", "") \
X(CONTENT_TYPE_COMMENT_BACK, 0x00002000, "0x00002000", "评论回复", "") \
X(CONTENT_TYPE_COMIC, 0x00004000, "0x00004000", "漫画", "") \
X(CONTENT_TYPE_LIVE_BROADCAST, 0x00008000, "0x00008000", "直播", "")

#define X(a, b, c, d, e) a,
enum ZHIZI_DEFINE_CONTENT_TYPE {
    ZHIZI_DEFINE_CONTENT_TYPE_TABLE
};
#undef X

extern const char* const ContentTypeTableStr[][3];
extern const int ContentTypeTableInt[];
/* content_type define end */

/* link_type begin */
#define ZHIZI_DEFINE_LINK_TYPE_TABLE \
    X(LINK_TYPE_WEBVIEW, 0x00000001, "0x00000001", "Web杂交页(上面是webview，用客户端内置浏览器打开url，下面是原生的相关推荐、评论等)") \
X(LINK_TYPE_NATIVE, 0x00000002, "0x00000002", "native(服务器下发json正文数据，客户端解析后显示，抓取清洗后有正文，标题)") \
X(LINK_TYPE_DOUBLE_CLICK, 0x00000004, "0x00000004", "流量双记（有外链url，且抓取清洗后有正文，标题）") \
X(LINK_TYPE_BROWSER, 0x00000008, "0x00000008", "从客户端跳转至外部浏览器打开（有外链url）") \
X(LINK_TYPE_PURE_WEBVIEW, 0x00000010, "0x00000010", "打开专题的h5网页（webview下方无原生的评论等元素）") \
X(LINK_TYPE_NO_ACTION, 0x00000020, "0x00000020", "无动作") \
X(LINK_TYPE_PLAY_IN_LIST_VIDEO, 0x00000040, "0x00000040", "客户端解析视频url（有视频播放url）") \
X(LINK_TYPE_PLAY_IN_LIST_AUDIO, 0x00000080, "0x00000080", "客户端解析音频url（有音频播放url）") \
X(LINK_TYPE_UNDER_DETAIL_READ_SOURCE, 0x00000100, "0x00000100", "native形式打开，正文底部显示查看原文链接") \
X(LINK_TYPE_APP_DOWNLOAD, 0x00000200, "0x00000200", "应用下载/安装/激活") \
X(LINK_TYPE_CALL, 0x00000400, "0x00000400", "打电话") \
X(LINK_TYPE_SMS, 0x00000800, "0x00000800", "发短信") \
X(LINK_TYPE_SMART_VIEW, 0x00001000, "0x00001000", "SmartView（为规避版权风险的一种浏览方式，优先打开web页面，可快速切换到原生页）") \
X(LINK_TYPE_H5_NATIVE, 0x00002000, "0x00002000", "H5 native,以H5形式打开原生页,服务器下发html数据，然后客户端用webview解析这些数据。") \
X(LINK_TYPE_GALLERY, 0x00004000, "0x00004000", "图集浏览形式打开，单张大图下带正文内容，左滑右滑可切换浏览图片") \
X(LINK_TYPE_PLAY_IN_LIST_MP4, 0x000008000, "0x00008000", "客户端解析视频url（有视频播放url）mpeg4")

#define X(a, b, c, d) a,
enum ZHIZI_DEFINE_LINK_TYPE {
    ZHIZI_DEFINE_LINK_TYPE_TABLE
};
#undef X

extern const char* const LinkTypeTableStr[][2];
extern const int LinkTypeTableInt[];
/* link_type end */

/* display_type begin */
#define ZHIZI_DEFINE_DISPLAY_TYPE_TABLE \
    X(DISPLAY_TYPE_NO_IMAGE, 0x00000001, "0x00000001", "无图，纯文字（必须有正文，标题）") \
X(DISPLAY_TYPE_SINGLE_THUMBNAIL, 0x00000002, "0x00000002", "单张缩略图（列表页展现1张缩略图，图片宽度大于200px，  1/2 < 宽/高 < 4）") \
X(DISPLAY_TYPE_TRIPLE_IMAGE, 0x00000004, "0x00000004", "三图（列表页展现3张缩略图，图片宽度大于200px，  1/2 < 宽/高 < 4）") \
X(DISPLAY_TYPE_BANNER, 0x00000008, "0x00000008", "通栏（需规定分辨率，图片宽度大于600px，5/3<=宽/高<=14/5，爬虫入库时将此图写入t_content表fall_image）") \
X(DISPLAY_TYPE_MULTIPLE_IMAGE, 0x00000010, "0x00000010", "多图文章（列表页缩略图数量大于等于3张，图片宽度大于200px，  1/2 < 宽/高 < 4）") \
X(DISPLAY_TYPE_SINGLE_FALL_IMAGE, 0x00000020, "0x00000020", "瀑布流（列表页展现瀑布流形式单图，图片宽度大于240px,1/2<宽/高<3，爬虫入库时将此图写入t_content表fall_image）") \
X(DISPLAY_TYPE_GT_SIX_IMAGE_COLLECT, 0x00000040, "0x00000040", "图集（详情页满足分辨率的图片数量大于等于6张，图片宽度大于200px，  1/2 < 宽/高 < 4）") \
X(DISPLAY_TYPE_BANNER_AUTO , 0x00000080, "0x00000080", "banner(轮播),banner位置，多个资讯聚合在一起，能自动循环播放") \
X(DISPLAY_TYPE_SINGLE_VOTE_IMAGE , 0x00000100, "0x00000100", "文字投票卡片") \
X(DISPLAY_TYPE_DOUBLE_VOTE_IMAGE, 0x00000200, "0x00000200", "两图投票卡片") \
X(DISPLAY_TYPE_SINGLE_BIG_IMAGE, 0x00000400, "0x00000400", "单张大图（需规定分辨率，图片宽度大于400px， 1/6 < 宽/ 高 < 4，爬虫入库时将此图写入t_content表fall_image）") \
X(DISPLAY_TYPE_SINGLE_BIG_GIF, 0x00000800, "0x00000800", "单张GIF（gif格式图片数量大于等于1）") \
X(DISPLAY_TYPE_SINGLE_BIG_VIDEO, 0x00001000, "0x00001000", "单条大图视频") \
X(DISPLAY_TYPE_OPEN_SCREEN_DISPLAY, 0x00002000, "0x00002000", "开屏展示方式") \
X(DISPLAY_TYPE_IMAGE_MULTIPLE_FUNCTION, 0x00004000, "0x00004000", "图文多功能按钮。") \
X(DISPLAY_TYPE_INSERT_POP_DISPLAY, 0x00008000, "0x00008000", "插屏弹窗展示方式。") \
X(DISPLAY_TYPE_SINGLE_VERTICAL_IMAGE , 0x00010000, "0x00010000", "单竖图") \
X(DISPLAY_TYPE_LOCKSCREEN_NEWS, 0x00020000, "0x00020000", "lockscreen_news（需规定分辨率，图片宽度大于400px，3/5 <宽／高< 8/5）") \
X(DISPLAY_TYPE_LOCKSCREEN_MEMES, 0x00040000, "0x00040000", "lockscreen_memes（需规定分辨率，图片宽度大于400px，1/2 <宽／高< 5/3）") \
X(DISPLAY_TYPE_GIF_CONVERT_MP4, 0x00080000, "0x00080000", "Mpeg4") \
X(DISPLAY_TYPE_SINGLE_THUMBNAIL_VIDEO, 0x00100000, "0x00100000", "单条小图视频")

#define X(a, b, c, d) a,
enum ZHIZI_DEFINE_DISPLAY_TYPE {
    ZHIZI_DEFINE_DISPLAY_TYPE_TABLE
};
#undef X

extern const char* const DisplayTypeTableStr[][2];
extern const int DisplayTypeTableInt[];
/* display_type end */

/* strategy begin */
#define ZHIZI_DEFINE_STRATEGY_TABLE \
    X(STRATEGY_PUSH, 1, "1",   "推送") \
X(STRATEGY_SHORT_INTEREST, 2, "2",   "短期兴趣") \
X(STRATEGY_FORCE_INSERT, 3, "3",   "强插") \
X(STRATEGY_FALLBACK, 4, "4",   "fallback") \
X(STRATEGY_FLOW_EXPLORATION, 5, "5",   "流量探索") \
X(STRATEGY_TOP, 6, "6",   "置顶") \
X(STRATEGY_FIRSTSCREEN_FORCE_INSERT, 7, "7",   "锁屏专题") \
X(STRATEGY_RECOMMENDATION, 8, "8",   "推荐") \
X(STRATEGY_MIX_INSERT, 9, "9",   "人工运营，混插") \
X(STRATEGY_RELATIVE_RECOMMENDATION, 10, "10", "相关推荐") \
X(STRATEGY_AD_RECOMMAND, 11, "11", "广告推荐") \
X(STRATEGY_SEARCH, 12, "12", "搜索") \
X(STRATEGY_SELF_MEDIA, 13, "13", "按源下发一月内的时间倒序排列的资讯") \
X(STRATEGY_MIXED_INSERT_VIDEOBOOST, 14, "14", "视频推荐") \
X(STRATEGY_MIXED_INSERT_PAID, 15, "15", "付费混插资讯池") \
X(STRATEGY_DOWNGRADE_SCORE_61, 16, "16", "降权，色情分0.6-0.8") \
X(STRATEGY_DOWNGRADE_SCORE_81, 17, "17", "降权，色情分0.8-0.9") \
X(STRATEGY_SPECIAL_ISSUE, 18, "18", "专题") \
X(STRATEGY_MIXED_INSERT_MOOD, 19, "19", "表情混插") \
X(STRATEGY_SUBSCRIPTION_SOURCE, 20, "20", "订阅源") \
X(STRATEGY_DIRECTED_EXPLORATION, 21, "21", "定向探索") \
X(STRATEGY_DIRECTED_RECOMMENDATION, 22, "22", "定向下发") \
X(STRATEGY_IMPORTANT_NEWS, 23, "23", "要闻（每个用户下发在第一屏）") \
X(STRATEGY_PUSH_EXPLORE, 24, "24", "探索推送") \
X(STRATEGY_PUSH_LABEL, 25, "25", "个性化推送匹配套路标签") \
X(STRATEGY_PUSH_LABEL_OTHER, 26, "26", "个性化推送未高度匹配套路标签") \
X(STRATEGY_PUSH_IMMEDIATELY, 27, "27", "立即推送") \
X(STRATEGY_INSTANTLY_INTERESTED, 28, "28", "即刻兴趣") \
X(STRATEGY_INTEREST_EXPLORING, 29, "29", "兴趣探索") \
X(STRATEGY_PUSH_OFFLINE, 30, "30", "离线推送") \
X(STRATEGY_CATEGORY_WEIGHT, 31, "31", "分类加权") \
X(STRATEGY_PAID_SOURCE, 32, "32", "付费内容源") \
X(STRATEGY_TIMELY_RECOMMENDATION, 33, "33", "时效性资讯推荐") \
X(STRATEGY_MIXED_INSERT_MEMESBOOST, 34, "34", "memes推荐") \
X(STRATEGY_INTEREST_BOOST, 35, "35", "兴趣加强") \
X(STRATEGY_HONEYBEEFALLBACK, 36, "36", "honeybeefallback") \
X(STRATEGY_NON_TIMELY_HIGH_QUALITY_NEWS, 37, "37", "非时效性精品池") \
X(STRATEGY_HOT_RECOMMENDATION, 38, "38", "热点资讯推荐") \
X(STRATEGY_STEPWISE_DELIVERY, 39, "39", "阶梯性投放") \
X(STRATEGY_COLD_START, 40, "40", "冷启动") \
X(STRATEGY_INSTANTLY_INTERESTED_TOPIC, 41, "41", "即刻兴趣-主题相似") \
X(STRATEGY_MIXED_INSERT_GIFBOOST, 42, "42", "gif推荐") \
X(STRATEGY_ACTIVITY, 43, "43", "活动") \
X(STRATEGY_MIXED_INSERT_HOT_NEWS, 101, "101", "热点新闻") \
X(STRATEGY_MIXED_INSERT_GOSSIP_FASHION, 102, "102", "八卦/时尚") \
X(STRATEGY_MIXED_INSERT_QULIFIED_LOWB, 103, "103", "优质三俗/标题党") \
X(STRATEGY_MIXED_INSERT_VERTICALNEWS_POOL1, 104, "104", "垂直类1") \
X(STRATEGY_MIXED_INSERT_VERTICALNEWS_POOL2, 105, "105", "垂直类2") \
X(STRATEGY_MIXED_INSERT_ZUIMEITIANQI, 106, "106", "最美天气") \
X(STRATEGY_MIXED_INSERT_YOUTH_STORIES, 107, "107", "小鲜肉") \
X(STRATEGY_MIXED_INSERT_DEEP_GOSSIP, 108, "108", "狗血八卦") \
X(STRATEGY_MIXED_INSERT_ANECDOTES, 109, "109", "奇闻灵异") \
X(STRATEGY_MIXED_INSERT_UNOFFICIAL_HISTORY, 110, "110", "野史") \
X(STRATEGY_MIXED_INSERT_ECCENTRIC_SOCIALNEWS, 111, "111", "社会狗血") \
X(STRATEGY_MIXED_INSERT_ALI_SPECIAL, 112, "112", "阿里") \
X(STRATEGY_MIXED_INSERT_DONGFANGWANG, 113, "113", "东方网") \
X(STRATEGY_MIXED_INSERT_JIANKANGYANGSHENG, 114, "114", "健康养生") \
X(STRATEGY_MIXED_INSERT_TIEXUEJUNSHI, 115, "115", "铁血军事") \
X(STRATEGY_MIXED_INSERT_JUNSHITOUTIAO, 116, "116", "军事头条") \
X(STRATEGY_MIXED_INSERT_INTERNET, 117, "117", "互联网") \
X(STRATEGY_MIXED_INSERT_ELECTRONIC_BUSINESS, 118, "118", "电商") \
X(STRATEGY_MIXED_INSERT_VIDEO, 119, "119", "人工精选视频") \
X(STRATEGY_MIXED_INSERT_FASHION_ENTERTAINMENT, 120, "120", "时尚/娱乐") \
X(STRATEGY_MIXED_INSERT_CATEGORY_HEADER, 121, "121", "分类头部混插") \
X(STRATEGY_MIXED_INSERT_QUALIFIED_NEWS, 201, "201", "优质垂直分类") \
X(STRATEGY_MIXED_INSERT_HOTNEWS_OVERSEA, 202, "202", "热门新闻") \
X(STRATEGY_MIXED_INSERT_FB_RATE_100W, 203, "203", "FBrate100w") \
X(STRATEGY_MIXED_INSERT_FB_RATE_50W, 204, "204", "FBrate50w") \
X(STRATEGY_MIXED_INSERT_FB_RATE_10W, 205, "205", "FBrate10w") \
X(STRATEGY_MIXED_INSERT_FB_RATE_1W, 206, "206", "FBrate1w") \
X(STRATEGY_MIXED_INSERT_COPYRIGHTEDBOOST, 207, "207", "媒体合作保量") \
X(STRATEGY_MIXED_INSERT_TWITTER_HOT_NEWS, 208, "208", "twitter_hot_news") \
X(STRATEGY_MIXED_INSERT_FB_HOT_NEWS, 209, "209", "fb_hot_news") \
X(STRATEGY_MIXED_INSERT_IMPORTANT_NEWS, 210, "210", "机器选择要闻") \
X(STRATEGY_MIXED_INSERT_HIGH_CONTENT_QUALITY, 211, "211", "高质量资讯混插策略") \
X(STRATEGY_MIXED_INSERT_TOP_SCREEN, 212, "212", "foru频道的第一屏") \
X(STRATEGY_MIXED_INSERT_TOP_ONE_POSITION, 213, "213", "foru频道头版头条") \
X(STRATEGY_MIXED_INSERT_YOUTUBE_RANKING, 214, "214", "站外评级A-及以上的视频") \
X(STRATEGY_MIX_INSERT_YOUTUBE_100W50W, 215, "215", "Youtube首页抓的视频") \
X(STRATEGY_MIX_INSERT_HIGH_INTERACTION, 216, "216", "高互动文章") \
X(STRATEGY_MIX_INSERT_ENTERTAINMENT, 217, "217", "娱乐新闻") \
X(STRATEGY_MIX_INSERT_VIRAL, 218, "218", "viral范娱乐新闻") \
X(STRATEGY_MIX_INSERT_BEAUTIES_HIGH_QUALITY, 219, "219", "beauties精品池混插") \
X(STRATEGY_MIX_INSERT_MEMES_HIGH_QUALITY, 220, "220", "memes精品池混插") \
X(STRATEGY_MIX_INSERT_GIF_HIGH_QUALITY, 221, "221", "gif精品池混插") \
X(STRATEGY_MIX_INSERT_VIDEO_HIGH_QUALITY, 222, "222", "video精品池混插")

#define X(a, b, c, d) a,
enum ZHIZI_DEFINE_STRATEGY {
    ZHIZI_DEFINE_STRATEGY_TABLE
};
#undef X

extern const char* const StrategyTableStr[][2];
extern const int StrategyTableInt[];
/* strategy end */
#endif
