package com.xoease.snowstorm.config;

public class Snow {
	public static final String VER="1.1";
	public static final String PROTOCAL="XMP";
	/**
	 * AbstractConnectionFactory _inputbufferSize
	 */
	public static int BUF_CAPACITY=2048000;//4 * 1024 * 1000byte 4M
	public static long TIMEOUT_PARSE=30000;//解析的时候的超市时间
	public static long TIMEOUT_NODATA=TIMEOUT_PARSE;//连上后没有发生过数据的超市时间 
}
