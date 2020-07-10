package com.inveno.load;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.inveno.thrift.AcsService;
import com.inveno.thrift.Status;
import com.inveno.thrift.SysType;

public class Main {
	
	public static void main(String[] args) throws TTransportException {
		
		if( args.length <= 0 ){
			System.out.println("输入入参");
			return ;
		}
		
		if( args.length !=1 ){
			System.out.println("输入入参需要为1个,file path");
			return ;
		}
		FileReader reader = null;
		List<String> resultList = new ArrayList<String>();
        try {
        	final LineIterator it = FileUtils.lineIterator(new File(args[0]), "UTF-8");
        	
        	TTransport transport = new TSocket("internal-acs-new-159644148.us-east-1.elb.amazonaws.com",9000,5000);
        	
        	transport = new TSocket("internal-acs-new-1469254322.ap-southeast-1.elb.amazonaws.com",9000,5000);
        	
        	TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
        	AcsService.Client client = new AcsService.Client(protocol);
        	transport.open();
        	int cnt =  0;
        	int totalCnt = 0;
        	try {
        	    while (it.hasNext()) {
        	        String line = it.nextLine();
        	        String app = "";
        	        String uid = "";
        	        try {
        	        	app	 = line.substring(line.indexOf("app:  ")+6, line.indexOf("end CoreHandler")-1);
        	        	uid = line.substring(line.indexOf("uid: ")+5, line.indexOf("  ,app:"));
					} catch (Exception e) {
						try {
							app	 = line.substring(line.indexOf("app: ")+5, line.indexOf("abtest: ")-2);
							uid = line.substring(line.indexOf("uid: ")+5, line.indexOf(" ,app:"));
						} catch (Exception e2) {
							System.out.println("Exception"+ e2);
						}
						
					}
        	        
        	        
        	        String pattern = "strategy:([0-9]), infoid:([0-9]+)";
        			Pattern r = Pattern.compile(pattern);
        		      // Now create matcher object.
        			Matcher m = r.matcher(line);
        			while(m.find()) {
        				String infoStr = m.group();
        				String infoId = infoStr.substring(infoStr.indexOf("infoid:")+7);
        				resultList.add( (uid+app+infoId).trim());
        			}
        			//14658*1000=14658000
        			if( resultList.size()>= 900 ){
        				try {
        					List<Status> reList = client.checkAndInsertMul(SysType.USER_READ, resultList, 1000);
        					System.out.println(resultList.get(0)+","+reList.get(0)+","+reList.size()+",inputList:"+resultList.size());
            				System.out.println("resultList cnt:" +cnt++);
            				totalCnt += resultList.size();
            				resultList.clear();
            			} catch (TTransportException e) {
            				e.printStackTrace();
            			} catch (TException e) {
            				e.printStackTrace();
            			}
        			}
        	    }
        	    
        	    if( resultList.size()<900 ){
        	    	try {
        	    		List<Status> reList = client.checkAndInsertMul(SysType.USER_READ, resultList, 1000);
        				System.out.println(resultList.get(0)+","+reList.get(0)+","+reList.size()+",inputList:"+resultList.size());
        				System.out.println("resultList cnt:" +cnt++);
        				totalCnt += resultList.size();
        				resultList.clear();
        			} catch (TTransportException e) {
        				e.printStackTrace();
        			} catch (TException e) {
        				e.printStackTrace();
        			}
        	    }
        	} finally {
        		transport.close();
        	    it.close();
        	}
        	System.out.println("totalCnt:"+ totalCnt);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
	}

}
