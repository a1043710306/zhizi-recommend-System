package inveno.spider.reports.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.jsoup.Jsoup;

import tw.qing.sys.StringManager;
import tw.qing.util.TextUtil;

public class Utils {
	
	public static  Object[] getAttributesArray(String[] atrrList,String split,Object object){
        ArrayList<Object> objects = new ArrayList<Object>();
    	for (String atrr : atrrList) {
    		objects.add(getAttrValueByName(atrr,object));
		}
		return objects.toArray();
	}
	
	
	public  static  Object getAttrValueByName(String attrName,Object object){
		String getter = "get" + stringNormallize(attrName); 
		//System.err.println(getter);
		 try {
			Method method = object.getClass().getMethod(getter, new Class[] {});
			Object value = method.invoke(object, new Object[] {});    
	        return value; 
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	
	public  static String stringNormallize(String str) {  
	    char[] ch = str.toCharArray();  
	    if (ch[0] >= 'a' && ch[0] <= 'z') {  
	        ch[0] = (char) (ch[0] - 32);  
	    }  
	    return new String(ch);  
	}  
	
	public static void sendContentToWeichat(String[] receivers,String content) throws IOException {
		StringManager smgr = StringManager.getManager("system");
		String url = smgr.getString("report.weichat.url");
		
		for (String receiver : receivers) {
			sendContentToWeichat(url,receiver,content);
		}
	}
	
	
	public static void sendContentToWeichat(String url, String receiver, String content) throws IOException{
		//curl -H "Content-Type: application/json" -X POST  --data '{"phone":"chuling.lu","msg":"建杰喊你过来呢"}'  "http://172.31.31.10:24680/message/weixin"
		
		String requestBody = "{\"phone\":\""+receiver+"\",\"msg\":\""+content+"\"}";
		System.err.println(requestBody);
		Jsoup.connect(url).header("Content-Type", "application/json").requestBody(requestBody).timeout(20000).post();
	}
	
	public static void main(String[] args) {
		try {
			StringManager smgr = StringManager.getManager("system");
			String[] weichatReceiver = TextUtil.getStringList(smgr.getString("report.weichat.receiver"));
			sendContentToWeichat(weichatReceiver, ("Product Crawler Statistic \\n"+
			"firmapp : mata-Indonesian\\n"+
			"date  : 2016-11-20 \\n"+
			"crawler amount : 8810\\n"+ 
			"avalible  amount : 4940 \\n").replaceAll("\t", ""));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		try {
//			sendContentToWeichat("{\"phone\":\"chuling.lu\",\"msg\":\"秦越喊你过来\"}");
//			sendContentToWeichat("{\"phone\":\"jianjie.zhu\",\"msg\":\"建杰测试\"}");
//			sendContentToWeichat("{\"phone\":\"yue.qin\",\"msg\":\"建杰喊你过来呢\"}");
//			System.err.println("发送成功");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}
