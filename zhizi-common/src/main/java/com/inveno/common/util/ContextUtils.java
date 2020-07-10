package com.inveno.common.util;

import com.inveno.common.bean.Context;

public class ContextUtils
{
	public static String toRequestInfoString(Context context) {
		StringBuffer sb = new StringBuffer();
		if (context != null)
		{
			sb.append(" request_id : ");
			sb.append(context.getRequestId());
			sb.append(" abtest : ");
			sb.append(context.getAbtestVersion());
			sb.append(" app : ");
			sb.append(context.getApp());
			if (context.getScenario() > 0)
			{
				sb.append(" scenario : ");
				sb.append(context.getScenario());
			}
		}
		return sb.toString();
	}
	public static final String[] array_position_desc = new String[]{
		"unknown" /*skipped index*/
		, "long_listpage"            /*0x01*/
		, "poll_list"                /*0x02*/
		, "subscription_list"        /*0x03*/
		, "subscription_source_list" /*0x04*/
		, "personal_centre"          /*0x05*/
		, "favorites_page"           /*0x06*/
		, "search_page"              /*0x07*/
		, "push"                     /*0x08*/
		, "special_issue"            /*0x09*/
		, "no_longer_use"            /*0x0a*/
		, "no_longer_use"            /*0x0b*/
		, "relevant_recommendation"  /*0x0c*/
		, "detail_textbottom"        /*0x0d*/
		, "self_media"               /*0x0e*/
		, "detailopen_facebookshare" /*0x0f*/
		, "video_tab"                /*0x10*/
		, "open_screen_ad"           /*0x11*/
		, "quickread"                /*0x12*/
		, "lockscreen"               /*0x13*/
		, "insert_screen"            /*0x14*/
		, "floating_window"          /*0x15*/
	};
	public static String getPositionDesc(Context context) {
		int position = getPosition(context);
		if (position < array_position_desc.length)
			return array_position_desc[position];
		else
			return array_position_desc[0];
	}
	public static int getPositionType(Context context) {
		return getPositionType(context.getScenario());
	}
	public static int getPosition(Context context) {
		return getPosition(context.getScenario());
	}
	public static int getChannelId(Context context) {
		return getChannelId(context.getScenario());
	}
	public static int getPositionType(long scenario) {
		return (int)((scenario & 0xff0000) >> 16);
	}
	public static int getPosition(long scenario) {
		return (int)((scenario & 0xff00) >> 8);
	}
	public static int getChannelId(long scenario) {
		return (int)(scenario & 0xff);
	}
	/**
	 * value : 0x000000 ~ 0xffffff
	 * 三个位元组依序表示 场景类型(position_type), 位置属性场景(position), 内容属性场景(channel)
	 */
	public static boolean isForYouChannel(Context context) {
		int channel = getChannelId(context);
		return (channel == 0x00);
	}
	public static boolean isVideoChannel(Context context) {
		int channel = getChannelId(context);
		return (channel == 0x0b);
	}
	public static boolean isVideoChannel(long scenario) {
        int channel = getChannelId(scenario);
        return (channel == 0x0b);
    }
	public static boolean isMemesChannel(Context context) {
		return isMemesChannel(context.getScenario());
	}
	public static boolean isShortVideoChannel(Context context) {
		return isShortVideoChannel(context.getScenario());
	}
	public static boolean isShoppingChannel(Context context) {
        return isShoppingChannel(context.getScenario());
    }
	public static boolean isSubscriptionChannel(Context context) {
        return isSubscriptionChannel(context.getScenario());
    }
	public static boolean isGifChannel(Context context) {
		int channel = getChannelId(context);
		return (channel == 0x26);
	}
	public static boolean isGifChannel(long scenario) {
        int channel = getChannelId(scenario);
        return (channel == 0x26);
    }
	public static boolean isComicChannel(Context context) {
		int channel = getChannelId(context);
		return (channel == 0x2f);
	}
	public static boolean isBeautyChannel(Context context) {
		return isBeautyChannel(context.getScenario());
	}
	public static boolean isLockscreen(Context context) {
		return isLockscreen(context.getScenario());
	}
	public static boolean isLockscreen(long scenario) {
		int position = getPosition(scenario);
		return (position == 0x13);
	}
	public static boolean isQuickread(Context context) {
		int position = getPosition(context);
		return (position == 0x12);
	}

	public static boolean isMemesChannel(long scenario) {
		int channel = getChannelId(scenario);
		return (channel == 0x25);
	}
	public static boolean isBeautyChannel(long scenario) {
		int channel = getChannelId(scenario);
		return (channel == 0x30);
	}

	public static boolean isShortVideoChannel(long scenario) {
		int channel = getChannelId(scenario);
		return (channel == 0x32);
	}
	
	public static boolean isShoppingChannel(long scenario) {
        int channel = getChannelId(scenario);
        return (channel == 0x33);
    }
	
	public static boolean isSubscriptionChannel(long scenario) {
        int channel = getChannelId(scenario);
        return (channel == 0x80);
    }

	public static String getInterfaceName(Context context) {
		String interfaceName = "q";
		if (context.getZhiziListReq() != null) {
			interfaceName = "list";
		} else if (context.getRecNewsListReq() != null ) {
			if (!context.getRecNewsListReq().isNeedBanner()) {
				interfaceName = "q";
			} else {
				interfaceName = "qb";
			}
		} else if (context.getScenario() > 0) {
			interfaceName = "qcn";
		}
		return interfaceName;
	}

	public static String getSemantic(Context context) {
		String semantic = "channel";
		if (context.getZhiziListReq() != null) {
			semantic = "scenario";
		}
		return semantic;
	}

	public static boolean isLongListPage(Context context) {
		return ((context.getScenario() & 0xffff00) == 0x010100);
	}
	
	public static boolean isLongListPage_NewsContained(Context context) {
		return ( (isLongListPage(context) ) && !(isVideoChannel(context) || isShortVideoChannel(context) || isGifChannel(context) || isMemesChannel(context) || isComicChannel(context) || isShoppingChannel(context) || isSubscriptionChannel(context) ));
	}
}
