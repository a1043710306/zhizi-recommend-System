package com.inveno.ser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;

import com.inveno.core.process.CoreHandler;
import com.inveno.thrift.NewsListResp;
import com.inveno.thrift.ResponParam;
import com.inveno.thrift.ResponseCode;

public class ThriftSerTest {
	
	public static void main(String[] args) throws TException {
		
		TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
		
		
		List<ResponParam> list = new ArrayList<ResponParam>();
		
		list.add(new ResponParam("8", "3213131"));
		list.add(new ResponParam("8", "3213133231"));
		list.add(new ResponParam("8", "3213131211"));
		
		NewsListResp res= new NewsListResp(ResponseCode.RC_SUCCESS,list);
		
		byte[] bytes = serializer.serialize(res);
		System.out.println(bytes.length);
		
		System.out.println(serializer.serialize(new ResponParam("8", "3213131")));
		
		TDeserializer deserializer = new TDeserializer(new TBinaryProtocol.Factory());
		NewsListResp res2 = new NewsListResp();
 		deserializer.deserialize(res2, bytes);
 		
 		System.out.println(res2);
		
		
 		/*System.out.println(CoreHandler.serializable(res).length);*/
 		
 		
 		
 		
 		
 		
	}

}
