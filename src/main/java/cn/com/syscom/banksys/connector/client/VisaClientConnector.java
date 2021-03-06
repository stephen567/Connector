package cn.com.syscom.banksys.connector.client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

import cn.com.syscom.banksys.connector.client.MsgClient;
import cn.com.syscom.banksys.connector.mq.MQReply;

public class VisaClientConnector 
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private static Channel ch;
	
	public static void main(String[] args)
	{
		//String configFilePath = "D:/WorkCode/Connector/src/main/resources/VISAComm.properties";
		String configFilePath = "VISAComm.properties";
		String ReplyMQConfig = "MQReply.properties";
		
		MsgClient client = new MsgClient(configFilePath);
		
		try
		{
			ch = client.connect();
			
			if (ch == null)
			{
				client.stop();
			}
		} 
		catch (Exception e)
		{
			client.stop();
			e.printStackTrace();
		}
		
		MQReply reply = new MQReply(ch);
		reply.loadConfig(ReplyMQConfig);
		
		reply.init();
	}
}
 
