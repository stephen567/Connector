package cn.com.syscom.banksys.connector.mq;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQSender 
{
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private String QUEUE_NAME;
	private Channel MQChannel;
	private Connection connection;
	private String MQUser;
	private String MQPasswd;
	
	
	public MQSender(String host) throws Exception
	{
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		connection = factory.newConnection();
		MQChannel = connection.createChannel();
		
		MQChannel.queueDeclare(QUEUE_NAME, false, false, false, null);
		
	}
	
	public void SendMsg(Byte[] msg) throws Exception
	{
		String message = msg.toString();
		
		
		MQChannel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
		logger.debug("Send the Message " + message + " to RabbitMQ.");
	}
	
	public void closeMQ() throws Exception
	{
		logger.info("¹Ø±ÕRabbitMQÍ¨µÀ£¡");
		
		MQChannel.close();
		connection.close();
	}

}
