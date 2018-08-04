package com.xoease.snowstorm.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.xoease.snowstorm.config.Snow;
import com.xoease.snowstorm.listen.noned.NoneDataConnListener;
import org.eclipse.jetty.io.ManagedSelector;
import org.eclipse.jetty.io.MappedByteBufferPool;
import org.eclipse.jetty.io.SelectChannelEndPoint;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.annotation.Name;

import com.xoease.snowstorm.io.SnowSelectChannelEndPoint;

/**
 * 
 * @author muhe
 * 
 */
public class SnowConnector extends ServerConnector {
	protected interface SnowServerConnecFactoryI{
		ConnectionFactory[] createConnectionFactory();
		/**
		 * Create a connector that inherits ContainerLifeCycle
		 * @return
		 */
		AbstractNetworkConnector createConnector();
		
	}
	private SnowConnector(@Name("server") SnowAbstractServer server, @Name("acceptors") int acceptors,
			@Name("selectors") int selectors) {
		
		super(server, null, null, new MappedByteBufferPool(), acceptors, selectors,server.createConnectionFactory());

	
	}
	@Override
	protected SelectChannelEndPoint newEndPoint(SocketChannel channel, ManagedSelector selectSet, SelectionKey key) throws IOException
    {
        return new SnowSelectChannelEndPoint(channel, selectSet, key, getScheduler(), getIdleTimeout());
    }
	public SnowConnector(@Name("server") SnowAbstractServer server) {
		this(server, -1, -1);
		if(StringUtil.isNotBlank(Snow.host)){
			this.setHost(Snow.host);
		}
	}
}
