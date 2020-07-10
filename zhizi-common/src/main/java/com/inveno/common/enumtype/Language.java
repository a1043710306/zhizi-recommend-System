package com.inveno.common.enumtype;

import java.io.Serializable;

public enum Language implements Serializable
{
	UNKNOWN("Unknown", 0x0000)
	, ZH_CN("zh_CN", 0x0001)
	, HINDI("Hindi", 0x0002)
	, ENGLISH("English", 0x0004)
	, INDONESIAN("Indonesian", 0x0008)
	, SPANISH("Spanish", 0x0010)
	;

	// 定义私有变量
	private String name;
	private int value;

	// 构造函数，枚举类型只能为私有
	private Language(String name, int value) {
		this.name = name;
		this.value = value;
	}

	public int getCode() {
		return value;
	}

	public void getCode(int value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static int getCode(String language) {
		java.util.List<Language> alLanguage = new java.util.ArrayList<Language>(java.util.Arrays.asList(Language.values()));
		for (int j = 0; j < alLanguage.size(); j++) {
			Language lang = alLanguage.get(j);
			if (language.equals(lang.getName())) {
				return lang.getCode();
			}
		}
		return UNKNOWN.getCode();
	}
	public static String[] listNames(int value) {
		java.util.ArrayList<String> alName = new java.util.ArrayList<String>();
		java.util.List<Language> alLanguage = new java.util.ArrayList<Language>(java.util.Arrays.asList(Language.values()));
		for (int j = 0; j < alLanguage.size(); j++) {
			Language lang = alLanguage.get(j);
			if ((lang.getCode() & value) > 0) {
				alName.add(lang.getName());
			}
		}
		return (String[])alName.toArray(new String[0]);
	}
}
