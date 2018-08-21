package cn.com.syscom.banksys.connector.mq;


import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectorMQSender 
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private ApplicationContext context;
	private RabbitAdmin admin;
	private RabbitTemplate amqpTemplate;

	public ConnectorMQSender(String ClassPathXMLConfigFile)
	{
		logger.info("Class ConnectorMQSender Initialing......" );
		
		context = new ClassPathXmlApplicationContext(ClassPathXMLConfigFile);
			
		amqpTemplate = context.getBean(RabbitTemplate.class);
	}
	
	public void convertAndSend(Object message)
	{
		amqpTemplate.convertAndSend(message);
	}
	
	public void convertAndSend(String routingKey, Object message)
	{
		amqpTemplate.convertAndSend(routingKey, message);
	}
}
