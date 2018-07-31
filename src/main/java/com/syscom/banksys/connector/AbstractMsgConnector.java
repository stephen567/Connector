package com.syscom.banksys.connector;

import com.syscom.banksys.connector.ConnectorConfiguration;
import com.syscom.banksys.connector.ConnectorConfigurer;
import com.syscom.banksys.connector.MessageListener;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractMsgConnector<
C extends ConnectorConfiguration,
B extends AbstractBootstrap>
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    // @VisibleForTest
//    final CompositeIsoMessageHandler<M> messageHandler;
//    private final MessageFactory<M> isoMessageFactory;
    private final AtomicReference<Channel> channelRef = new AtomicReference<>();
    private final C configuration;
    private ConnectorConfigurer<C, B> configurer;
    private SocketAddress socketAddress;
    private EventLoopGroup bossEventLoopGroup;
    private EventLoopGroup workerEventLoopGroup;
    private B bootstrap;

    protected AbstractMsgConnector(C configuration) 
    {
        assert (configuration != null) : "Configuration must be provided";
        this.configuration = configuration;
    }

    /**
     * Making connector ready to create a connection / bind to port.
     * Creates a Bootstrap
     *
     * @see AbstractBootstrap
     */
    public void init() 
    {
        logger.info("Initializing");
        bossEventLoopGroup = createBossEventLoopGroup();
        workerEventLoopGroup = createWorkerEventLoopGroup();
        bootstrap = createBootstrap();
    }

    public void shutdown() 
    {
        if (workerEventLoopGroup != null) 
        {
            workerEventLoopGroup.shutdownGracefully();
            workerEventLoopGroup = null;
        }
        if (bossEventLoopGroup != null) 
        {
            bossEventLoopGroup.shutdownGracefully();
            bossEventLoopGroup = null;
        }
    }

    protected void configureBootstrap(B bootstrap) 
    {
        bootstrap.option(ChannelOption.TCP_NODELAY,
                Boolean.parseBoolean(System.getProperty(
                        "nfs.rpc.tcp.nodelay", "true")))
                .option(ChannelOption.AUTO_READ, true);

        if (configurer != null) 
        {
            configurer.configureBootstrap(bootstrap, configuration);
        }
    }

    protected ConnectorConfigurer<C, B> getConfigurer() 
    {
        return configurer;
    }

    public void setConfigurer(ConnectorConfigurer<C, B> connectorConfigurer) 
    {
        this.configurer = connectorConfigurer;
    }

    public SocketAddress getSocketAddress() 
    {
        return socketAddress;
    }

    public void setSocketAddress(SocketAddress socketAddress) 
    {
        this.socketAddress = socketAddress;
    }

    protected abstract B createBootstrap();

    protected B getBootstrap() 
    {
        return bootstrap;
    }

    protected EventLoopGroup createBossEventLoopGroup() 
    {
        return new NioEventLoopGroup();
    }

    protected EventLoopGroup getBossEventLoopGroup() 
    {
        return bossEventLoopGroup;
    }

    protected EventLoopGroup createWorkerEventLoopGroup() 
    {
        return new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
    }

    protected EventLoopGroup getWorkerEventLoopGroup() 
    {
        return workerEventLoopGroup;
    }

    protected Channel getChannel() 
    {
        return channelRef.get();
    }

    protected void setChannel(Channel channel) 
    {
        this.channelRef.set(channel);
    }

    public C getConfiguration() 
    {
        return configuration;
    }

}
