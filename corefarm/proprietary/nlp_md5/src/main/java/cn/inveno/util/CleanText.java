package cn.inveno.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.nlpcn.commons.lang.jianfan.JianFan;

/**
 * 文本预处理
 * @author minwei.yang
 *
 */
public class CleanText {

    private static final String[] NUMBER_STR_ARRAY = {
        "㈠㈡㈢㈣㈤㈥㈦㈧㈨㈩",
        "①②③④⑤⑥⑦⑧⑨⑩",
//        "一二三四五六七八九十",
        "⑴⑵⑶⑷⑸⑹⑺⑻⑼⑽⑾⑿⒀⒁⒂⒃⒄⒅⒆⒇",
        "⒈⒉⒊⒋⒌⒍⒎⒏⒐⒑⒒⒓⒔⒕⒖⒗⒘⒙⒚⒛",
        "ⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩⅪⅫ",
        "ⅰⅱⅲⅳⅴⅵⅶⅷⅸⅹ"
    };
    private static final char[] EMOJI_NUM_ARRAY = {
            '\uE225', // Emoji 表情 中的 0，后面的依次是1-9
            '\uE21C', 
            '\uE21D',
            '\uE21E',
            '\uE21F',
            '\uE220',
            '\uE221',
            '\uE222',
            '\uE223',
            '\uE224',
            };
    private static Map<Character,String> numberMap = new HashMap<Character,String>(); 
    
    private static final String[] SIGN_STR_ARRAY = {
        "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~", // [\41,\177) 之间的符号(8进制)
        "！＂＃＄％＆＇（）＊＋，－．／：；＜＝＞？＠［＼］＾＿｀｛｜｝～", // [\uFF01 , \uFF5F) 之间的符号
        "。、…‘’“”·∶﹕—﹗〖〗【】〔〕〈〉『』「」《》〃‖々―∕※", //  其他横排符号
//        "︻︼︽︾︵︶︹︺︿﹀﹁﹂﹃﹄︻︼︷︸ ︴", // 竖排符号
        "♂ ♀", //其他常见特殊符号
//        "ˉˇ¨" // 不明含义的符号
    };
    private static Pattern allowCharPattern = null; // 消息体允许出现的字符
    
    static{
        // 加载所有的数字序号
        for(String oneLine : NUMBER_STR_ARRAY){
            for(int i = 0; i< oneLine.length(); i++){
                numberMap.put(oneLine.charAt(i), ""+(i+1));
            }
        }
        for(int i = 0; i< EMOJI_NUM_ARRAY.length; i++){
            numberMap.put(EMOJI_NUM_ARRAY[i], ""+i);
        }
        
        // 初始化 允许出现的字符 的正则式
        StringBuilder appendStr = new StringBuilder();
        appendStr.append("[");
        appendStr.append("\41-\177\u4E00-\u9FA5");
        for(int i = 2; i < SIGN_STR_ARRAY.length; i++ ){
            appendStr.append(SIGN_STR_ARRAY[i]);
        }
        
        appendStr.append("]+");
        
        allowCharPattern = Pattern.compile(appendStr.toString());
    }
    
    /**
     * 单字符 全角转半角
     * @param c
     * @return
     */
    final public static char  toDBC(final char c){
        if (c == '\u3000') {
            return ' ';
        } else if (c > '\uFF00' && c < '\uFF5F') {
            return (char) (c - 65248);
        }else return c;
    }
    
    /**
     * 对字符串进行归整化处理（降噪） 包含：全角转半角、数字序号转数字、繁体中文转简体中文、罕见字符过滤
     * @param rawText
     * @return
     */
    public static String chineseDenoise(String rawText){
        StringBuilder tmpStrBuilder = new StringBuilder();
        StringBuilder resultStrBuilder = new StringBuilder();
        
     // 繁体中文转简体中文
        String str = JianFan.f2J(rawText);
        
        for(int i = 0; i< str.length(); i++){
            char oneChar = str.charAt(i);
            oneChar = toDBC(oneChar); // 全角转半角
            
            if(numberMap.containsKey(oneChar)){
                tmpStrBuilder.append(numberMap.get(oneChar)); // 数字序号转数字
            }else{
                tmpStrBuilder.append(oneChar);
            }
            
       }
       
       // 只保留允许的字符
       Matcher allowCharMatcher = allowCharPattern.matcher(tmpStrBuilder.toString());
       while(allowCharMatcher.find()){
           resultStrBuilder.append(allowCharMatcher.group());
       }
       
       return resultStrBuilder.toString();
    }
    
    /**
     * 清理html标签 （段落转为\n）
     * @param rawText
     * @return
     */
    public static String removeHtmlTag(String rawText) {
        String text = rawText == null ? "" : rawText;
        String result = StringEscapeUtils.unescapeHtml4(text) // html 解码
                .replaceAll(" ", " ") 
                .replaceAll("&apos;", "'") // html 解码遗漏的单引号转换
                .replaceAll("<!--.*?-->", "")
                .replaceAll("<\\s+", "<")
                .replaceAll("\\s+>", ">")
                .replaceAll("<[Ss][Tt][Yy][Ll][Ee]([^>])*>.*?</[Ss][Tt][Yy][Ll][Ee]>", "")
                .replaceAll("<[Ss][Cc][Rr][Ii][Pp][Tt]([^>])*>.*?</[Ss][Cc][Rr][Ii][Pp][Tt]>", "")
                .replaceAll("<[A-Za-z]([^>])*>", "")
                .replaceAll("</[p|P]([^>])*>", "\n")
                .replaceAll("</[A-Za-z]([^>])*>", "")
                .replaceAll("<\\\\/[p|P]([^>])*>", "\n")
                .replaceAll("<\\\\/[A-Za-z]([^>])*>", "");
        return result;
    }
    
    /**
     * 清理html标签 （全部整合为一行）
     * @param rawText
     * @return
     */
    public static String removeHtmlTag2(String rawText) {
        return removeHtmlTag(rawText).replaceAll("\n", " ");
    }
    
    /**
     * 全角转半角
     * @param input String.
     * @return 半角字符串
     */
    public static String toDBC(String input) {
         char c[] = input.toCharArray();
         for (int i = 0; i < c.length; i++) {
             if (c[i] == '\u3000') {
                 c[i] = ' ';
             } else if (c[i] > '\uFF00' && c[i] < '\uFF5F') {
                 c[i] = (char) (c[i] - 65248);
             }
         }
         String returnString = new String(c);
         return returnString;
    }
    
    public static void main(String[] args) {
        String str = "《这是测试》。... testing×７８９¤♂♀中國⒅⒆⒇";
        System.out.println(CleanText.chineseDenoise(str));
        
        String htmlStr = "<  h1 >title < \\/h1><p>p1</p>p2";
        System.out.println(CleanText.removeHtmlTag(htmlStr));
        System.out.println(CleanText.removeHtmlTag2(htmlStr));
    }

}
