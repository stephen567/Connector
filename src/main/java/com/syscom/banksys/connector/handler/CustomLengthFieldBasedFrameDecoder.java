package com.syscom.banksys.connector.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.ByteOrder;

public class CustomLengthFieldBasedFrameDecoder extends LengthFieldBasedFrameDecoder 
{
    private final int lengthFieldLength;
    private final int initialBytesToStrip;
    
    private int outLength = 0;

    public CustomLengthFieldBasedFrameDecoder(
            int maxFrameLength,
            int lengthFieldOffset, int lengthFieldLength) 
    {
        this(maxFrameLength, lengthFieldOffset, lengthFieldLength, 0, 0);
    }

	public CustomLengthFieldBasedFrameDecoder(
			int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
			int lengthAdjustment, int initialBytesToStrip)
	{
		this(ByteOrder.BIG_ENDIAN, maxFrameLength, lengthFieldOffset, lengthFieldLength, 
				lengthAdjustment, initialBytesToStrip, true);
	}

    public CustomLengthFieldBasedFrameDecoder(
            int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
            int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
        this(
                ByteOrder.BIG_ENDIAN, maxFrameLength, lengthFieldOffset, lengthFieldLength,
                lengthAdjustment, initialBytesToStrip, failFast);
    }

	public CustomLengthFieldBasedFrameDecoder(
			ByteOrder byteOrder, int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
			int lengthAdjustment, int initialBytesToStrip, boolean failFast)
	{
		super(byteOrder, maxFrameLength, lengthFieldOffset, lengthFieldLength,
				lengthAdjustment, initialBytesToStrip, failFast);

        this.lengthFieldLength = lengthFieldLength;
        this.initialBytesToStrip = initialBytesToStrip;
        
        this.outLength = 0;
	}

	public CustomLengthFieldBasedFrameDecoder(
			int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
			int lengthAdjustment, int initialBytesToStrip, int outLength)
	{
		this(maxFrameLength, lengthFieldOffset, lengthFieldLength,
				lengthAdjustment, initialBytesToStrip);
		
		if (( outLength > 0 ) && (outLength < this.lengthFieldLength))
		{
            throw new IllegalArgumentException(
                    "outLength (" + outLength +" )" +
                       "must be equal to or greater than " +
                       "lengthFieldLength (" + lengthFieldLength +  
                       ") when outLength greater than 0.");
		}

		this.outLength = outLength;
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception
	{
		if ( outLength <= 0)
		{
			return super.decode(ctx, in);
		}
		
		int inLength = in.readableBytes();
		int length = inLength - this.initialBytesToStrip;
		
		ByteBuf frame = ctx.alloc().buffer(length + this.outLength);
		
	    switch (this.outLength) 
	    {
	    case 1:
	    	if (length >= 256) {
                throw new IllegalArgumentException(
                        "length does not fit into a byte: " + length);
            }
            frame.writeByte((byte) length);
            break;
	    case 2:
	    	if (length >= 65536) {
                throw new IllegalArgumentException(
                        "length does not fit into a short integer: " + length);
            }
            frame.writeShort((short) length);
            break;
	    case 3:
	    	if (length >= 16777216) {
                throw new IllegalArgumentException(
                        "length does not fit into a medium integer: " + length);
            }
            frame.writeMedium(length);
            break;
	    case 4:
	    	frame.writeInt(length);
	        break;
	    case 8:
	        frame.writeLong(length);
	        break;
	    default:
	        throw new Error("should not reach here");
	        }
	    
	    frame.writeBytes((ByteBuf)super.decode(ctx,  in));
	    
	    return frame;
	}
}
