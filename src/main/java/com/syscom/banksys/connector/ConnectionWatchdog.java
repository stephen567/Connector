package com.syscom.banksys.connector;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Timer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import static java.util.concurrent.TimeUnit.MILLISECONDS; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public abstract class ConnectionWatchdog extends ChannelInboundHandlerAdapter implements TimerTask
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Bootstrap bootstrap;
	private final Timer timer;
	private final int RemotePort;
	private final String RemoteIP;
	private final int MaxAttempts;
	
	private volatile boolean reconnect = true;
	private int attempts;
	
	public ConnectionWatchdog(Bootstrap bootstrap, Timer timer, String host, int port, int maxAttempts)
	{
		this.bootstrap = bootstrap;
		this.timer = timer;
		this.RemotePort = port;
		this.RemoteIP = host;
		this.MaxAttempts = maxAttempts;
	}
	
	public boolean isReconnect()
	{
		return reconnect;
	}
	
	public void setReconnect(boolean reconnect)
	{
		this.reconnect = reconnect;
	}
	
	/**
	 * 通讯连通后，重置重连次数，并继续后续处理。
	 * @param ctx
	 * @throws Exception
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		Channel channel = ctx.channel();
		attempts = 0;
		
		logger.info("连接到Channel {}", channel);
		
		ctx.fireChannelActive();
	}
	
	/**
	 *  通讯断连后，触发channelInactive()，重连通讯
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception
	{
		boolean doReconnect = this.reconnect;
		if (doReconnect)
		{
			if (attempts < this.MaxAttempts)
			{
				attempts++;
			}
			
			long timeout = 2 << attempts;
			timer.newTimeout(this, timeout, MILLISECONDS);
		}
		
		logger.info("通讯连接已断开，Channel：{}, Host IP: {}, Port:{}, 通讯重连配置：{}", ctx.channel(), RemoteIP, RemotePort, doReconnect);
		
		ctx.fireChannelInactive();
	}
	
	@Override
	public void run(Timeout timeout) throws Exception 
	{
		ChannelFuture future;
		synchronized (bootstrap)
		{
			bootstrap.handler(new ChannelInitializer<Channel>() {
				
				@Override
				protected void initChannel(Channel ch) throws Exception
				{
					ch.pipeline().addLast(handlers());
				}
			});
			
			future = bootstrap.connect(RemoteIP, RemotePort);
		}
		
		future.addListener(new ChannelFutureListener() {
			
			public void operationComplete(ChannelFuture f) throws Exception
			{
				boolean succeed = f.isSuccess();
				
				logger.info("通讯重连  {} 已 {}.", RemoteIP+":"+RemotePort, succeed ? "成功" : "失败");
				
				if(!succeed)
				{
					f.channel().pipeline().fireChannelInactive();
				}
			}
			
		});

	}
	
	public abstract ChannelHandler[] handlers();
}
