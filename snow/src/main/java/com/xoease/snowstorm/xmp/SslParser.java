package com.xoease.snowstorm.xmp;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class SslParser extends NextPparse{
	private volatile static SslParser sslp;
	public static SslParser getSslParse(){
		if(sslp==null){
			sslp = new SslParser();
		}
		return sslp  ; 
	}
	
private  SslParser() {
}
	@Override
	public Object parse(Object atta, List<Object> streamObj) {
		Object r = super.parse(atta, streamObj);

		System.out.println("this is app ssl parser  ");

		return r;
	}

}
