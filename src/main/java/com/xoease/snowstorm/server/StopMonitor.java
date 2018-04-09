package com.xoease.snowstorm.server;

/**
 * set jetty server properties  
 * 这是jetty的默认功能 启动一个服务  接受停止命令
 * @author linkedfun1
 *
 */
public class StopMonitor {
	/**
	 * 输出这个服务的调试信息 并不是server的调试信息
	 * @param b
	 */
	public static  void setDebug(boolean b){
		if(b)
		System.setProperty("DEBUG", "1");
	}
	public static  void setHost(String host ,int port , String passkey){
		System.setProperty("STOP.HOST", host);
		System.setProperty("STOP.PORT", port+"");
		System.setProperty("STOP.KEY", passkey);
	}
}
