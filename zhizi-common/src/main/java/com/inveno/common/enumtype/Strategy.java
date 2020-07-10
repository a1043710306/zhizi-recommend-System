package com.inveno.common.enumtype;

import java.io.Serializable;

public enum Strategy implements Serializable
{
	PUSH("推送","1")
	, SHORT_INTEREST("短期兴趣","2")
	, FORCE_INSERT("强插","3")
	, FALLBACK("fallback","4")
	, FLOW_EXPLORATION("流量探索","5")
	, TOP("置顶","6")
	, FIRSTSCREEN_FORCE_INSERT("专题首屏", "7")
	, RECOMMENDATION("推荐","8")
	, MIXED_INSERT_OPERATION("运营资讯混插","9")
	, MIXED_INSERT_VIDEOBOOST("视频推荐","14")
	, MIXED_INSERT_PAID("付费资讯混插","15")	//sourceRank 0x01
	, DOWNGRADE_SCORE_061("降权,色情分0.6-0.8", "16")
	, DOWNGRADE_SCORE_081("降权,色情分0.8-0.9", "17")
	, MIXED_INSERT_MOOD("表情资讯混插","19")	//sourceRank 0x02
	, INTEREST_EXPLORING("兴趣探索", "29")
	, MIXED_INSERT_MEMESBOOST("memes推荐", "34")
	, INTEREST_BOOST("兴趣加强", "35")
	, MIXED_INSERT_GIFBOOST("gif推荐", "42")
	;

	 // 定义私有变量
    private String code;
    private String name;

    // 构造函数，枚举类型只能为私有
    private Strategy(String name,String code) {
    	this.name =  name;
        this.code = code;
    }

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
