package com.xoease.snowstorm.xmp.handler;

import java.util.List;

import org.eclipse.jetty.server.Request;

import com.xoease.snowstorm.server.SnowAbstractServer;

public class AppConverHandler implements LastHandler {

	@Override
	public Object parse(Object atta, List<Object> streamObj) {
		System.out.println(atta);
		//atta instalce from xmpRequest 
		return null;
	}

	@Override
	public void onStart(SnowAbstractServer server) {
		System.out.println("APp start ....");

	}

}
