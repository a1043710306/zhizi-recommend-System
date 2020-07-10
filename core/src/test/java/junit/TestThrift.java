package junit;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.inveno.thrift.PredictService;

public class TestThrift {
	
	public static void main(String[] args) {
		
		TTransport transport = new TSocket("", 2222);
		TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
		try {
			transport.open();
		} catch (TTransportException e) {
 			e.printStackTrace();
		}
		
		PredictService.Client client = new PredictService.Client(protocol);
		
		//client.GetPredictList(req);
		
 	}
	
	

}
