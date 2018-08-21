package cn.com.syscom.banksys.connector.client;


import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import cn.com.syscom.banksys.connector.handler.Heartbeats;
import cn.com.syscom.banksys.connector.mq.ConnectorMQSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.text.SimpleDateFormat;

@ChannelHandler.Sharable
public class MsgClientHandler extends SimpleChannelInboundHandler 
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	/*
	private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private String FormattedDate()
	{
		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		
		return df.format(System.currentTimeMillis());
	}
	*/
	
	private Channel channel;

	public MsgClientHandler()
	{
		logger.debug("通讯客户端处理进程MsgClientHandler初始化");
	}
	
	public void channelActive(ChannelHandlerContext ctx)
	{
		this.channel = ctx.channel();
		
		ctx.fireChannelInactive();
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception 
	{
		// TODO Auto-generated method stub
		logger.debug("开始读取报文");
		//int len = ((ByteBuf)msg).readableBytes();
		
		ByteBuf buf = (ByteBuf)msg;
		//int len = buf.readableBytes();
		
		byte[] bytes = new byte[buf.readableBytes()];
		buf.readBytes(bytes);
		
		String message = bytestoHexString(bytes);
		logger.debug("通讯客户端读取到报文内容为： {}", message);

		//ApplicationContext context = new ClassPathXmlApplicationContext("Producer.xml");
		//RabbitTemplate amqptemplate = context.getBean(RabbitTemplate.class);
		
		//amqptemplate.convertAndSend(message);
		ConnectorMQSender MQSender = new ConnectorMQSender("Producer.xml");
		MQSender.convertAndSend(message);
	}

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception 
    {
        if (evt instanceof IdleStateEvent) 
        {
        	IdleState state = ((IdleStateEvent) evt).state();
        	
            if (state == IdleState.WRITER_IDLE) 
            {
            	logger.info("发送写空闲心跳包！");
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
		logger.error("读取交易报文异常退出：{} ", cause.getMessage());
		ctx.close();
	}
	
	

}
