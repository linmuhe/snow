package com.xoease.snowstorm.listen.noned;

import org.eclipse.jetty.io.Connection;

import com.xoease.snowstorm.server.SnowAbstractConnection;

public interface InterNoneSendMonitor {

	void add(SnowAbstractConnection con);

}
