package com.syscom.banksys.connector.client;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MsgClientHandler extends SimpleChannelInboundHandler 
{
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Channel channel;

	public MsgClientHandler()
	{
		logger.info("通讯客户端处理进程MsgClientHandler初始化成功！");
	}
	
	public void channelActive(ChannelHandlerContext ctx)
	{
		logger.info("Channel has actived!");
		this.channel = ctx.channel();
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception 
	{
		// TODO Auto-generated method stub
		String body = (String) msg;
		logger.info("The input Message is " + body);

	}
	
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	{
		logger.warn("读取交易报文异常退出： " + cause.getMessage());
		ctx.close();
	}

}
