package com.xoease.snowstorm.io;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.eclipse.jetty.io.ManagedSelector;
import org.eclipse.jetty.io.SelectChannelEndPoint;
import org.eclipse.jetty.util.thread.Scheduler;

public class SnowSelectChannelEndPoint extends SelectChannelEndPoint {

	public SnowSelectChannelEndPoint(SocketChannel channel, ManagedSelector selector, SelectionKey key,
			Scheduler scheduler, long idleTimeout) {
		super(channel, selector, key, scheduler, idleTimeout);
	}
	@Override
	public void needsFillInterest() {
		super.needsFillInterest();
	}
}
