package com.syscom.banksys.connector.client;

import com.syscom.banksys.connector.AbstractMsgConnector;
import com.syscom.banksys.connector.client.ClientConfiguration;
import com.syscom.banksys.connector.ReconnectOnCloseListener;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class MsgClient4 extends AbstractMsgConnector<ClientConfiguration, Bootstrap> 
{

    private ReconnectOnCloseListener reconnectOnCloseListener;

    public MsgClient4(SocketAddress socketAddress, ClientConfiguration config ) 
    {
        super(config );
        setSocketAddress(socketAddress);
    }

    public MsgClient4(SocketAddress socketAddress ) 
    {
        this(socketAddress, ClientConfiguration.getDefault() );
    }

    public ChannelFuture connect() throws InterruptedException 
    {
        final Channel channel = connectAsync().sync().channel();
        assert (channel != null) : "Channel must be set";
        setChannel(channel);
        return channel.closeFuture();
    }

    public ChannelFuture connect(String host, int port) throws InterruptedException 
    {
        return connect(new InetSocketAddress(host, port));
    }

    public ChannelFuture connect(SocketAddress serverAddress) throws InterruptedException 
    {
        setSocketAddress(serverAddress);
        return connect().sync();
    }

    public ChannelFuture connectAsync() 
    {
        logger.info("Connecting to {}", getSocketAddress());
        final Bootstrap b = getBootstrap();
        reconnectOnCloseListener.requestReconnect();
        final ChannelFuture connectFuture = b.connect();
        connectFuture.addListener(connFuture -> {
            if (!connectFuture.isSuccess()) {
                reconnectOnCloseListener.scheduleReconnect();
                return;
            }
            Channel channel = connectFuture.channel();
            logger.info("Client is connected to {}", channel.remoteAddress());
            setChannel(channel);
            channel.closeFuture().addListener(reconnectOnCloseListener);
        });

        return connectFuture;
    }

    @Override
    protected Bootstrap createBootstrap() 
    {
        final Bootstrap b = new Bootstrap();
        
        b.group(getBossEventLoopGroup())
                .channel(NioSocketChannel.class)
                .remoteAddress(getSocketAddress())
                .handler(new ChannelInitializer<SocketChannel>(){
    				@Override
    				public void initChannel(SocketChannel ch)
    				throws Exception
    				{
    					ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(2048,0,2,0,4));
    					ch.pipeline().addLast(new MsgClientHandler());
    				}
    			});

        configureBootstrap(b);

        b.validate();

        reconnectOnCloseListener = new ReconnectOnCloseListener(this,
                getConfiguration().getReconnectInterval(),
                getBossEventLoopGroup()
        );

        return b;
    }

    public ChannelFuture disconnectAsync() 
    {
        reconnectOnCloseListener.requestDisconnect();
        final Channel channel = getChannel();
        if (channel != null) 
        {
            final SocketAddress socketAddress = getSocketAddress();
            logger.info("Closing connection to {}", socketAddress);
            return channel.close();
        } else 
        {
            return null;
        }
    }

    public void disconnect() throws InterruptedException 
    {
        final ChannelFuture disconnectFuture = disconnectAsync();
        
        if (disconnectFuture != null) 
        {
            disconnectFuture.await();
        }
    }

    /**
     * Sends asynchronously and returns a {@link ChannelFuture}
     *
     * @param isoMessage A message to send
     * @return ChannelFuture which will be notified when message is sent
     */
    public ChannelFuture sendAsync(byte[] msg) 
    {
        Channel channel = getChannel();
        if (channel == null) 
        {
            throw new IllegalStateException("Channel is not opened");
        }
        if (!channel.isWritable()) 
        {
            throw new IllegalStateException("Channel is not writable");
        }
        
        return channel.writeAndFlush(msg);
    }

    public void send(byte[] msg) throws InterruptedException 
    {
        sendAsync(msg).sync().await();
    }

    public boolean isConnected() 
    {
        Channel channel = getChannel();
        return channel != null && channel.isActive();
    }
}
