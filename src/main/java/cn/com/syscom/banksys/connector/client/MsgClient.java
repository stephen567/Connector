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
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
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

import cn.com.syscom.banksys.connector.handler.ConnectionWatchdog;
import cn.com.syscom.banksys.connector.handler.CustomLengthFieldBasedFrameDecoder;

public class MsgClient 
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
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
	private boolean reConnect = true;
	private int MaxReconnectTimes = 12;
	
    
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
        final ConnectionWatchdog watchdog = new ConnectionWatchdog(bootstrap, timer,this.RemoteServerIP, this.RemotePort, this.MaxReconnectTimes) {
           public ChannelHandler[] handlers() {
               return new ChannelHandler[] {
                		//���Լ�[ConnectionWatchdog]װ�ص�handler���У�����·�ϵ�֮�󣬻ᴥ��ConnectionWatchdog #channelInActive����
                        this,
                        //ÿ��30s��ʱ�䴥��һ��userEventTriggered�ķ���������ָ��IdleState��״̬λ��WRITER_IDLE
                        new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS),
                        //ʵ��userEventTriggered����������state��WRITER_IDLE��ʱ����һ����������sever�ˣ�����server���һ�����
                        //idleStateTrigger,
                        new CustomLengthFieldBasedFrameDecoder(2048,0,2,2,4,2),
                        msgHandler
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
		
		logger.info("ͨѶ�ͻ��˹ر�!");
	}
}
