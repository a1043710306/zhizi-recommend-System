package com.inveno.load;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * 
 *
 */
public class PattenString {

	public static void main(String[] args) {
		
		
		
		String line ="q uid: 01011612241430317001000049920906 ,app: gionee ,abtest: 123 and return";
		String app	 = line.substring(line.indexOf("app: ")+5, line.indexOf("abtest: ")-2);
		String uid = line.substring(line.indexOf("uid: ")+5, line.indexOf(" ,app:"));
		System.out.println(app +"," +uid);
		
		
		
		
		
		
		
		
		
		
		/*

		// 先进行写死
		String inputStr = "abcd efgh aabb ccdd 1122 1234 1213";
		
		// 判断入参
		if (args.length <= 0) {
			System.out.println("请输入模式入参");
			return;
		}

		String patten = args[0];
		if (patten.length() != 4) {
			System.out.println("请输入长度为4");
			return;
		}
		
		//其中patten有4*4*4*4 种模式:情况
		
		String arr[] = inputStr.split(" ");
		for (String string : arr) {
			Pattern p = Pattern.compile(patten);
			Matcher m = p.matcher(string);
			while (m.find()) {
				System.out.println(m.group());
			}
		}
		
	*/}

}
