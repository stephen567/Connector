package cn.com.syscom.banksys.connector.handler;


import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.syscom.banksys.connector.mq.ConnectorMQSender;

@ChannelHandler.Sharable
public class MsgInboundHandler extends SimpleChannelInboundHandler<Object> 
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
	private ConnectorMQSender MQSender;

	public MsgInboundHandler()
	{
		logger.info("通讯客户端处理进程MsgClientHandler初始化");
		
		MQSender = new ConnectorMQSender("Producer.xml");
	}

	public MsgInboundHandler(String MQSendXMLConfig)
	{
		logger.info("通讯客户端处理进程MsgClientHandler初始化");
		
		MQSender = new ConnectorMQSender(MQSendXMLConfig);
	}

	public void channelActive(ChannelHandlerContext ctx)
	{
		this.channel = ctx.channel();
		
		ctx.fireChannelActive();
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
		logger.info("通讯客户端读取到报文内容为： {}", message);
		
		//logger.info("通讯客户端读取到报文内容为： {}", bytes.toString());

		//ApplicationContext context = new ClassPathXmlApplicationContext("Producer.xml");
		//RabbitTemplate amqptemplate = context.getBean(RabbitTemplate.class);
		
		//amqptemplate.convertAndSend(message);
		
		//ConnectorMQSender MQSender = new ConnectorMQSender("Producer.xml");
		MQSender.convertAndSend(message);
		//MQSender.convertAndSend(buf);

		//String respMsg = "16010200ac00000000000000000000000000000000000810822000000800020204000000000000000822071751000223f8f2f3f4f0f7f0f0f0f2f2f3670100649f3303204000950580000100009f37049badbcab9f100c0b010a03a0b00000000000009f26080123456789abcdef9f360200ff820200009c01009f1a0208409a030101019f02060000000123005f2a0208409f03060000000000008407a00000000310100580000000020071";
		//ctx.writeAndFlush(respMsg);
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
			String hv = Integer.toHexString(v).toUpperCase();
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
