package com.xoease.snowstorm.xmp;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.xoease.snowstorm.conn.SnowConnection;
import com.xoease.snowstorm.conn.SnowServer;
import com.xoease.snowstorm.server.SnowConnector;
import com.xoease.snowstorm.xmp.exce.XmpParseException;

/**
 * 和协议栈有关的parser类
 * @author sks
 *
 */
public class XmpParser extends DynamicBufParser {
	private final SnowConnection conCtx;
	public XmpParser(SnowConnection connCtx ,long timeout) {
		super(connCtx.getBufferPool(),timeout);
		this.conCtx = connCtx;
		if(SnowServer.USE_SSL){
			addBean(SslParser.getSslParse());
		}

	}
	@Override
	public boolean timeout() {
		 boolean tout = super.timeout();
		 if(tout ){
			 //把超时设置给CONNECTion
			 //SnowTimeoutConnection 会去检测调用事件 这样就把parse的TIMEOUT 和CONNection的链接上了
			 conCtx.setParseTimeout();
		 }
		 return tout;
	}
	/**
	 * 读取的数据到BUf里
	 */
	/*private ByteBuffer buf ;
	private ByteBuffer oldBuf ;
	public ByteBuffer getBuf() {
		return buf;
	}
	/**
	 * 使用之前要设置BUF
	 * @param buf
	 
	public void setBuf(ByteBuffer buf) {
		this.buf = buf;
	}
	
	public ByteBuffer getOldBuf() {
		return oldBuf;
	}
	public void setOldBuf(ByteBuffer oldBuf) {
		this.oldBuf = oldBuf;
	}
	*/
	/**
	 * 判断是否是结束符
	 * @param a
	 * @return
	 */
	/*private boolean isEof(char a){
		return a== 10 || a ==13 ;
	}*/
	/*private String readline(){
		char c ;
		StringBuffer sb = new StringBuffer();
		while(buf.hasRemaining() && !isEof( c = buf.getChar()) ) {
			sb.append(c);
		}
		return sb.toString();
	}*/
	protected static final Logger LOG = Log.getLogger(XmpParser.class);
	
	@Override
	protected void readStart() {
		conCtx.setFirstReadTimeStamp();
		super.readStart();
	}
	
	/* #WaitDataparing or  readed object data  
	 * @see com.xoease.snowstorm.xmp.NextPparse#parse(java.lang.Object, java.util.List)
	 */
	@Override
	public Object parse(Object atta,List<Object> streamObj)  {
		atta = super.parse(atta, streamObj);
		
		int c=0;
		ByteBuffer readbuf = null;
		try {
			 readbuf= getBuf();
			 c = conCtx.getEndPoint().fill(readbuf);
		} catch (IOException e) {
			LOG.info( e.getMessage());
			c=-1 ;
		}

		if(c!=-1){
		
			/*SnowServer server=(SnowServer)((SnowConnector)streamObj.get(0)).getServer();
			if(SnowServer.USE_SSL){
				try {
					SSLEngine ssleng = sslOn("E:\\t.keystore", "123456");
					
					System.out.println(					ssleng.getHandshakeStatus());
					
				} catch (Exception e) {
					e.printStackTrace();
					return null ; 
				}
			}else{
				
			}*/
			byte[] dst = new byte[c];
			readbuf.get(dst, 0, c);
			System.out.println("this is xmp parser  ");

			System.out.println(ByteUtils.toHexString(dst));
				readStart();
				//开始解析读到的数据  当解析完成后要调用readEnd() 代表协议完整
				//ByteUtils.toCharArray(dst)
					
				//TODO 

					
					//if protocal is  ok or  XmpParseException or waitdingdata
						readEnd();
						//	indust();
		
							return "xxxxxxxxxxxxxxxxxxxxxxxx protocal" ;

					//else 
					 //return new WaitDataParing();
					
		}else{return null;}
	}
	/**
	 * 业务层
	 */
	/*@Deprecated
	private void indust() {
		conCtx.getEndPoint().write(new Callback(){

			@Override
			public void succeeded() {
				System.out.println("xxxx");
				Callback.super.succeeded();
			}

			@Override
			public void failed(Throwable x) {
				System.out.println("23232322");
				Callback.super.failed(x);
			}

			
			
		},ByteBuffer.wrap("sd".getBytes()));
	}*/
	
	/**
	 * 这个方法由SnowConnection的onfillable来调用
	 * @param stream
	 * @return
	 * @throws XmpParseException
	 */
	public Object parse(ArrayList<Object> stream) throws XmpParseException{
		//解析流
		Object o=this.parse(null, stream);
		if(o==null){
			//对方关闭
			throw new XmpParseException(1099);
		}else if(o instanceof WaitDataParing){
			//继续等待数据 但不处理业务
			return o;
		}else{
			return conCtx.getLastHandler().parse(o, stream);

		}
	}
	
	/*private byte[] parse(ByteBuffer buf) throws XmpParseException{
	
		String strlen=readline();
		int len =0;
			if(strlen.isEmpty() ){
				_state =XMP_STATE.CLOSE;
				throw new XmpParseException();
			}
		try{
			len=Integer.valueOf(strlen);
			byte[] dst = new byte[len];
			ByteBuffer redbuf ;
			try{
				redbuf = buf.get(dst, 0, len);
				end();
				// 查看剩余
				if(buf.hasRemaining()){
					this.oldBuf = buf.compact().duplicate();
				}
				return dst ;
			}catch(BufferUnderflowException e){
				this.oldBuf = buf.duplicate();
			}
		}finally{
			if(len <=0){
				_state =XMP_STATE.CLOSE;
				throw new XmpParseException();
			}
		}
		return null;
	}
	*/
	
}
