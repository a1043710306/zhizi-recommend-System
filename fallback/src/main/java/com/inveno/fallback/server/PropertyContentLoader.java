package com.inveno.fallback.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.inveno.fallback.IServiceLoader;


public class PropertyContentLoader implements IServiceLoader {

    private static Map<String,String> cacheData = new HashMap<String,String>();
	
    private Properties prop = new Properties();
    
    //后续修改成正则或者使用spring的resource方法
	public void loadService() {
		// TODO Auto-generated method stub
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("prop/es-mapping-value.properties");
		try {
			prop.load(is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//加载到内存中
		Set<Object> keys = prop.keySet();
		Iterator<Object> it = keys.iterator();
		while(it.hasNext()){
			Object objKey = it.next();
			if(objKey instanceof String){
				String realKey = (String)objKey;
				cacheData.put(realKey,(String)prop.get(realKey));
			}
		}
		
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}
	
	public static String getValues(String key){
		return cacheData.get(key);
	}
	
	public static void main(String[] args) {
		String str = "es-mapping-value";
        String regex = "\\wmapping\\w";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        System.out.println(m.matches());
	}
	
}
