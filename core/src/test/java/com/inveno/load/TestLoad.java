package com.inveno.load;


import java.util.ArrayList;
import java.util.List;

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

public class TestLoad {
	
	public static void main(String[] args) {
		
		if( args.length <= 0 ){
			System.out.println("输入入参");
			return ;
		}
		
		if( args.length !=3 ){
			System.out.println("输入入参需要为3个,uid app infoId");
			return ;
		}
		TTransport transport = new TSocket("internal-acs-new-159644148.us-east-1.elb.amazonaws.com",9000,5000);
		transport = new TSocket("internal-acs-new-1469254322.ap-southeast-1.elb.amazonaws.com",9000,5000);
    	TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
    	AcsService.Client client = new AcsService.Client(protocol);
    	
    	List<String> resultList = new ArrayList<String>();
    	resultList.add((args[0]+args[1]+args[2]).trim());
    	try {
			transport.open();
			
			List<Status> list = client.existMul(SysType.USER_READ, resultList, 1000);
			System.out.println(list);
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		}finally{
			transport.close();
		}
		
		
	}

}
