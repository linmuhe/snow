package com.xoease.snowstorm.conn;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;

import com.xoease.snowstorm.server.Connector;
import com.xoease.snowstorm.server.SnowAbstractConnection;
import com.xoease.snowstorm.server.handler.Handler;
import com.xoease.snowstorm.xmp.Pparser;
import com.xoease.snowstorm.xmp.XmpParser;
import com.xoease.snowstorm.xmp.exce.XmpParseException;
import com.xoease.snowstorm.xmp.handler.DefaultLastHandler;
import com.xoease.snowstorm.xmp.handler.LastHandler;


/**
 * @author sks
 * content for parser 
 * 这个类就和协议栈有关了
 */
public class SnowConnection extends SnowAbstractConnection   {
	private XmpParser parser;
	/*private XmpGenetor genetor;
	 */

	public boolean isHanded = false ;//握手成功
	public  String aesPassword;
	public boolean isHanded() {
		return isHanded;
	}
	public void setHanded(boolean handed) {
		isHanded = handed;
	}

	protected SnowConnection(Connector connector, EndPoint endPoint) {
		super(connector, endPoint);
	}



	/* 有一个schedule不断的去调用它 
	 * @see com.xoease.snowstorm.server.SnowTimeoutConnection#parseTimeout()
	 */
	@Override
	public boolean parseTimeout(){
		return parser.timeout() ; 
	//	return super.parseTimeout();
	}
	public LastHandler getLastHandler(){
		Handler handl = _connector.getServer().getHandler() ;
		if(handl instanceof LastHandler){
			return (LastHandler)handl;
		}
		return null;
	}
	{
	//	System.out.println("======ne wparse");
		//解析超时
		long parser_timeout = get_idleTimeout() ;
		parser=new XmpParser(this,parser_timeout);
	}
	/*
	 * 
	 * @Override
	public void onClose() {
		System.out.println("on cloded");
	}
	@Override
	public void onParseTimeout() {
		super.onParseTimeout();
		this.close();
		System.out.println(Thread.currentThread().getName() +"--time out ...........................");
	}*/
	@Override
	public void onFillable() {
		

		ArrayList<Object> stream = new ArrayList<Object>();
		stream.add(_connector);
		stream.add(this);
		try {
			parser.parse(stream);
			this.fillInterested();
		} catch (XmpParseException e) {
			if(e.getCode()==1099){
				LOG.info("remote closed.");
				this.close();
			}else{
				e.printStackTrace();
			}
		}
		
		//这里调用过来block的读取
		/*ByteBufferPool buffer=getBufferPool();
		ByteBuffer bytBuf = buffer.acquire(getInputBufferSize(), false);
		
		//SnowAbstractConnection last=setCurrentConnection(this);
		SnowSelectChannelEndPoint ep = (SnowSelectChannelEndPoint)getEndPoint() ;
		//首次连接的话 读验证 完成验证[生成会话信息保留 并返回] 否则发送提示并断开连接
		//
		int fillre=parse_fill(ep,bytBuf);
		
		if(fillre <  0 ){
			this.close(); 
		}	
		try {
			parser.parse();
			buffer.release(bytBuf);
		} catch (XmpParseException e) {
			e.printStackTrace();
		}*/
		// 其它的数据就是一般数据 留到业务层
		//to  read  httpcopnn 375
		
		
		/*System.out.println(">>>>"+this.hashCode());
		try {
			//while(true){
				if(ep.isOpen()){
					try {
						
						System.out.println(Thread.currentThread().getName());
						//ep.fillInterested(callback);
						undstandBufferTmp(sd, ep);
							fillInterested();
	
					} catch (IOException e) {
						//  Auto-generated catch block
						e.printStackTrace();
						
					}
		        	
		        }else{
		        	
					System.out.println("is cloded");
					
					//break;
		        }
			//}
		}finally {
			setCurrentConnection(last) ; 
			 if (LOG.isDebugEnabled())
	                LOG.debug("{} onFillable exit ", this);  
		}*/
	}

  

	/**
	 * 读入数据到buf
	 * @param ep
	 * @param bytBuf
	 * @return
	 */
	/*private int parse_fill(SnowSelectChannelEndPoint ep, ByteBuffer bytBuf) {
		try {
			
			int re = ep.fill(bytBuf);
			if(re > 0 ){
				parser.setBuf(bytBuf);
				parser.setConn(this);
				return  re; 
			}else{
				
				return -1 ;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}*/

/**
 * 零时测试用的
 * @param sd
 * @param ep
 * @throws IOException
 */
@Deprecated
	private void undstandBufferTmp(ByteBuffer sd, EndPoint ep) throws IOException {
		int fkil=ep.fill(sd);
		LOG.debug(fkil+"");
	//	sd.put((byte)'c');
		LOG.debug(sd.position()+"-"+sd.capacity()+"-"+sd.limit());
		sd.flip();
		LOG.debug(sd.position()+"-"+sd.capacity()+"-"+sd.limit());
		sd.clear();
	}


}
