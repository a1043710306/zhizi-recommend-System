package com.inveno.thrift;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class InitProcessImplTest {
	
	
	public static void main(String[] args) throws InterruptedException, TException  {
		for (int i = 0; i < 1; i++) {
			try {
				testInitProcessNew();
//				testInitProcess();
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
 			
 		}
		for (int i = 0; i < 1; i++) {
//			testInitProcess();//单功能测试;
			Thread.sleep(100);
		}
//		testqcnProcess();
		//testInitProcessNew();
		testConcurrent();//测试并发
//		testInitProcess();
		HashMap<String,String> map =  new HashMap<String,String> ();
		Set<Entry<String, String>> entry =  map.entrySet();
		
		 
	}
	
	
	
	private static void testInitProcessNew() {


		TTransport transport;
//		transport = new TFramedTransport(new TSocket("192.168.1.221", Integer.parseInt("29095"), Integer.parseInt("50000")));
		
//		transport = new TFramedTransport(new TSocket( "121.201.57.45", Integer.parseInt("29096"), Integer.parseInt("500000")));
//		transport = new TFramedTransport(new TSocket( "192.168.9.53", Integer.parseInt("19095"), Integer.parseInt("50000")));
		//国内线上测试
		transport = new TFramedTransport(new TSocket("121.201.57.45", Integer.parseInt("19095"), Integer.parseInt("50000000")));
		//海外线上测试
//		transport = new TFramedTransport(new TSocket( "52.91.111.162", Integer.parseInt("19096"), Integer.parseInt("500000000")));
		//ucloud
		transport = new TFramedTransport(new TSocket( "106.75.9.5", Integer.parseInt("19095"), Integer.parseInt("500000000")));
 		//transport = new TFramedTransport(new TSocket("192.168.9.129", Integer.parseInt("19095"), Integer.parseInt("500000")));
  		try {
			transport.open();
		} catch (TTransportException e1) {
			e1.printStackTrace();
		}
		TProtocol protocol = new TBinaryProtocol(transport);

		com.inveno.thrift.ZhiziCore.Client client = new com.inveno.thrift.ZhiziCore.Client(protocol);

		ZhiziListReq request = new ZhiziListReq();
 //		request.setNum(10);
		request.setNum(3);
// 		request.setAbTestVersion("280");
 		request.setAbTestVersion("121");
 		request.setAbTestVersion("301");
 		request.setAbTestVersion("121");
 		request.setAbTestVersion("359");
 		request.setAbTestVersion("369");
// 		request.setAbTestVersion("178");
 		request.setAbTestVersion("173");
 		
  		request.setNum(10);
		//com.inveno.thrift.BaseInfo base = new BaseInfo();
		//base.setApp("emui");
		int j = (int) (Math.random() * 10000);
		request.setUid("152322esss394" );
		request.setUid("152322esss333" );
		
		request.setUid("152322esss333216552");
		request.setUid("152322esss333218574");
		request.setUid("152322esss33321" + j);
		
//		request.setUid("152322esss333218963");
 		
		request.setApp("ali");
//		request.setApp("meizu");
		request.setApp("moxiulauncher");
		request.setApp("fuyiping-gionee");
		request.setApp("noticias");
		request.setApp("ali");
		request.setApp("ali");
		
//		request.setApp("tianyu");
		request.setOperation(2);
		request.setScenario("0x010100");
//		request.setScenario("0x010125");
		
//		request.setScenario("0x01050f");
//		request.setScenario("0x020500");//meizu
//		request.setScenario("0x01050f");
		
		//0x01050f
		request.setDisplay("0x08");
//		request.setDisplay("0x100");
		request.setLink_type("0x03");
		request.setContent_type("0x0001");
		request.setLanguage("zh_CN");
		
		
		
	/*	request.setApp("noticias");
		request.setDisplay("0x0000101f");
//		request.setDisplay("0x100");
		request.setLink_type("0x0000317f");
		request.setContent_type("0x00000013");
		request.setLanguage("Spanish");
		*/
		
		
		//lockScreen  713FF
		/*request.setDisplay("0x0000001e");
		request.setContent_type("0x00000001");
		request.setLink_type("0x0000313f");
		request.setScenario("0x0713FF");
		request.setAbTestVersion("180");*/
		// request.setType("1");
		 
		// request.needBanner=true;
		// request.setNeedBanner(true);
		//request.setBase(base);
		//request.setAbTestVersion("1");

		NewsListRespzhizi resp = null;
		try {
			resp = client.recNewsListZhizi(request);
		} catch (TException e) {
 			e.printStackTrace();
		}
		// NewsListResp resp = client.timelineNewsList(req1);
		System.out.println("result:" + resp);
		transport.close();

	
		
	}
	
	public static void testqcnProcess() throws TException {

		TTransport transport;
		transport = new TFramedTransport(new TSocket("192.168.1.221", Integer.parseInt("29095"), Integer.parseInt("5000000")));
		
//		transport = new TFramedTransport(new TSocket("192.168.1.221", Integer.parseInt("29095"), Integer.parseInt("50000")));
		
		transport = new TFramedTransport(new TSocket( "121.201.57.45", Integer.parseInt("30009"), Integer.parseInt("500000")));
//		transport = new TFramedTransport(new TSocket( "192.168.9.53", Integer.parseInt("19095"), Integer.parseInt("5000000")));
 		//transport = new TFramedTransport(new TSocket("192.168.9.129", Integer.parseInt("19095"), Integer.parseInt("500000")));
  		try {
			transport.open();
		} catch (TTransportException e1) {
			e1.printStackTrace();
		}
		TProtocol protocol = new TBinaryProtocol(transport);

		com.inveno.thrift.ZhiziCore.Client client = new com.inveno.thrift.ZhiziCore.Client(protocol);

		//RecNewsListReq request = new RecNewsListReq();
 //		request.setNum(10);
		
 		//request.setAbTestVersion("1");
  		/*request.setNum(10);
  		request.setNum(3);
  		request.setNeedBanner(true);*/
		com.inveno.thrift.BaseInfo base = new BaseInfo();
// 		base.setApp("fuyiping-gionee");
		base.setApp("tianyu");

		// request.setType("1");
		base.setVer("1.0");
		int j = (int) (Math.random() * 10000);
		//base.setUid("152322esss394" );
		
		base.setUid("152322esss33321" + j);
 		
		base.setUid("152322esss3332131933");
//		base.setUid("01011509162018371201000016183902");
		
//		base.setUid("152322esss3332176");
		
		base.setNet("123");
		base.setOs("111");
		base.setOsver("1");
		base.setTk("1");
		 //request.needBanner=true;
		 //request.setNeedBanner(true);
		//request.setBase(base);
		//request.setAbTestVersion("2");
/*
		UserClickReq req = new UserClickReq();
		req.setBase(base);
		req.setInfoId("14065896");
		req.setStrategy("4");*/

		TimelineNewsListReq req1 = new TimelineNewsListReq();
		req1.setBase(base);
		//req1.setLastInfoId(14065896);
		req1.setChannelId(1);
		req1.setChannelId(2);
		req1.setChannelId(0);
		req1.setNum(10);
		req1.setAbTestVersion("121");
		//req1.setNum(8);
		//req1.setNum(10);
		req1.setNum(4);
		//client.userClick(req);

		NewsListResp resp = client.timelineNewsList(req1);
		// NewsListResp resp = client.timelineNewsList(req1);
		System.out.println("result:" + resp);
		transport.close();

	}



	public static void testInitProcess() throws TException {
		
		TTransport transport;
		transport = new TFramedTransport(new TSocket("192.168.1.221", Integer.parseInt("19097"), Integer.parseInt("5000000")));
		
//		transport = new TFramedTransport(new TSocket("192.168.1.221", Integer.parseInt("29095"), Integer.parseInt("50000")));
		
		transport = new TFramedTransport(new TSocket( "121.201.57.45", Integer.parseInt("19095"), Integer.parseInt("500000000")));
		
//		transport = new TFramedTransport(new TSocket( "106.75.9.5", Integer.parseInt("19095"), Integer.parseInt("500000000")));
//		transport = new TFramedTransport(new TSocket( "192.168.9.53", Integer.parseInt("19095"), Integer.parseInt("5000000")));
 		//transport = new TFramedTransport(new TSocket("192.168.9.129", Integer.parseInt("19095"), Integer.parseInt("500000")));
  		try {
			transport.open();
			
			
		} catch (TTransportException e1) {
			e1.printStackTrace();
		}
		TProtocol protocol = new TBinaryProtocol(transport);

		com.inveno.thrift.ZhiziCore.Client client = new com.inveno.thrift.ZhiziCore.Client(protocol);

		RecNewsListReq request = new RecNewsListReq();
 //		request.setNum(10);
		
 		//request.setAbTestVersion("1");
//  		
  		request.setNum(1);
//		request.setNum(3);
		request.setNum(10);
//  		request.setNeedBanner(true);
		com.inveno.thrift.BaseInfo base = new BaseInfo();
//		base.setApp("emui");
		
		base.setApp("emui");
		base.setApp("tianyu");
 		/*base.setApp("0febb9b4-486");
 		
 		base.setApp("coolpad");*/
		base.setApp("coolpad");
		
		
		base.setApp("coolpad");
		
		base.setApp("coolpad");
		base.setApp("0febb9b4-486");
		base.setApp("fuyiping-gionee");
		base.setApp("emui");
		base.setApp("fuyiping-gionee");
		base.setApp("tianyu");
		base.setApp("coolpad");
		base.setApp("emui");
		// request.setType("1");
		base.setVer("1.0");
		int j = (int) (Math.random() * 10000);
		//base.setUid("152322esss394" );
		base.setUid("152322esss333223" );
		
		base.setUid("152322esss33321968");
		base.setUid("152322esss333216847");
		base.setUid("152322esss333215378");
		base.setUid("152322esss33321" + j);
//		
//		base.setUid("865267029013967");
		
//		base.setUid("01011509162018371201000016183902");
//		base.setUid("008600250817474");
//		base.setUid("99000773254597");
		
		base.setNet("3g");
		base.setOs("iOS");
		base.setOsver("2.1.5");
		base.setTk("1");
		 //request.needBanner=true;
		 //request.setNeedBanner(true);
		request.setBase(base);
		request.setAbTestVersion("82");
		request.setAbTestVersion("76");
		request.setAbTestVersion("60");
		request.setAbTestVersion("104");
		request.setAbTestVersion("99");
		request.setAbTestVersion("60");
		
		request.setAbTestVersion("115");
		
		request.setAbTestVersion("59");
		request.setAbTestVersion("94");
 		request.setAbTestVersion("99");
 		request.setAbTestVersion("120");
 		request.setAbTestVersion("334");
// 		request.setAbTestVersion("173");
// 		request.setAbTestVersion("109");
/*
		UserClickReq req = new UserClickReq();
		req.setBase(base);
		req.setInfoId("14065896");
		req.setStrategy("4");*/

		/*TimelineNewsListReq req1 = new TimelineNewsListReq();
		req1.setBase(base);
		req1.setLastInfoId(14065896);
		req1.setNum(3);*/
		//req1.setNum(8);
		//req1.setNum(10);

		//client.userClick(req);

		NewsListResp resp = client.recNewsList(request);
		// NewsListResp resp = client.timelineNewsList(req1);
		System.out.println("result:" + resp);
		try {
			
		} catch (Exception e) {
			// TODO: handle exception
		}finally{
			transport.close();
		}
		

	}
	
	
	public static void testConcurrent() throws InterruptedException{
		

 		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);

		//long begin = System.currentTimeMillis();
		for (int index = 0; index <= 50; index++) {

			long begin = System.currentTimeMillis();
			for (int j = 0; j < 20; j++) {

				// long end = System.currentTimeMillis();
				// 控制每秒钟最多多少个线程访问
				// System.out.println(end - begin);
				/*
				 * if(end - begin >=500){ Thread.sleep(2500);
				 * System.out.println(new
				 * SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
				 * begin = System.currentTimeMillis(); }
				 */
				Runnable run = new MythreadTest2(index);
				fixedThreadPool.execute(run);
			}
			long end = System.currentTimeMillis();
			//System.out.println("cost :"+ (end -begin)+ " ms "+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));

			System.out.println("50个完毕");
			Thread.sleep(1000);
		}
		fixedThreadPool.shutdown();
	
	}

}


