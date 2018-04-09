package com.xoease.snowstorm.xmp;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.util.List;

import org.eclipse.jetty.io.ByteBufferPool;

import com.xoease.snowstorm.config.Snow;
import com.xoease.snowstorm.io.BufManaged;

public abstract class DynamicBufParser extends TimeoutParser{
	//private BufManaged _bufManaged=null;
	private ByteBufferPool _Bufpool=null;
	
	public DynamicBufParser(ByteBufferPool bufPool,long timeout) {
		super(timeout);
		_Bufpool=bufPool;
		//_bufManaged = new BufManaged(bufPool);
	}
	protected ByteBuffer getBuf(){
		return _Bufpool
				.acquire(128, false);
	}
	protected void releaseBuf(){return ;}
	
}
