package com.syscom.banksys.connector;


import com.syscom.banksys.connector.ConnectorConfiguration;
import com.syscom.banksys.connector.ConnectorConfigurer;

import io.netty.bootstrap.AbstractBootstrap;

/**
 * Adapter class for ConnectorConfigurer. Don't use it any more.
 *
 * @param <C> ConnectorConfiguration
 * @param <B> AbstractBootstrap
 * @deprecated Use {@link com.github.kpavlov.jreactive8583.ConnectorConfigurer} instead
 */
@Deprecated
public class ConnectorConfigurerAdapter<C extends ConnectorConfiguration, B extends AbstractBootstrap> implements ConnectorConfigurer<C, B> 
{
}
