package cn.com.syscom.banksys.connector.handler;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartBeatHandler extends ChannelInboundHandlerAdapter 
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception 
    {
        ByteBuf HeartBeatbuf = Unpooled.buffer(4);
        HeartBeatbuf.writeInt(0);

    	if (evt instanceof IdleStateEvent) 
        {
        	IdleState state = ((IdleStateEvent) evt).state();
        	
            if (state == IdleState.WRITER_IDLE) 
            {
            	logger.info("·¢ËÍÐ´¿ÕÏÐÐÄÌø°ü£¡");
                ctx.writeAndFlush(HeartBeatbuf);
            }
        } else 
        {
            super.userEventTriggered(ctx, evt);
        }
    }
}
