package com.xoease.snowstorm.xmp;

import java.util.Date;
import java.util.List;

public abstract class TimeoutParser extends NextPparse {
	public enum XMP_STATE{
		START,// ready to read 
		END, //read fin 
		CLOSE, // The associated stream/endpoint should be closed  
	    CLOSED
	};
	protected volatile XMP_STATE _state=null ;
	private long timeout_stimp ;
	/**
	 * @param timeout 读取过程中的超时时间
	 */
	public TimeoutParser(long timeout) {
		this.timeout_stimp =timeout;
	}
	/**
	 * 开始读取的时间
	 */
	private long read_begin_time=0;
	/**
	 * (就是解析协议开始)
	 * 第一次读到数据  时候调用 设置读取数据 状态 
	 */
	protected void readStart(){
		
		_state = XMP_STATE.START;
		read_begin_time=System.currentTimeMillis();

	}
	@Deprecated
	protected void readSuccess(){
		//读取到数据的时间  
		read_begin_time=System.currentTimeMillis();
	}
	protected void readEnd(){
		read_begin_time = 0 ;
		_state = XMP_STATE.END;
	}
	
	/**
	 * 默认没有开始读取的时候或读取完成 都为0
	 */
	//private static final boolean _timeout=false;
	/**
	 *解析过程中超时 这个只有解析器知道
	 * @return
	 */
	public boolean timeout(){
		return (read_begin_time > 0 && _state==XMP_STATE.START ) ? 
				(System.currentTimeMillis() - read_begin_time) > timeout_stimp : false;
	}
	
	
}
