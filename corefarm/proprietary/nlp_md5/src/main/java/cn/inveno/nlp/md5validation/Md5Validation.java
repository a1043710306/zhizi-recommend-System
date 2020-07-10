package cn.inveno.nlp.md5validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.nlpcn.commons.lang.jianfan.JianFan;

import cn.inveno.util.CleanText;
import cn.inveno.util.SentencesUtil;

public interface Md5Validation
{
    public String execute(String title, String content);
}
