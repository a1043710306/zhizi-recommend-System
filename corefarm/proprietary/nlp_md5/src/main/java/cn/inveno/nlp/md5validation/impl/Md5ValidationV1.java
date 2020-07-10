package cn.inveno.nlp.md5validation.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.nlpcn.commons.lang.jianfan.JianFan;

import cn.inveno.nlp.md5validation.Md5Validation;
import cn.inveno.util.CleanText;
import cn.inveno.util.SentencesUtil;

public class Md5ValidationV1 implements Md5Validation
{
    static String specChar = "[-。、…‘’“”·∶﹕—﹗〖〗【】〔〕〈〉『』「」《》〃‖々―∕※︻︼︽︾︵︶︹︺︿﹀﹁﹂﹃﹄︻︼︷︸ ︴♂ ♀ˉˇ¨`~!@#$%^&*()+=|{}\"':;,\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]+";
    static Pattern pattern = Pattern.compile(specChar);
    public String execute(String title, String content)
    {
        // 标题与正文合并
        String strContent = title + "。" + content;

        // 过滤html标签
        strContent = CleanText.removeHtmlTag(strContent);

        // 转全半角
        strContent = CleanText.toDBC(strContent);

        // 多个空格转一个空格
        strContent=strContent.replaceAll("\\s+", " ");

        // 繁体字转简体字
        strContent=JianFan.f2J(strContent);

        List<String> sentenceList = SentencesUtil.toSentenceList(strContent);
        Map<String, Integer> sMap = new HashMap<String, Integer>();
        for (String s : sentenceList) {
            sMap.put(s, s.length());
        }

        List<Map.Entry<String, Integer>> sLst = new ArrayList<Map.Entry<String, Integer>>(sMap.entrySet());
        Collections.sort(sLst, new Comparator<Map.Entry<String, Integer>>() {
            //降序排序
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue() - o1.getValue());
            }
        });

        String strMerge = "";
        for (int j = 0; j < sLst.size() && j<3; j++) {
            // 统计最大的三个句子
            strMerge = strMerge + sLst.get(j).getKey();
        }
        strMerge = strMerge.toLowerCase();

        Matcher m = pattern.matcher(strMerge);
        String result = m.replaceAll("").trim();
        String md5val = org.apache.commons.codec.digest.DigestUtils.md5Hex(result);

        // 合并后的字符串如果为空或者为空格则认为没有内容
        if (StringUtils.isEmpty(result) || StringUtils.isEmpty(result.replaceAll("\\s+", "")))
        {
            md5val = null;
        }

        return md5val;
    }

    public static void main(String[] args){
        Md5ValidationV1 self = new Md5ValidationV1();
        String content = "<p><img src=\"http://img.lem88.com/flyshare/upload/spiderimg2/20160914/707511428cc146db84def8c4a08aaef0.jpg?400*400\" ></img></p>";
        //System.out.println(CleanText.removeHtmlTag(content));
        System.out.println(self.execute("this is a boy", content));
    }
}
