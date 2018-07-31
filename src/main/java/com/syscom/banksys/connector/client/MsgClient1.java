package com.syscom.banksys.connector.client;

import com.syscom.banksys.connector.AbstractMsgConnector;
import com.syscom.banksys.connector.client.ClientConfiguration;
import com.syscom.banksys.connector.ReconnectOnCloseListener;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class MsgClient1 extends AbstractMsgConnector<ClientConfiguration, Bootstrap> 
{

    private ReconnectOnCloseListener reconnectOnCloseListener;

    public MsgClient1(SocketAddress socketAddress, ClientConfiguration config ) 
    {
        super(config );
        setSocketAddress(socketAddress);
    }

    public MsgClient1(SocketAddress socketAddress ) 
    {
        this(socketAddress, ClientConfiguration.getDefault() );
    }


    /**
     * Connects synchronously to remote address.
     *
     * @return Returns the {@link ChannelFuture} which will be notified when this
     * channel is closed.
     * @throws InterruptedException if connection process was interrupted
     * @see #setSocketAddress(SocketAddress)
     */
    public ChannelFuture connect() throws InterruptedException 
    {
        final Channel channel = connectAsync().sync().channel();
        assert (channel != null) : "Channel must be set";
        setChannel(channel);
        return channel.closeFuture();
    }

    /**
     * Connect synchronously to  specified host and port.
     *
     * @param host A server host to connect to
     * @param port A server port to connect to
     * @return {@link ChannelFuture} which will be notified when connection is established.
     * @throws InterruptedException if connection process was interrupted
     */
    public ChannelFuture connect(String host, int port) throws InterruptedException 
    {
        return connect(new InetSocketAddress(host, port));
    }

    /**
     * Connects synchronously to specified remote address.
     *
     * @param serverAddress A server address to connect to
     * @return {@link ChannelFuture} which will be notified when connection is established.
     * @throws InterruptedException if connection process was interrupted
     */
    public ChannelFuture connect(SocketAddress serverAddress) throws InterruptedException 
    {
        setSocketAddress(serverAddress);
        return connect().sync();
    }

    /**
     * Connects asynchronously to remote address.
     *
     * @return Returns the {@link ChannelFuture} which will be notified when this
     * channel is active.
     */
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

                .handler(new ChannelInitializer<>(
                        getConfiguration(),
                        getConfigurer(),
                        getWorkerEventLoopGroup(),
                        getIsoMessageFactory(),
                        getMessageHandler()
                ));

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
        if (channel != null) {
            final SocketAddress socketAddress = getSocketAddress();
            logger.info("Closing connection to {}", socketAddress);
            return channel.close();
        } else {
            return null;
        }
    }

    public void disconnect() throws InterruptedException 
    {
        final ChannelFuture disconnectFuture = disconnectAsync();
        if (disconnectFuture != null) {
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
        if (channel == null) {
            throw new IllegalStateException("Channel is not opened");
        }
        if (!channel.isWritable()) {
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
