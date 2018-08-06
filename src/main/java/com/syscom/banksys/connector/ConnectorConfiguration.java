package com.syscom.banksys.connector;

public abstract class ConnectorConfiguration 
{
    public static final int DEFAULT_IDLE_TIMEOUT_SECONDS = 30;
    public static final int DEFAULT_MAX_FRAME_LENGTH = 8192;
    
    
    private int maxFrameLength = DEFAULT_MAX_FRAME_LENGTH;
    private int idleTimeout = DEFAULT_IDLE_TIMEOUT_SECONDS;
    private boolean replyOnError = false;
    private boolean addLoggingHandler = true;
    
    private String Host;
    private int Port;
    
    private String RqstQueueName;
    private String RespQueueName;
    private String MQUser;
    private String MQPasswd;
    private String MQPort;


    protected ConnectorConfiguration(Builder builder) 
    {
        addLoggingHandler = builder.addLoggingHandler;
        idleTimeout = builder.idleTimeout;
        maxFrameLength = builder.maxFrameLength;
        replyOnError = builder.replyOnError;
    }

 
    /**
     * Channel read/write idle timeout in seconds.
     * <p>
     * If no message was received/sent during specified time interval then `Echo` message will be sent.</p>
     *
     * @return timeout in seconds
     */
    public int getIdleTimeout() 
    {
        return idleTimeout;
    }

    /**
     * Set Idle Timeout in seconds
     *
     * @param idleTimeoutSeconds Idle timeout in seconds
     * @deprecated Use {@link Builder}
     */
    @Deprecated
    public void setIdleTimeout(int idleTimeoutSeconds) 
    {
        this.idleTimeout = idleTimeoutSeconds;
    }

    public int getMaxFrameLength() 
    {
        return maxFrameLength;
    }

    /**
     * @param maxFrameLength the maximum length of the frame.
     * @deprecated Use {@link Builder}
     */
    @Deprecated
    public void setMaxFrameLength(int maxFrameLength) 
    {
        this.maxFrameLength = maxFrameLength;
    }

    /**
     * @deprecated Use {@link Builder}
     * @param addLoggingHandler should logging handler be added to pipeline
     */
    @Deprecated
    public void setAddLoggingHandler(boolean addLoggingHandler) 
    {
        this.addLoggingHandler = addLoggingHandler;
    }

    /**
     * Returns true is {@link IsoMessageLoggingHandler}
     * <p>Allows to disable adding default logging handler to {@link io.netty.channel.ChannelPipeline}.</p>
     *
     * @return true if {@link IsoMessageLoggingHandler} should be added.
     */
    public boolean addLoggingHandler() 
    {
        return addLoggingHandler;
    }

    /**
     * Whether to reply with administrative message in case of message syntax errors. Default value is <code>false.</code>
     *
     * @return true if reply message should be sent in case of error parsing the message.
     */
    public boolean replyOnError() 
    {
        return replyOnError;
    }

    /**
     * @param replyOnError should reply on error
     * @deprecated Use {@link Builder}
     */
    @Deprecated
    public void setReplyOnError(boolean replyOnError) 
    {
        this.replyOnError = replyOnError;
    }

    @SuppressWarnings({"unchecked", "unused"})
    protected abstract static class Builder<B extends Builder> 
    {
        private int maxFrameLength = DEFAULT_MAX_FRAME_LENGTH;
        private int idleTimeout = DEFAULT_IDLE_TIMEOUT_SECONDS;
        private boolean addLoggingHandler = true;
        private boolean replyOnError = false;

        public B withMaxFrameLength(int maxFrameLength) 
        {
            this.maxFrameLength = maxFrameLength;
            return (B) this;
        }

        public B withIdleTimeout(int idleTimeout) 
        {
            this.idleTimeout = idleTimeout;
            return (B) this;
        }

        public B withReplyOnError(boolean replyOnError) 
        {
            this.replyOnError = replyOnError;
            return (B) this;
        }

        public B withAddLoggingHandler(boolean addLoggingHandler) 
        {
            this.addLoggingHandler = addLoggingHandler;
            return (B) this;
        }
    }
}
