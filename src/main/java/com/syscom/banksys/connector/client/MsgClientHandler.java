package com.syscom.banksys.connector.client;


import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

import com.syscom.banksys.connector.Heartbeats;

@ChannelHandler.Sharable
public class MsgClientHandler extends SimpleChannelInboundHandler 
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	//private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private String FormattedDate()
	{
		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		
		return df.format(System.currentTimeMillis());
	}
	
	private Channel channel;

	public MsgClientHandler()
	{
		logger.info("{}: ͨѶ�ͻ��˴������MsgClientHandler��ʼ��", FormattedDate());
	}
	
	public void channelActive(ChannelHandlerContext ctx)
	{
		logger.info("{}: ͨѶ�ͻ��˿�ʼ���ӷ�����......", FormattedDate());
		this.channel = ctx.channel();
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception 
	{
		// TODO Auto-generated method stub
		logger.info("{}: ��ʼ��ȡ����", FormattedDate());
		ByteBuf buf = (ByteBuf)msg;
		byte[] bytes = new byte[buf.readableBytes()];
		buf.readBytes(bytes);
		
		String message = bytestoHexString(bytes);
		logger.info("{}: ͨѶ�ͻ��˶�ȡ����������Ϊ�� {}", FormattedDate(), message);
	}

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception 
    {
        if (evt instanceof IdleStateEvent) 
        {
        	IdleState state = ((IdleStateEvent) evt).state();
        	
            if (state == IdleState.WRITER_IDLE) 
            {
            	logger.info("{}: ����д������������", FormattedDate());
                ctx.writeAndFlush(Heartbeats.heartbeatContent());
            }
        } else 
        {
            super.userEventTriggered(ctx, evt);
        }
    }

	public String bytestoHexString(byte[] src)
	{
		StringBuilder stringBuilder = new StringBuilder();
		if (src == null || src.length <= 0)
		{
			return null;
		}
		
		for(int i = 0; i < src.length; i++)
		{
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2)
			{
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		
		return stringBuilder.toString();
	}
	
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	{
		logger.error("{}: ��ȡ���ױ����쳣�˳���{} ", FormattedDate(), cause.getMessage());
		ctx.close();
	}

}
