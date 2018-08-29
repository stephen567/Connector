package cn.com.syscom.banksys.connector.handler;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldPrepender;

import java.nio.ByteOrder;
import java.util.List;

public class CustomLengthFieldPrepender extends LengthFieldPrepender
{
	private final ByteOrder byteOrder;
    private final int lengthFieldLength;
    private final boolean lengthIncludesLengthFieldLength;
    private final int lengthAdjustment;
    
    private final int fillLength;

    public CustomLengthFieldPrepender(int lengthFieldLength) 
    {
        this(lengthFieldLength, false);
    }
    
    public CustomLengthFieldPrepender(int lengthFieldLength, boolean lengthIncludesLengthFieldLength) 
    {
        this(lengthFieldLength, 0, lengthIncludesLengthFieldLength);
    }
    
    public CustomLengthFieldPrepender(int lengthFieldLength, int lengthAdjustment) 
    {
        this(lengthFieldLength, lengthAdjustment, false);
    }
    
    public CustomLengthFieldPrepender(int lengthFieldLength, int lengthAdjustment, boolean lengthIncludesLengthFieldLength) 
    {
        this(ByteOrder.BIG_ENDIAN, lengthFieldLength, lengthAdjustment, lengthIncludesLengthFieldLength);
    }
    
    public CustomLengthFieldPrepender(
            ByteOrder byteOrder, int lengthFieldLength,
            int lengthAdjustment, boolean lengthIncludesLengthFieldLength) 
    {
      this(byteOrder, lengthFieldLength,
            lengthAdjustment, lengthIncludesLengthFieldLength, 0);
    }
    
    public CustomLengthFieldPrepender(ByteOrder byteOrder, 
    		int lengthFieldLength, int lengthAdjustment,
    		boolean lengthIncludesLengthFieldLength, int fillLength)  
    {
        super(byteOrder, lengthFieldLength,
                lengthAdjustment, lengthIncludesLengthFieldLength);
 
        this.byteOrder = byteOrder;
        this.lengthFieldLength = lengthFieldLength;
        this.lengthIncludesLengthFieldLength = lengthIncludesLengthFieldLength;
        this.lengthAdjustment = lengthAdjustment;

        if (fillLength <= 0 || fillLength > 4)
        {
            throw new IllegalArgumentException(
                    "Zeroes Filling Field Length (" + fillLength + ") isn't less than zero "
                    		+ "and great than 4");
        	
        }
        this.fillLength = fillLength;
    }
    
    public CustomLengthFieldPrepender(int lengthFieldLength, boolean lengthIncludesLengthFieldLength, int fillLength) 
    {
        this(ByteOrder.BIG_ENDIAN, lengthFieldLength, 0, lengthIncludesLengthFieldLength, fillLength);
    }

    
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception 
    {
        int length = msg.readableBytes() + lengthAdjustment;
        if (lengthIncludesLengthFieldLength) 
        {
            length += lengthFieldLength;
        }

        if (length < 0) 
        {
            throw new IllegalArgumentException(
                    "Adjusted frame length (" + length + ") is less than zero");
        }

        switch (lengthFieldLength) 
        {
        case 1:
            if (length >= 256) 
            {
                throw new IllegalArgumentException(
                        "length does not fit into a byte: " + length);
            }
            out.add(ctx.alloc().buffer(1).order(byteOrder).writeByte((byte) length));
            break;
        case 2:
            if (length >= 65536) 
            {
                throw new IllegalArgumentException(
                        "length does not fit into a short integer: " + length);
            }
            out.add(ctx.alloc().buffer(2).order(byteOrder).writeShort((short) length));
            break;
        case 3:
            if (length >= 16777216) 
            {
                throw new IllegalArgumentException(
                        "length does not fit into a medium integer: " + length);
            }
            out.add(ctx.alloc().buffer(3).order(byteOrder).writeMedium(length));
            break;
        case 4:
            out.add(ctx.alloc().buffer(4).order(byteOrder).writeInt(length));
            break;
        case 8:
            out.add(ctx.alloc().buffer(8).order(byteOrder).writeLong(length));
            break;
        default:
            throw new Error("should not reach here");
        }
        
        switch (fillLength)
        {
        case 1:
        	out.add(ctx.alloc().buffer(fillLength).order(byteOrder).writeByte(0));
        	break;
        case 2:
        	out.add(ctx.alloc().buffer(2).order(byteOrder).writeShort((short) 0));
        	break;
        case 3:
        	out.add(ctx.alloc().buffer(3).order(byteOrder).writeMedium(0));
        	break;
        case 4:
        	out.add(ctx.alloc().buffer(4).order(byteOrder).writeInt(0));
            break;
        default:
            throw new Error("should not reach here");  
        }
        
        out.add(msg.retain());
    }
}
