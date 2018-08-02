package com.xoease.snowstorm.server;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jetty.io.AbstractConnection;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ArrayUtil;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import com.xoease.snowstorm.config.Snow;

public abstract class AbstractConnectionFactory extends ContainerLifeCycle implements ConnectionFactory
{
    private final String _protocol;
    private final List<String> _protocols;
    private int _inputbufferSize = Snow.BUF_CAPACITY;

    protected AbstractConnectionFactory(String protocol)
    {
        _protocol=protocol;
        _protocols=Collections.unmodifiableList(Arrays.asList(new String[]{protocol}));
    }
    
    protected AbstractConnectionFactory(String... protocols)
    {
        _protocol=protocols[0];
        _protocols=Collections.unmodifiableList(Arrays.asList(protocols));
    }

    @Override
    public String getProtocol()
    {
        return _protocol;
    }

    @Override
    public List<String> getProtocols()
    {
        return _protocols;
    }

    public int getInputBufferSize()
    {
        return _inputbufferSize;
    }

    public void setInputBufferSize(int size)
    {
        _inputbufferSize=size;
    }

    protected AbstractConnection configure(AbstractConnection connection, Connector connector, EndPoint endPoint)
    {
        connection.setInputBufferSize(getInputBufferSize());

        // Add Connection.Listeners from Connector
        if (connector instanceof ContainerLifeCycle)
        {
            ContainerLifeCycle aggregate = (ContainerLifeCycle)connector;
            for (Connection.Listener listener : aggregate.getBeans(Connection.Listener.class))
                connection.addListener(listener);
        }
        // Add Connection.Listeners from this factory
        for (Connection.Listener listener : getBeans(Connection.Listener.class))
            connection.addListener(listener);
        
        return connection;
    }

    @Override
    public String toString()
    {
        return String.format("%s@%x%s",this.getClass().getSimpleName(),hashCode(),getProtocols());
    }

    public static ConnectionFactory[] getFactories(SslContextFactory sslContextFactory, ConnectionFactory... factories)
    {
        factories=ArrayUtil.removeNulls(factories);

        if (sslContextFactory==null)
            return factories;

        for (ConnectionFactory factory : factories)
        {
            if (factory instanceof HttpConfiguration.ConnectionFactory)
            {
                HttpConfiguration config = ((HttpConfiguration.ConnectionFactory)factory).getHttpConfiguration();
                if (config.getCustomizer(SecureRequestCustomizer.class)==null)
                    config.addCustomizer(new SecureRequestCustomizer());
            }
        }
        return (ConnectionFactory[]) ArrayUtil.prependToArray(new SslConnectionFactory(sslContextFactory,factories[0].getProtocol()),factories,ConnectionFactory.class);

    }
}
