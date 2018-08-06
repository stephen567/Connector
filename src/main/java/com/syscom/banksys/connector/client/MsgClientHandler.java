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
		logger.info("ͨѶ�ͻ��˴������MsgClientHandler��ʼ���ɹ���");
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
		logger.warn("��ȡ���ױ����쳣�˳��� " + cause.getMessage());
		ctx.close();
	}

}
