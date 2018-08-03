package com.xoease.snowstorm.conn;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.util.List;

import com.xoease.snowstorm.config.Snow;
import com.xoease.snowstorm.server.handler.Handler;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.thread.ThreadPool;

import com.xoease.snowstorm.listen.noned.NoneSendMonitor;
import com.xoease.snowstorm.server.ConnectionFactory;
import com.xoease.snowstorm.server.SnowAbstractServer;
import com.xoease.snowstorm.util.ShutdownMonitor;
import com.xoease.snowstorm.xmp.XmpParser;
import com.xoease.snowstorm.xmp.handler.AppConverHandler;
import com.xoease.snowstorm.xmp.handler.DefaultLastHandler;
import com.xoease.snowstorm.xmp.handler.LastHandler;
/**
 * @author sks
 *
 */
public class SnowServer extends SnowAbstractServer {
	public volatile static boolean USE_SSL ;
	///以上 为了业务调用
	
	 /**
	 * true 启用此功能 （连接后发时间不发送数据就调用SnowAbstractconnection里实现的NoneSendData）
	 */
	private boolean _noneSendDataIdle;
	

	public SnowServer(InetSocketAddress addr) {
		super(addr);
       
	}
	public SnowServer(  ) {
		this(Snow.port, true);
	}
	public SnowServer(int port,boolean ssl) {
		super(port);

		USE_SSL = ssl ; 
		//默认开启在连接没有数据时调用的方法
		setNoneSendDataIdle(true);
		//默认开启在关闭的时候调用生命周期的stop
		setStopAtShutdown(true);
	}

	/* 
	 * 返回Connection的工厂类
	 * @see com.xoease.snowstorm.server.SnowConnector.SnowServerConnecFactoryI#createConnectionFactory()
	 */
	@Override
	public ConnectionFactory[] createConnectionFactory() {
		SnowConnectionFactory snowConect = new SnowConnectionFactory();
		ConnectionFactory[] arfa = new ConnectionFactory[1];
		arfa[0] = snowConect;
		//arfa[1] = new SnowConnectionFactory();
		return arfa;
	}
	/* (non-Javadoc)
	 * @see com.xoease.snowstorm.server.SnowAbstractServer#doStart()
	 */
	@Override
	protected void doStart() throws Exception {
		addNoneSendCal();
		//把协议解析后的对想给它  启动和解析后 都会调用第二个参数  
		//如果Hander有beans类就调用子类
		Handler h =  instaceHandler();

		setHandler(h);
		super.doStart();
	}

	/**
	 * 由于jetty 用于构建嵌入式web 所以这个handler不通用with jetty handler
	 * @return
	 */
	protected  Handler instaceHandler(){
		return  new DefaultLastHandler(this,new AppConverHandler());
	}
	private void addNoneSendCal() {
		if(getConnectors().length > 0){
			if(isNoneSendDataIdle()){
				ContainerLifeCycle connector = (ContainerLifeCycle) getConnectors()[0] ; 
				NoneSendMonitor o = new NoneSendMonitor() ; 
				connector.addBean(o) ; 
			}
		}
	}
	@Override
	public ShutdownMonitor getShutdownMonitor() {
		return null  ;
	}

	public boolean isNoneSendDataIdle() {
		return _noneSendDataIdle;
	}

	public void setNoneSendDataIdle(boolean noneSendDataIdle) {
		this._noneSendDataIdle = noneSendDataIdle;
	}

	


	
}
