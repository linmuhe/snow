package com.xoease.snowstorm.xmp;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.util.List;

import org.eclipse.jetty.io.ByteBufferPool;

import com.xoease.snowstorm.config.Snow;

public abstract class DynamicBufParser extends TimeoutParser{
	protected static int _MinBuf =128;
	protected static int _MaxBuf = Snow.BUF_CAPACITY ;
	private ByteBufferPool _Bufpool=null;
	
	public DynamicBufParser(ByteBufferPool bufPool,long timeout) {
		super(timeout);
		_Bufpool=bufPool;
		//_bufManaged = new BufManaged(bufPool);
	}
	protected ByteBuffer getBuf(){
		return _Bufpool.acquire(_MinBuf, false);
	}
	protected void release(ByteBuffer buffer){
		 _Bufpool.release(buffer);
	}
	protected void releaseBuf(){return ;}
	
}
