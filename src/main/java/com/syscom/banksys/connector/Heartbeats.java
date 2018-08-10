package com.syscom.banksys.connector;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 
 * VISA�������Ӳ�ѯ��Ϊ4�ֽ�message length headerΪȫ0��û���������ݵı���
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
