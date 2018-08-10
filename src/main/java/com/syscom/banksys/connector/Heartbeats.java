package com.syscom.banksys.connector;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 
 * VISA空闲连接查询包为4字节message length header为全0，没有其他数据的报文
 *
 */
@SuppressWarnings("deprecation")
public class Heartbeats 
{
    private static final ByteBuf HEARTBEAT_BUF;

    static 
    {
        ByteBuf buf = Unpooled.buffer(4);

        buf.writeByte(0);

        HEARTBEAT_BUF = Unpooled.unmodifiableBuffer(Unpooled.unreleasableBuffer(buf));
    }

    public static ByteBuf heartbeatContent() 
    {
        return HEARTBEAT_BUF.duplicate();
    }
}
