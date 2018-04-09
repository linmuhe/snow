package com.xoease.snowstorm.listen.noned;

import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.xoease.snowstorm.server.Connector;
import com.xoease.snowstorm.server.SnowAbstractConnection;
import com.xoease.snowstorm.server.SnowAbstractServer;

/**
 * 针对每个新的连接的listener
 * @author sks
 *
 */
public class NoneDataConnListener implements Connection.Listener {
	private Connector _connector ; 
	private static final Logger LOG = Log.getLogger(NoneDataConnListener.class);

	public NoneDataConnListener(Connector connector) {
		this._connector=connector ; 
	}
	@Override
	public void onOpened(Connection connection) {
		if(_connector instanceof ContainerLifeCycle && connection instanceof SnowAbstractConnection){
			ContainerLifeCycle con = (ContainerLifeCycle)_connector ;
			InterNoneSendMonitor monitor = con.getBean(InterNoneSendMonitor.class) ;  
			monitor.add((SnowAbstractConnection)connection);
			LOG.debug(connection + " - added to noneDataIdleSets");
		}
	}
	@Override
	public void onClosed(Connection connection) {}
}
