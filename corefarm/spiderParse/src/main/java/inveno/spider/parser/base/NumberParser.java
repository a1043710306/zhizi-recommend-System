package inveno.spider.parser.base;

import inveno.spider.parser.exception.ExtractException;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberParser {
	private static Pattern numberPattern = Pattern.compile("\\d+");
	
	public static int getReplyNumber(String strNumber,ParseStrategy.ReplyNumExtractionStrategy strategy) throws ExtractException{
		Matcher m = numberPattern.matcher(strNumber);
		ArrayList<String> numbers = new ArrayList<String>();
		while(m.find()){
			String strNum = m.group();
			numbers.add(strNum);
		}
		if(numbers.size()==1){
			int num = Integer.parseInt(numbers.get(0));
			return num;
		} // reply number and read number
		else if (numbers.size()==2){
			int num1 = Integer.parseInt(numbers.get(0));
			int num2 = Integer.parseInt(numbers.get(1));
			if(num1<num2) return num1;
			else return num2;
		} else {
			if (strategy == ParseStrategy.ReplyNumExtractionStrategy.Strict)
				throw new ExtractException("Cannot find reply_number in " + strNumber);
		    else return -1;
		}

	}
	
	public static int getClickNumber(String strNumber,ParseStrategy.ClickNumExtractionStrategy strategy) throws ExtractException{
		Matcher m = numberPattern.matcher(strNumber);
		ArrayList<String> numbers = new ArrayList<String>();
		while(m.find()){
			String strNum = m.group();
			numbers.add(strNum);
		}
		if(numbers.size()==1){
			int num = Integer.parseInt(numbers.get(0));
			return num;
		} // reply number and read number
		else if (numbers.size()==2){
			int num1 = Integer.parseInt(numbers.get(0));
			int num2 = Integer.parseInt(numbers.get(1));
			if(num1>num2) return num1;
			else return num2;
		} else {
			if (strategy == ParseStrategy.ClickNumExtractionStrategy.Strict)
				throw new ExtractException("Cannot find click_number in " + strNumber);
		    else return -1;
		}

	}
	

}
