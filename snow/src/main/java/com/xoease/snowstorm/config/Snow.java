package com.xoease.snowstorm.config;

import com.xoease.snowstorm.util.PropertyUtil;

public class Snow {
	public static final String VER="1.1";
	public static final String PROTOCAL="XDMP";
	/**
	 * AbstractConnectionFactory _inputbufferSize
	 */
	public static int BUF_CAPACITY=2048000;//4 * 1024 * 1000byte 4M //数据包上线
	public static long TIMEOUT_PARSE=30000;//解析的时候的超市时间
	public static long TIMEOUT_NODATA=TIMEOUT_PARSE;//连上后没有发生过数据的超市时间
	public static String PublicKey ="";
	public static String PrivateKey ="";
	public static Integer port =8888;
	public static String host ="";
}
