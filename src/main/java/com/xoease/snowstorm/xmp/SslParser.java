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
		return super.parse(atta, streamObj);
	}
private SSLEngine sslOn(String keystorePath,String pass) throws Exception{
		
		KeyStore ks = KeyStore.getInstance("JKS");      // 创建JKS密钥库  
        ks.load(new FileInputStream(keystorePath), pass.toCharArray());  

        // 创建管理JKS密钥库的X.509密钥管理器  
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");  
        kmf.init(ks, pass.toCharArray());  
          
        // 构造SSL环境，指定SSL版本为3.0，也可以使用TLSv1，但是SSLv3更加常用。  
        SSLContext s = SSLContext.getInstance("SSLv3");  
          
          
        s.init(kmf.getKeyManagers(), null, null); 
	
		// 打印这个SSLContext实例使用的协议  
		//System.out.println("缺省安全套接字使用的协议: " + s.getProtocol());  
		// 获取SSLContext实例相关的SSLEngine  
		SSLEngine e = s.createSSLEngine(); 
		
		e.setUseClientMode(false);//
        e.setNeedClientAuth(true);//
        
		return e ; 
	}
}
