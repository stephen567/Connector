package cn.com.syscom.banksys.connector;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.syscom.banksys.connector.ConnectorConfiguration;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractMsgConnector<
C extends ConnectorConfiguration, 
B extends AbstractBootstrap>
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicReference<Channel> channelRef = new AtomicReference<>();
    private final C configuration;
    private SocketAddress socketAddress;
    private EventLoopGroup bossEventLoopGroup;
    private EventLoopGroup workerEventLoopGroup;
    private B bootstrap;

    protected AbstractMsgConnector(C configuration) 
    {
        assert (configuration != null) : "Configuration must be provided";
        this.configuration = configuration;
    }

    public void init() 
    {
        logger.info("通讯端初始化！");
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
