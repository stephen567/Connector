package com.syscom.banksys.connector;

import io.netty.channel.ChannelHandlerContext;

public interface MessageListener
{
	boolean applies(String msg);
	
	boolean onMessage(ChannelHandlerContext ctx, String msg);

}
