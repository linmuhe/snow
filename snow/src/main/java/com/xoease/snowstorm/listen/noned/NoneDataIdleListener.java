package com.xoease.snowstorm.listen.noned;

import org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener;
import org.eclipse.jetty.util.component.LifeCycle;

import com.xoease.snowstorm.server.SnowConnector;

/**
 * @see NoneSendMonitor
 * 用于在connector启动的时候 启动线程监听器 扫描在一定时间内没有发生过数据的connection
 * @author muhe
 *
 */

@Deprecated
public class NoneDataIdleListener extends AbstractLifeCycleListener{
	private SnowConnector transfer(LifeCycle event){
		if(event instanceof SnowConnector){
			return (SnowConnector)event;
		}
		return null ; 
	}
	@Override
	public void lifeCycleStarting(LifeCycle event) {
           //    System.out.println("lifeCycleStarting");
               NoneSendMonitor o = new NoneSendMonitor() ; 
               transfer(event).addBean(o ,true) ; 
	}

	@Override
	public void lifeCycleStarted(LifeCycle event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void lifeCycleFailure(LifeCycle event, Throwable cause) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void lifeCycleStopping(LifeCycle event) {
		
	}

	@Override
	public void lifeCycleStopped(LifeCycle event) {
		
	}

}
