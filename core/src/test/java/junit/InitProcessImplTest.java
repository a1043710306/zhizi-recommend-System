package junit;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.inveno.thrift.BaseInfo;
import com.inveno.thrift.NewsListResp;
import com.inveno.thrift.RecNewsListReq;
import com.inveno.thrift.TimelineNewsListReq;
import com.inveno.thrift.UserClickReq;


@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(locations={"classpath:spring-mvc.xml","classpath:applicationContext-*.xml"})
public class InitProcessImplTest {
	
	
	@Test
	public void test() throws TException{
		 
		
	}

}