class MythreadTest2 implements Runnable {
	int index;

	public MythreadTest2(int index) {
		this.index = index;
	}

	@Override
	public void run() {


		TTransport transport;
//		transport = new TFramedTransport(new TSocket("192.168.1.221", Integer.parseInt("29095"), Integer.parseInt("50000")));
		
//		transport = new TFramedTransport(new TSocket( "121.201.57.45", Integer.parseInt("29096"), Integer.parseInt("500000")));
//		transport = new TFramedTransport(new TSocket( "192.168.9.53", Integer.parseInt("19095"), Integer.parseInt("50000")));
		transport = new TFramedTransport(new TSocket("121.201.57.45", Integer.parseInt("19095"), Integer.parseInt("50000000")));
		
		transport = new TFramedTransport(new TSocket( "106.75.9.5", Integer.parseInt("19095"), Integer.parseInt("500000000")));
 		//transport = new TFramedTransport(new TSocket("192.168.9.129", Integer.parseInt("19095"), Integer.parseInt("500000")));
  		try {
			transport.open();
		} catch (TTransportException e1) {
			e1.printStackTrace();
		}
		TProtocol protocol = new TBinaryProtocol(transport);

		com.inveno.thrift.ZhiziCore.Client client = new com.inveno.thrift.ZhiziCore.Client(protocol);

		ZhiziListReq request = new ZhiziListReq();
 //		request.setNum(10);
		request.setNum(3);
 		request.setAbTestVersion("121");
 		request.setAbTestVersion("291");
 		request.setAbTestVersion("305");
 		request.setAbTestVersion("121");
// 		request.setAbTestVersion("280");
 		request.setAbTestVersion("173");
  		request.setNum(10);
		//com.inveno.thrift.BaseInfo base = new BaseInfo();
		//base.setApp("emui");
		int j = (int) (Math.random() * 10000);
		request.setUid("152322esss394" );
		request.setUid("152322esss333" );
		
		request.setUid("152322esss333216552");
		request.setUid("152322esss33321" + j);
//		request.setUid("01011606071626008301000000050801");
		
//		request.setUid("152322esss33321581223");
 		
		request.setApp("ali");
//		request.setApp("meizu");
		request.setApp("moxiulauncher");
		request.setApp("fuyiping-gionee");
		request.setApp("ali");
		
//		request.setApp("tianyu");
		request.setOperation(2);
		request.setScenario("0x010100");
//		request.setScenario("0x01050f");
//		request.setScenario("0x020500");//meizu
//		request.setScenario("0x01050f");
		
		//0x01050f
		request.setDisplay("0x07");
//		request.setDisplay("0x100");
		request.setLink_type("0x03");
		request.setContent_type("0x0001");
		request.setLanguage("zh_CN");
		
		request.setDisplay("0x08");
//		request.setDisplay("0x100");
		request.setLink_type("0x03");
		request.setContent_type("0x0001");
		request.setLanguage("zh_CN");
		// request.setType("1");
		 
		// request.needBanner=true;
		// request.setNeedBanner(true);
		//request.setBase(base);
		//request.setAbTestVersion("1");

		NewsListRespzhizi resp = null;
		try {
			resp = client.recNewsListZhizi(request);
		} catch (TException e) {
 			e.printStackTrace();
		}
		// NewsListResp resp = client.timelineNewsList(req1);
		System.out.println("result:" + resp);
		transport.close();

	
		
	}
}
