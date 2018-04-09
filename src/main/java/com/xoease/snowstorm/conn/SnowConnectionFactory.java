package com.xoease.snowstorm.conn;

import org.eclipse.jetty.io.AbstractConnection;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;

import com.xoease.snowstorm.config.Snow;
import com.xoease.snowstorm.listen.noned.NoneDataConnListener;
import com.xoease.snowstorm.server.AbstractConnectionFactory;
import com.xoease.snowstorm.server.Connector;

/**
 * factory 会在connector里调用创建 然后转到snowserver的方法里创建
 * @author linkedfun1
 *
 */
public class SnowConnectionFactory extends AbstractConnectionFactory{
	/**
	 * use in connection 
	 * @author linkedfun1
	 *
	 */
	public interface NoneSendData {
		/**
		 * 连接了成功后 但是在一段时间内从没有发送过数据 就调用这个方法
		 */
		void onNoneTimeout();
		/**
		 * =true 代表没有交换过数据
		 *  才会执行方法onConnectedIdle
		 * 
		 */
		boolean IsHappened();
		boolean timeout();
	}
	public SnowConnectionFactory() {
		super(Snow.PROTOCAL);
	}

	@Override
	public Connection newConnection(Connector connector, EndPoint endPoint) {
	/*	System.out.println("xxx"+endPoint.getClass().getName());
if(		endPoint.getClass().isAssignableFrom(NetworkTrafficSelectChannelEndPoint.class)
){
	
}*/
		/*if(connector instanceof SnowConnector){
			NoneSendMonitor o = new NoneSendMonitor() ;
			SnowConnector conna = (SnowConnector)connector;
			conna.addBean(o ,true) ;  
		}*/
		AbstractConnection connection = this.configure( connector, endPoint);
		return connection ;
	}

	protected AbstractConnection configure(Connector connector, EndPoint endPoint) {
		SnowConnection connection=new SnowConnection( connector,  endPoint);
		//org.eclipse.jetty.util.component.LifeCycle.Listener listener = null ;
		//connector.addLifeCycleListener(listener);
		/*Connection.Listener ll =new Connection.Listener(){

			@Override
			public void onOpened(Connection connection) {
				System.out.println("l;isten on open ");
			}

			@Override
			public void onClosed(Connection connection) {
				System.out.println("l;isten on close  ");
			}
			
		};
		connection.addListener(ll );*/
		NoneDataConnListener  connListener = new NoneDataConnListener(connector);
		addBean(connListener);
		//connection.addListener(connListener);
		//设置缓冲区的大小
		//会把Facotry 和 connector 里实现了 Connection.Listener 的bean  加到connection的Listener
		return super.configure(connection, connector, endPoint);
	}

	
	
}
