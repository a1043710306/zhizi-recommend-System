package com.inveno.core.process.post.process;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;

import com.inveno.thrift.ResponParam;


public class Main {
	
	
	

	public static void main(String[] args) throws SocketException, Exception {
		
		Set<byte[]> set =new  HashSet<byte[]>();
		set.add("1".getBytes());
		set.add(null);
		
		byte[][] fields = set.toArray(new byte[0][0]);
		TDeserializer deserializer = new TDeserializer(new TCompactProtocol.Factory());
		ResponParam res = new ResponParam();
		try {
			deserializer.deserialize(res, null);
		} catch (TException e) {
			e.printStackTrace();
		}
		System.out.println("res :" +res);
		Thread.sleep(111111);
		
		ArrayBlockingQueue<String> q = new ArrayBlockingQueue<String>(10);
		q.add("1");
		System.out.println(q);
		
		
		LinkedBlockingQueue<String> l = new LinkedBlockingQueue<String>();
		l.add("1");
		l.add("2");
		System.out.println(l);
		
		Map<String,Integer> maxPosOfConfig = new ConcurrentHashMap<String,Integer>();
		maxPosOfConfig.put("41", 200);
		
		System.out.println(maxPosOfConfig.get("31"));
		/*16  2 1024
		17 31 12607
		1034370750#16#2#1024
		*"content_type":"0x00000011","display":"0x0000001f","language":"Spanish","link_type":"0x0000313f"
		*17 31  12607
		*16  2 1024
		*/
		System.out.println(8&55);
		System.out.println(1024&31);
		System.out.println(2&12607);
		
		List<String> toRemoveIDList = new ArrayList<String>();
		toRemoveIDList.add("1");
		toRemoveIDList.add("2");
		toRemoveIDList.add("3");
		
		String[] arr = toRemoveIDList.toArray(new String[0]);
		for (String string : arr) {
			System.out.println(string);
		}
 		
		/*String str = "NewsListRespzhizi(retcode:RC_SUCCESS, data:[ResponParam(strategy:8, infoid:1032221129, gmp:0.03233096, adultScore:0.0, categoryId:121, keywordScores:0.01924, language:Spanish, sourceRank:0), ResponParam(strategy:8, infoid:1032178445, gmp:0.03729276, adultScore:0.0, categoryId:121, keywordScores:0.013307, language:Spanish, sourceRank:0), ResponParam(strategy:8, infoid:1032358243, gmp:0.03546493, adultScore:0.0, categoryId:121, keywordScores:0.017139, language:Spanish, sourceRank:0), ResponParam(strategy:8, infoid:1032332387, gmp:0.02960854, adultScore:0.0, categoryId:121, keywordScores:0.018872, language:Spanish, sourceRank:0), ResponParam(strategy:8, infoid:1032154965, gmp:0.01832322, adultScore:0.5, categoryId:121, keywordScores:0.029712, language:Spanish, sourceRank:0), ResponParam(strategy:8, infoid:1032155289, gmp:0.02362383, adultScore:0.0, categoryId:121, keywordScores:0.024397, language:Spanish, sourceRank:0), ResponParam(strategy:8, infoid:1032438942, gmp:0.0, adultScore:0.0, categoryId:121, keywordScores:0.047893, language:Spanish, sourceRank:0), ResponParam(strategy:8, infoid:1032203466, gmp:0.03560172, adultScore:0.0, categoryId:121, keywordScores:0.01142, language:Spanish, sourceRank:0)], offset:16, ttl:600, reset:false";
		String pattern = "strategy:([0-9]), infoid:([0-9]+)";
		Pattern r = Pattern.compile(pattern);

	      // Now create matcher object.
		Matcher m = r.matcher(str);
		while(m.find()) {
			String infoStr = m.group();
			System.out.println(infoStr.substring(infoStr.indexOf("infoid:")+7));
		}*/
		
		//System.out.println(str.substring(str.indexOf("NewsListRespzhizi(retcode:RC_SUCCESS, data:[")+5, str.indexOf("], offset:16")));
		
		/*System.out.println("Hotoday_v2.2.8".compareTo("Hotoday_v2.2.7"));
		System.out.println("Hotoday_v2.2.8".compareTo("Hotoday_v2.2.8.1"));
		Map<String , Integer> map = new HashMap<String, Integer>();
		map.put("1", 1);
		map.put("2", 2);
		map.put("3", 3);
		map.put("4", 4);
		
		map.forEach( (key, value) -> {
			
			value += 2;
		});
		
		System.out.println(map);
		
		System.out.println(0x00ff00 & 0x011006);
		System.out.println(0x001000);*/
		
		/*
		
		String prefix ="192.168.1";
		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (addr.isSiteLocalAddress() && !addr.isLoopbackAddress()
							&& addr.getHostAddress().indexOf(":") == -1) {
						String sAddr = addr.getHostAddress();
						// boolean isIPv4 =
						// InetAddressUtils.isIPv4Address(sAddr);
						boolean isIPv4 = sAddr.indexOf(':') < 0;

						if (isIPv4 && sAddr.startsWith(prefix)){
							System.out.println("ip is " + sAddr);
						}
					}
				}
			}
		} catch (Exception ex) {
		}

	*/}}

