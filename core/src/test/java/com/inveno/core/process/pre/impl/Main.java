package com.inveno.core.process.pre.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.alibaba.fastjson.JSON;

public class Main {
	
	public static void main(String[] args)  {
		
		/*System.out.println(128&0x00000091); 
		System.out.println(0x00000400);*/
		
		System.out.println(0x00001007&1059);
		
		/*System.out.println();
		System.out.println(0x0000313f&0x02);
		System.out.println(1059&0x0000313f);//1024+1+16+8 //12607 
		
		
		Map<String, Double> memeGMPByApp = new HashMap<String, Double>();
		
		double a = memeGMPByApp.get("1");
		
		
		System.out.println(8192&0x00000040);
		
		
		System.out.println((int)(69643&0xff) == 11 );
		
		String.valueOf(1);
		
		String product2TimeZone = "{\"aNum\":\"-3\",\"bNum\":1}";
		System.out.println(product2TimeZone);
		System.out.println(JSON.parseObject(product2TimeZone, Map.class).get("aNum"));
		
		System.out.println("-------------");
		Date date = new Date(System.currentTimeMillis()); // 2014-1-31 21:20:50    
        System.out.println(date);    
        Calendar calendar = Calendar.getInstance();    
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+"+9));    
        // 或者可以 Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));    
        calendar.setTime(date);    
        System.out.println(calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
        System.out.println(calendar.get(Calendar.SECOND));
		
		System.out.println("-------------");
		String [] aa = new String[]{"1","2"};
		
		doSome(aa);
		
		String strValue = "3";
		
		//System.out.println(Integer.parseUnsignedInt(strValue.substring(2), 16));
		
		String[] arr = "11".split("#");
		
		System.out.println(arr.length);
		for (String string : arr) {
			System.out.println(string);
		}
		
		System.out.println("40564637#0#2#1047".split("#").length);*/
		
		
		/*String aaaa = "0xa9e3bdc0276c378a78ce912127afdbc3";
		
		System.out.println(aaaa.length());
 		System.out.println(new BigInteger(aaaa.substring(2), 16));
 		
 		List list = new ArrayList();
		List reList = new ArrayList();
		list.add("1");
		list.add("2");
		list.add("3");
		list.add("4");
		list.add("5");
		System.out.println(list.subList(0, 1));
		System.out.println(list.subList(1, 3));*/
		/*String json = "{\"categoryId\":[],\"rank\":10}";
		JSONObject jsonObject = JSONObject.parseObject(json);
		System.out.println(jsonObject.getJSONArray("categoryId").toString());*/
		/*List<Integer> categoryIds = JSON.parseArray(jsonObject.getJSONArray("categoryId").toString(), Integer.class);
		
		System.out.println(categoryIds);*/
		/*Set<String> resultSet = new HashSet<String>();
		resultSet.add("1");
		resultSet.add("2");
		
		
		resultSet.remove("-1");*/
		
		/*Set<ResponParam> resultSet = new HashSet<ResponParam>();
		
		
		ResponParam res1 = new ResponParam("8", "1016336008");
		res1.setGmp(0.0549415);
		res1.setAdultScore(0.0);
		res1.setLanguage("Hindi");
		res1.setImgCnt(1);
		
		ResponParam res = new ResponParam("4", "1016336008");
		
		resultSet.add(res1);
		resultSet.add(res);
		
		System.out.println(resultSet);*/
		
		//System.out.println((int)( 0x010100&0xff) !=0);
		
		/*String json = "{\"relatedArticle\":[\"[33544167,33275469,33223636,33366261,33681690]\"],\"click\":[\"33812754\"]}";
		
		JSONObject jsonObject = JSON.parseObject(json);
		
		List<String> str= JSON.parseArray(jsonObject.getJSONArray("relatedArticle").toString(), String.class);
		
		System.out.println(JSON.parseArray(str.get(0),String.class));*/
		
		//System.out.println(str.get(0).);
		
		/*jsonObject.
		
		 JSON.parseArray(jsonObject.getJSONArray("categoryId").toString(), Integer.class);
		 
		 List<String> str = Arrays.asList(map.get("relatedArticle").get(0).split(","));
		 System.out.println(str);
		System.out.println();*/
		
		/*String json = "[{\"rule_id\":0,\"source\":[[102],[3]],\"ratio\":[0.5,0.5],\"range\":[1,2,3]},{\"rule_id\":1,\"source\":[[3],[101]],\"ratio\":[0.1,0.9],\"range\":[6,7,8]},{\"rule_id\":2,\"source\":[[3],[0]],\"ratio\":[0.6,0.4],\"range\":[14,15,16]},{\"rule_id\":3,\"source\":[[0],[101]],\"ratio\":[0.6,0.4],\"range\":[21,22]},{\"rule_id\":4,\"source\":[[103],[101],[0]],\"ratio\":[0.1,0.2,0.7],\"range\":[23,24,25,28]},{\"rule_id\":5,\"source\":[[100]],\"ratio\":[1],\"range\":[30]},{\"rule_id\":6,\"source\":[[101],[103]],\"ratio\":[0.1,0.9],\"range\":[34,35,36]}]";
		
		String json1 = "{\"rule_id\":0,\"source\":[[102],[3]],\"ratio\":[0.5,0.5],\"range\":[1,2,3]}";
 		
		MixRule mixRule = JSON.parseObject(json1,MixRule.class);
		
		System.out.println(mixRule.getRange());*/
		
		//
	/*	List<String> list = JSON.parseArray(json,String.class);
		for (int i = 0 ; i< list.size() ; i++) {*/
			/*
//			System.out.println(list.get(i));
			
//			MixRule mixRule = JSON.parse(list.get(i),MixRule.class);
		//	MixRule mixRule = JSON.parseObject(list.get(i),MixRule.class);
			JSONObject jsonObject = JSON.parseObject(list.get(i));
			
			
			List<List> list1 = JSON.parseArray(jsonObject.get("source").toString(),List.class) ;
			 
			List<Double> ratioList = JSON.parseArray(jsonObject.get("ratio").toString(), Double.class);
			
			System.out.println(list1 + "," + ratioList);
 			
			for (int j = 0; j < list1.size(); j++) {
				System.out.println(JSON.parseArray(list1.get(j).toString(),Integer.class));
				System.out.println(JSON.parseArray(jsonObject.get("ratio").toString(),Double.class).get(j));
			}
			
			
			for (String string2 : list1) {
				System.out.println(JSON.parseArray(string2,String.class));
			}
			
			//jsonObject.get("range");
			
		}*/
	 
		/*int i = -1;
		System.out.println(i);*/
		/*System.out.println(0xCFF&0x08);
		
		System.out.println((int)(0.4*8));
		
		
		System.out.println(Math.round(0.6*3));
		System.out.println(3.2>3);
		*/
		//String ifn = "865267021579270:51,353490069873400:51,353490069873327:81,867368020120401:62,353490069871354:46,353490069871016:51,99000559216734:46,A000004F5C2666:60,865308024524267:46,864821028347106:51,353490069873442:111,865707027936693:31,99000556092437:60,99000524489050:37,353490069871347:117,868618025859026:60,865267021579270:51,353490069873400:51,353490069873327:81,867368020120401:62,353490069871354:46,353490069871016:51,99000559216734:60,A000004F5C2666:60,865308024524267:46,864821028347106:51,353490069873442:106,865707027936693:31,99000556092437:60,99000524489050:37,868618025859026:60";
		/*if(ifn.contains("865267021579270")){
			System.out.println(true);
		}*/
		
		
		/*System.out.println(0x01050f&0xff);
		
		List list = new ArrayList();
		List reList = new ArrayList();
		list.add("1");
		list.add("2");
		list.add("3");
		list.add("4");
		list.add("5");
		
		List list1 = list;
		System.out.println(list.subList(2, list.size()));
		System.out.println(list);
		
		reList.add("8");
		
		reList.addAll(list.subList(2, list.size()));
		System.out.println(reList);*/
		
		/*
		int cnt =3 ;
		
		List list1 = new ArrayList();
		for (int i = list.size() -1 ; i> list.size() -cnt  -1; i--) {
			System.out.println(0);
			list1.add(list.get(i));
		}
 		 
		System.out.println(list1);
		
		
		list.removeAll(list1);
		System.out.println(list1);
		System.out.println(list);
		*/
		
		//System.out.println(0x01051d);
		//System.out.println(Integer.toHexString(65920));
		
		/*List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(2);
		list.add(3);
		
		System.out.println( list.contains(2) );*/
		
		/*ClassPathResource resource = new ClassPathResource("applicationContext-resources.xml");  
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();  
		XmlBeanDefinitionReader  reader = new XmlBeanDefinitionReader(factory);  
		reader.loadBeanDefinitions(resource);*/
		
		//System.out.println(getNum());
		
		/*Set<String> idSet = new HashSet<String>();
		idSet.add("1");
		idSet.add("2");
		idSet.add("3");
		idSet.add("4");
		
		String[] str = idSet.toArray(new String[0]);
		System.out.println(Arrays.asList(str));*/
		
	}
	
	
	

	private static void doSome(String[] aa) {
		aa[0] = null;
		
	}




	private  static int getNum() {
		 
		try {
			int i = 1/0;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(1);
			return 0;
		}finally{
			System.out.println("finally");
		}
		return 22;
	}

}
