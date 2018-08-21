package cn.com.syscom.banksys.connector.mq;


import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQReceiveMessageListener implements MessageListener 
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void onMessage(Message message) 
	{
		// TODO Auto-generated method stub
		logger.info("MQ收到报文： {}", message.toString());
	}
}
