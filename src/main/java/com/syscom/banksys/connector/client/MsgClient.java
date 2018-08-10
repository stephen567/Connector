package com.syscom.banksys.connector.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.syscom.banksys.connector.ConnectionWatchdog;
//import com.syscom.banksys.connector.ConnectorIdleStateTrigger;

public class MsgClient 
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	//private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private String FormattedDate()
	{
		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		
		return df.format(System.currentTimeMillis());
	}
	
	private Channel channel;
	private EventLoopGroup group;
    private Bootstrap bootstrap;
    
    private MsgClientHandler msgHandler = new MsgClientHandler();
    //private ConnectorIdleStateTrigger idleStateTrigger = new ConnectorIdleStateTrigger();
    protected final HashedWheelTimer timer = new HashedWheelTimer(new ThreadFactory() {
		private AtomicInteger threadIndex = new AtomicInteger(0);
		
		public Thread newThread(Runnable r) 
		{
			return new Thread(r, "NettyClientConnectorExecutor_" + this.threadIndex.incrementAndGet());
		}
	});
	
	private String RemoteServerIP = "192.168.10.176";
	private int RemotePort = 9999;
	
    public MsgClient(String host, int port) 
    {
        this.RemoteServerIP = host;
        this.RemotePort = port;
    }
    
    public MsgClient()
    {
    	init();
    }

    public void start() throws Exception
    {
    	group = new NioEventLoopGroup();
    	
    	try
    	{
    		bootstrap = new Bootstrap();
    		bootstrap.group(group)
    		 .channel(NioSocketChannel.class)
    		 .remoteAddress(new InetSocketAddress(RemoteServerIP, RemotePort))
    		 .handler(new MsgChannelHandlerInitializer());
    		
    		ChannelFuture f = bootstrap.connect().sync();
    		logger.info("{}: 通讯客户端连接到服务器。 IP:{}  Port:{} ",FormattedDate(), this.RemoteServerIP, RemotePort);
    		
    		f.channel().closeFuture().sync();
    	} 
    	finally
    	{
    		group.shutdownGracefully().sync();
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
       // 重连watchdog
        final ConnectionWatchdog watchdog = new ConnectionWatchdog(bootstrap, timer,RemoteIP, port, 12) {
           public ChannelHandler[] handlers() {
               return new ChannelHandler[] {
                		//将自己[ConnectionWatchdog]装载到handler链中，当链路断掉之后，会触发ConnectionWatchdog #channelInActive方法
                        this,
                        //每隔30s的时间触发一次userEventTriggered的方法，并且指定IdleState的状态位是WRITER_IDLE
                        new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS),
                        //实现userEventTriggered方法，并在state是WRITER_IDLE的时候发送一个心跳包到sever端，告诉server端我还活着
                        //idleStateTrigger,
                        new LengthFieldBasedFrameDecoder(2048,0,2,2,4),
                        msgHandler
                };
           }};

        watchdog.setReconnect(true);

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

               future = bootstrap.connect(RemoteIP, port);
           }

            future.sync();
            channel = future.channel();
        } catch (Exception e) 
        {
            e.printStackTrace();
        }
        
        logger.info("{}: 通讯客户端连接到服务器。 IP:{}  Port:{} ",FormattedDate(), this.RemoteServerIP, RemotePort);

		return channel;
   }
    
    public void setConfig()
    {
    	this.RemoteServerIP = new String("192.168.10.176");
    	this.RemotePort = 9999;
    }
	
	public void stop()
	{
		if (channel != null)
			channel.close();
		
		if (group != null)
			group.shutdownGracefully();
		
		logger.info("{}: 通讯客户端关闭!", FormattedDate());
	}
	

	public static class MsgChannelHandlerInitializer extends ChannelInitializer<SocketChannel>
	{
		@Override
		protected void initChannel(final SocketChannel ch) throws Exception
		{
			ch.pipeline().addLast(new IdleStateHandler(0, 10, 0, TimeUnit.SECONDS));
			ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(2048,0,2,2,4));
			ch.pipeline().addLast(new MsgClientHandler());
		}
	}
}
