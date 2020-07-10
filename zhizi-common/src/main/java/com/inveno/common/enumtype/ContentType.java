package com.inveno.common.enumtype;

import java.io.Serializable;

public enum ContentType implements Serializable
{
	DEFAULT("", 0)
	, NEWS("一般资讯", 0x0001)
	, VIDEO("视频资讯", 0x0002)
	, SPECIAL_ISSUE("专题资讯", 0x0010)
	, GIF("动态GIF图", 0x0020)
	, MEME("meme资讯", 0x0080)
	, BEAUTY("美女资讯", 0x0080)
	, COMIC("漫画资讯", 0x4000)
	;

	 // 定义私有变量
    private String name;
    private int value;

    // 构造函数，枚举类型只能为私有
    private ContentType(String name, int value) {
    	this.name = name;
        this.value = value;
    }

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
