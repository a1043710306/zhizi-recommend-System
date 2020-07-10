package com.inveno.common.enumtype;

import java.io.Serializable;

/**
 * @deprecated rename to Strategy
 */
public enum BehaviorType implements Serializable
{
	SHORTBEHAVIOR("短期行为","2"),
	HEADINFO("强插","3"),
	NEWINFO("最新资讯","4"),
	CTR("流量探索","5"),
	TOP("置顶","6"),
	FIRSTSCREEN_FORCE_INSERT("专题首屏", "7"),
	GMPNEWS("GMPKEYWORD推荐","8"),
	OPERATIONNEWS("运营资讯混插","9"),
	VIDEOBOOSTNEWS("VIDEO资讯混插","14"),
	MIXEDINSERT_PAIDNEWS("付费资讯混插","15"),	//sourceRank 0x01
	MIXEDINSERT_MOODNEWS("表情资讯混插","19");	//sourceRank 0x02

	 // 定义私有变量
    private String code;
    private String name;

    // 构造函数，枚举类型只能为私有
    private BehaviorType(String name,String code) {
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
