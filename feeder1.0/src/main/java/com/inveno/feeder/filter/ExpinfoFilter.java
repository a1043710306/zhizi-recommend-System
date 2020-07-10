package com.inveno.feeder.filter;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.inveno.thrift.AcsService;
import com.inveno.thrift.Status;
import com.inveno.thrift.SysType;

/**
 * 流量探索过acs
 * @author klaus liu
 *
 */
public class ExpinfoFilter
{
	private static final Logger flowLogger = Logger.getLogger("feeder.flow");

	private static String  SERVERIP = "192.168.1.11";
	private static Integer SERVERPORT = 8080;
	private static Integer TIMEOUT = 1000;
	private static Integer ACSTIMEOUT = 1000;

	//静态初始化
	static
	{
		File configFile = new File("connection-info.properties");
		try
		{
			FileReader reader = new FileReader(configFile);
			Properties props = new Properties();
			props.load(reader);
			SERVERIP = props.getProperty("online-thrift-ip");
			SERVERPORT = Integer.valueOf(props.getProperty("online-thrift-port"));
			TIMEOUT = Integer.valueOf(props.getProperty("online-thrift-timeout"));
			ACSTIMEOUT = Integer.valueOf(props.getProperty("online-acs-timeout"));
		}
		catch(Exception e)
		{
			flowLogger.fatal("ExpinfoFilter read connection-info.properties file exception =", e);
		}
	}

	public static boolean insertACS(List<String> alKeys)
	{
		TTransport transport = null;
		boolean status = true;
		try {
			transport = new TSocket(SERVERIP, SERVERPORT, TIMEOUT);
			TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
			AcsService.Client client = new AcsService.Client(protocol);
			transport.open();
			client.checkAndInsertMul(SysType.USER_READ, alKeys, ACSTIMEOUT);
		}
		catch (Exception e)
		{
			flowLogger.error("TException is = ", e);
			status = false;
		}
		finally
		{
			if (null != transport)
			{
				transport.close();
			}
		}
		return status;
	}
	public static boolean checkByAcs(List<String> alKeys)
	{
		TTransport transport = null;
		boolean status = true;
		try {
			transport = new TSocket(SERVERIP, SERVERPORT, TIMEOUT);
			TProtocol protocol = new TBinaryProtocol(new TFramedTransport(transport));
			AcsService.Client client = new AcsService.Client(protocol);
			transport.open();
			List<Status> list = client.existMul(SysType.USER_READ, alKeys, ACSTIMEOUT);
			for (int j = 0; j < alKeys.size(); j++)
			{
				String key = (String)alKeys.get(j);
				int returnState = list.get(j).getValue();
				flowLogger.info("\t\t key=" + key + ", return state=" + returnState);
				if (returnState == 0)
				{
					status = false;
					break;
				}
			}
		}
		catch (TTransportException e)
		{
			flowLogger.error("TTransportException is = ", e);
		}
		catch (Exception e)
		{
			flowLogger.error("TException is = ", e);
		}
		finally
		{
			if (null != transport)
			{
				transport.close();
			}
		}
		return status;
	}
}
