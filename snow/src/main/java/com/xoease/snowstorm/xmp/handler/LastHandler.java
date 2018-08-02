package com.xoease.snowstorm.xmp.handler;

import com.xoease.snowstorm.server.SnowAbstractServer;
import com.xoease.snowstorm.xmp.Pparser;

public interface   LastHandler extends Pparser{
	 /**
	 * 服务启动的时候执行 
	 * 
	 */
	void onStart(SnowAbstractServer server);
	/**
	 * 获取Server 
	 * @return
	 */
	
}
