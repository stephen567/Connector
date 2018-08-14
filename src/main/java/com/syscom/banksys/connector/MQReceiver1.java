package com.syscom.banksys.connector;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Delivery;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelGroupFutureListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MQReceiver 
{
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private ConnectionFactory factory;
	private Connection connection;
	private Channel channel;
	
	private String exchangeName;
	private String queueName;
	private String routingKey;
	private String MQUser = "guest";
	private String MQPasswd = "guest";
	private String MQPort;
	
	private Thread listenThread;
	
	public MQReceiver() throws Exception
	{
		factory = new ConnectionFactory();
		factory.setHost("192.168.10.139");
		factory.setUsername("guest");
		factory.setPassword("guest");
		factory.setPort(5672);
		factory.setVirtualHost("/");
		
		connection = factory.newConnection();
		
		channel = connection.createChannel();
		
		channel.exchangeDeclare(exchangeName, "direct", true, false, null);
		channel.queueDeclare(queueName, true, false, false, null);
		channel.queueBind(queueName, exchangeName, routingKey);
		
		channel.basicQos(1);
		
		logger.info("MQReceiver进程初始化成功！");
	}
	
	public void start()
	{
		listenThread = new Thread() {
			@Override
			public void run() {
				Consumer consumer = new DefaultConsumer(channel);
				try
				{
					channel.basicConsume(queueName, false, consumer);
					
					while(true)
					{
						Delivery delivery = consumer.nextDelivery();
						String message = new String(delivey)
					}
				} catch(Exception e)
				{
					logger.info("创建RabbitMQ接受listener出错，%s", e.getMessage());
				}
			}
		};
	}
	
	public void respMsgAndAck(String message, final Channel channel, Delivery delivery)
	{
		if(EchoServerHandler.channels != null)
		{
			logger.info("");
		}
		
		ByteBuf msg = Unpooled.copiedBuffer(message.getBytes());
		EchoServerHandler.channels.writeAndFlush(msg).addListener(
				new ChannelGroupFutureListener() {
					public void operationComplete(ChannelGroupFuture f) throws Exception
					{
						channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
						logger.info("MQ Receiver收到信息！");
					}
				});
	}
	
	
}
