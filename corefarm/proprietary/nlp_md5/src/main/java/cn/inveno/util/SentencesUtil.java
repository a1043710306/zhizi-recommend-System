package cn.inveno.util;

import java.util.ArrayList;
import java.util.List;

public class SentencesUtil {
    private SentencesUtil(){};
    
    public static List<String> toSentenceList(String content) {
        return toSentenceList(content.toCharArray());
    }

    public static List<String> toSentenceList(char[] chars) {
        StringBuilder sb = new StringBuilder();

        List<String> sentences = new ArrayList<String>();

        for (int i = 0; i < chars.length; i++) {
            if ((sb.length() != 0)
                    || ((!Character.isWhitespace(chars[i])) && (chars[i] != ' '))) {
                sb.append(chars[i]);
                switch (chars[i]) {
                case '.':
                    if ((i < chars.length - 1) && (chars[(i + 1)] == ' ' ) ) {
                        insertIntoList(sb, sentences);
                        sb = new StringBuilder();
                    }
                    break;
                case '…':
                    insertIntoList(sb, sentences);
                    sb = new StringBuilder("…");
                    break;
                case '\t':
                case '\n':
                case '\r':
                case '!':
//                case ';':
                case '?':
                case '。':
                case '！':
//                case '；':
                case '？':
                    insertIntoList(sb, sentences);
                    sb = new StringBuilder();
                }
            }
        }

        if (sb.length() > 0) {
            insertIntoList(sb, sentences);
        }

        return sentences;
    }

    private static void insertIntoList(StringBuilder sb, List<String> sentences) {
        String content = sb.toString().trim();
        if (content.length() > 0)
            sentences.add(content);
    }
}
