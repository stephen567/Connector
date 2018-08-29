package cn.com.syscom.banksys.connector.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ThreadFactory;
//import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import cn.com.syscom.banksys.connector.handler.ConnectionWatchdog;
import cn.com.syscom.banksys.connector.handler.CustomLengthFieldBasedFrameDecoder;
import cn.com.syscom.banksys.connector.handler.CustomLengthFieldPrepender;
import cn.com.syscom.banksys.connector.handler.HeartBeatHandler;
import cn.com.syscom.banksys.connector.handler.MsgInboundHandler;
import cn.com.syscom.banksys.connector.handler.RespOutboundHandler;

public class MsgClient 
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Channel channel;
	private EventLoopGroup group;
    private Bootstrap bootstrap;
    
    //private MsgInboundHandler msgHandler = new MsgInboundHandler();
   
    protected final HashedWheelTimer timer = new HashedWheelTimer(new ThreadFactory() 
    {
		private AtomicInteger threadIndex = new AtomicInteger(0);
		
		public Thread newThread(Runnable r) 
		{
			return new Thread(r, "NettyClientConnectorExecutor_" + this.threadIndex.incrementAndGet());
		}
	});
	
	private String RemoteServerIP = "192.168.10.139";
	private int RemotePort = 9999;
	private boolean reConnect = true;
	private int RetryTimes = 12;
    
    public MsgClient()
    {
    	init();
    }
    
    public MsgClient(String host, int port) 
    {
        this.RemoteServerIP = host;
        this.RemotePort = port;
        
        init();
    }
    
    public MsgClient(String classPathConfigFile)
    {
/*
    	Properties properties = new Properties();
    	
    	try
    	{
    		Resource res = new ClassPathResource(classPathConfigFile);
    		//Resource res = new FileSystemResource(classPathConfigFile);
    		properties.load(res.getInputStream());
    	}
    	catch (IOException e)
    	{
    		throw new RuntimeException("Failed to load IP Config File from classpath");
    	}
    	
    	String ip = properties.getProperty("Host");
    	int port = Integer.parseInt(properties.getProperty("Port"));
    	int reConnect = Integer.parseInt(properties.getProperty("reConnect"));
    	int retryTimes = Integer.parseInt(properties.getProperty("RetryTimes"));
    	
    	logger.debug("��IP�����ļ�{}�л�ȡ�����IPΪ:{}", classPathConfigFile, ip);
    	logger.debug("��IP�����ļ�{}�л�ȡ�����PortΪ��{}", classPathConfigFile, port);
    	logger.debug("��IP�����ļ�{}�л�ȡ������־Ϊ��{}", classPathConfigFile, reConnect);
    	logger.debug("��IP�����ļ�{}�л�ȡ�����������Ϊ��{}", classPathConfigFile, retryTimes);
    	
    	this.RemoteServerIP = ip;
    	this.RemotePort = port;
    	this.RetryTimes = retryTimes;
    	if (reConnect == 1)
    	{
    		this.reConnect = true;
    	}
    	else
    	{
    		this.reConnect = false;
    	}
*/
    	loadConfig(classPathConfigFile);
    	
    	init();
    }
    
    public void loadConfig(String configPath)
    {
    	Properties props = new Properties();
    	
    	try
    	{
    		Resource res = new ClassPathResource(configPath);
    		//Resource res = new FileSystemResource(classPathConfigFile);
    		props.load(res.getInputStream());
    	}
    	catch (IOException e)
    	{
    		logger.warn("��ȡǰ��ͨѶ�����ļ�{}����", configPath);
    		e.printStackTrace();
    	}
    	
    	String inString;
    	
    	inString = props.getProperty("Host");
    	if (inString != null)
    	{
    		this.RemoteServerIP = inString;
    		logger.debug("��IP�����ļ�{}�л�ȡ�����IPΪ:{}", configPath, RemoteServerIP);
    	}
    	
    	inString = props.getProperty("Port");
    	if (inString != null)
    	{
    		this.RemotePort = Integer.parseInt(inString);
    		logger.debug("��IP�����ļ�{}�л�ȡ�����PortΪ:{}", configPath, RemotePort);
    	}
    	
    	inString = props.getProperty("reConnect");
    	if (inString != null)
    	{
    		if(Integer.parseInt(inString) == 1)
    		{
    			this.reConnect = true;
    		}
    		else
    		{
    			this.reConnect = false;
    		}
    		logger.debug("��IP�����ļ�{}�л�ȡ�Ϻ�������־Ϊ:{}", configPath, reConnect);
    	}

    	inString = props.getProperty("RetryTimes");
    	if (inString != null)
    	{
    		this.RetryTimes = Integer.parseInt(inString);
    		logger.debug("��IP�����ļ�{}�л�ȡ�Ϻ������������Ϊ��{}", configPath, RetryTimes);
    	}
    }
    
    protected void init()
    {
    	group = new NioEventLoopGroup();
    	bootstrap = new Bootstrap().group(group);
    	
    	bootstrap.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT)
		.option(ChannelOption.SO_REUSEADDR, true)
		.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) SECONDS.toMillis(3))
		.channel(NioSocketChannel.class);
    	
    	bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
    	.option(ChannelOption.TCP_NODELAY, true)
		.option(ChannelOption.ALLOW_HALF_CLOSURE, false);
    }
    
    public Channel connect(String RemoteIP, int port)
    {
    	this.RemoteServerIP = RemoteIP;
    	this.RemotePort = port;
    	
    	return connect();
    }
    
    public Channel connect( )
    {
       // ����watchdog
        final ConnectionWatchdog watchdog = new ConnectionWatchdog(bootstrap, timer,this.RemoteServerIP, this.RemotePort, this.RetryTimes) 
        {
           public ChannelHandler[] handlers() {
               return new ChannelHandler[] {
                		//���Լ�[ConnectionWatchdog]װ�ص�handler���У�����·�ϵ�֮�󣬻ᴥ��ConnectionWatchdog #channelInActive����
                        this,
                        //ÿ��30s��ʱ�䴥��һ��userEventTriggered�ķ���������ָ��IdleState��״̬λ��WRITER_IDLE
                        new IdleStateHandler(0, 10, 0),
                        //ʵ��userEventTriggered����������state��WRITER_IDLE��ʱ����һ����������sever�ˣ�����server���һ�����
                        new HeartBeatHandler(),
                        new LengthFieldBasedFrameDecoder(2048,0,2,2,4),
                        //new CustomLengthFieldBasedFrameDecoder(2048,0,2,2,4,2),
                        //new StringDecoder(),
                        //new CustomLengthFieldPrepender(1, false, 1),
                        //new RespOutboundHandler(),
                        new MsgInboundHandler("Producer.xml")
                };
           }};

        watchdog.setReconnect(this.reConnect);

        try 
        {
            ChannelFuture future;
            synchronized (bootstrap) {
               bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            	   @Override
                   protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(watchdog.handlers());
                   }
                });

       		   logger.info("ͨѶ�ͻ��˿�ʼ���ӷ�����......");
               future = bootstrap.connect(this.RemoteServerIP, this.RemotePort);
           }

            future.sync();
            channel = future.channel();
        } catch (Exception e) 
        {
            e.printStackTrace();
        }
        
        if (channel != null)
        {
        	logger.info("ͨѶ�ͻ������ӷ������ɹ��� IP:{}  Port:{} ", this.RemoteServerIP, this.RemotePort);
        } else
        {
        	logger.error("ͨѶ�ͻ������ӷ�����ʧ�ܡ� IP:{}  Port:{} ", this.RemoteServerIP, this.RemotePort);
        }

		return channel;
   }
	
	public void stop()
	{
		if (channel != null)
			channel.close();
		
		if (group != null)
			group.shutdownGracefully();
		
		logger.info("ͨѶ�ͻ��˹ر�!");
	}
}
