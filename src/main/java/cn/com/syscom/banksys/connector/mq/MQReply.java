package cn.com.syscom.banksys.connector.mq;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQReply 
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Channel channel;
	private ChannelHandlerContext context;
	
	Properties properties;
	
	private String mq_ip = "192.168.10.139";
	private int mq_port = 5672;
	private String mq_user = "admin";
	private String mq_passwd = "cmbadmin";
	private String mq_virtualHost = "/";
	private String mq_exchange = "VISARespExchange";
	private String mq_queue = "VISARespQueue";
	private String mq_routingKey = "VISA_RESPONSE";

	public MQReply(Channel ch)
	{
		this.channel = ch;
	}
	
	public void loadConfig(String configPath)
	{
    	Properties properties = new Properties();
    	
    	try
    	{
    		Resource res = new ClassPathResource(configPath);
    		//Resource res = new FileSystemResource(classPathConfigFile);
    		properties.load(res.getInputStream());
    	}
    	catch (IOException e)
    	{
    		logger.warn("读取MQ Reply配置文件{}出错！", configPath);
    		e.printStackTrace();
    	}
    	
    	String inString;
    	
    	inString = properties.getProperty("mq.ip");
    	if (inString != null)
    	{
    		this.mq_ip = inString;
    		logger.debug("从MQ配置文件{}中获取应答MQ IP为:{}", configPath, mq_ip);
    	}
    	
    	inString = properties.getProperty("mq.port");
    	if (inString != null)
    	{
    		this.mq_port = Integer.parseInt(inString);
    		logger.debug("从MQ配置文件{}中获取应答MQ Port为:{}", configPath, mq_port);
    	}
    	
    	inString = properties.getProperty("mq.user");
    	if (inString != null)
    	{
    		this.mq_user = inString;
    		logger.debug("从MQ配置文件{}中获取应答User为:{}", configPath, mq_user);
    	}

    	inString = properties.getProperty("mq.password");
    	if (inString != null)
    	{
    		this.mq_passwd = inString;
    		logger.debug("从MQ配置文件{}中获取应答Password为:{}", configPath, mq_passwd);
    	}

    	inString = properties.getProperty("mq.virtualHost");
    	if (inString != null)
    	{
    		this.mq_virtualHost = inString;
    		logger.debug("从MQ配置文件{}中获取应答Virtual Host为:{}", configPath, mq_virtualHost);
    	}

    	inString = properties.getProperty("mq.exchange");
    	if (inString != null)
    	{
    		this.mq_exchange = inString;
    		logger.debug("从MQ配置文件{}中获取应答exchange为:{}", configPath, mq_exchange);
    	}

    	inString = properties.getProperty("mq.queue");
    	if (inString != null)
    	{
    		this.mq_queue = inString;
    		logger.debug("从MQ配置文件{}中获取应答queue为:{}", configPath, mq_queue);
    	}

    	inString = properties.getProperty("mq.routingkey");
    	if (inString != null)
    	{
    		this.mq_routingKey = inString;
    		logger.debug("从MQ配置文件{}中获取应答routing Key为:{}", configPath, mq_routingKey);
    	}
	}
	
	public void init()
	{
		CachingConnectionFactory factory = new CachingConnectionFactory(this.mq_ip);
		factory.setPort(this.mq_port);
		factory.setUsername(this.mq_user);
		factory.setPassword(this.mq_passwd);
		factory.setVirtualHost(this.mq_virtualHost);
/*		
		RabbitAdmin admin = new RabbitAdmin(factory);
		
		DirectExchange exchange = new DirectExchange(this.mq_exchange);
		admin.declareExchange(exchange);
		
		Queue queue = new Queue(this.mq_queue, true, false, false, null);
		admin.declareQueue(queue);
		
		admin.declareBinding(BindingBuilder.bind(queue).to(exchange).with(this.mq_routingKey));
*/		
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(factory);
		MessageListenerAdapter adapter = new MessageListenerAdapter(new MsgReplyProcess( this.channel));
		adapter.addQueueOrTagToMethodName(this.mq_queue, "MsgReply");
		
		container.setQueueNames(this.mq_queue);
		container.setMessageListener(adapter);
		container.start();
	}
	
	public class MsgReplyProcess
	{
		private Channel channel;
		
		public MsgReplyProcess(Channel channel)
		{
			this.channel = channel;
		}
		
		public void MsgReply(String message)
		{
			logger.info("MQ收到应答报文: {}", message);
			
			channel.writeAndFlush(message);
		}
	}
}




