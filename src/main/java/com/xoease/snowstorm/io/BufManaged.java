package com.xoease.snowstorm.io;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import org.eclipse.jetty.io.ByteBufferPool;

import com.xoease.snowstorm.config.Snow;

/**
 * @author sks
 * buf管理器
 */
public class BufManaged {
	private ByteBufferPool _bufPool=null;

	protected static int _minBuf =128;
	protected static int _maxBuf = Snow.BUF_CAPACITY ; 
	public BufManaged(ByteBufferPool bufPool) {
		_bufPool = bufPool;
	}
	protected int getMinBufSize(){
		return _minBuf;
	}
	protected int getMaxBufSize(){
		return _maxBuf;
	}
}
