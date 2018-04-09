package com.xoease.snowstorm.server;

import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.deploy.graph.Edge;
import org.eclipse.jetty.io.AbstractConnection;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.Scheduler;

import com.xoease.snowstorm.config.Snow;
import com.xoease.snowstorm.conn.SnowConnectionFactory.NoneSendData;

/**
 * @author muhe
 * 在一定时间内没有交换过数据 的时候用  这个用listener来实现 
 * 
 * 还有一部分是解析的时候  超时  这个是用scheduler
 */
public abstract class SnowTimeoutConnection extends AbstractConnection implements NoneSendData {
		private Scheduler _scheduler=null;

	    private static final Logger LOG = Log.getLogger(AbstractConnection.class);

	protected SnowTimeoutConnection(EndPoint endp, Executor executor) {
		super(endp, executor);
		
	}
	protected SnowTimeoutConnection(EndPoint endp, Executor executor,Scheduler sched) {
		this(endp,executor);
		this._scheduler = sched;
	}
	@Override
	public void onOpen() {
		super.onOpen();
		if(_scheduler!=null)
			startParsetimeoutSchedulr();
		
	}
	/**
	 * 最后一次的读取到数据的时间
	 * 在onFillable里 设置它
	 */
	private long _lastReadTimeStamp = 0 ;   
	
	public boolean IsHappened() {
		if(  getLastReadTimeStamp() ==0  ){
			return true ; 
		}
		return false  ; 
	}
	/* 这是连接之后  没有发送过来数据的超市
	 * @see com.xoease.snowstorm.conn.SnowConnectionFactory.NoneSendData#timeout()
	 */
	@Override
	public boolean timeout(){
		long timeout=System.currentTimeMillis() - getCreatedTimeStamp() ; 
		return timeout > this._idleTimeout ;
	}
	public long getLastReadTimeStamp() {
		return _lastReadTimeStamp;
	}
	/**
	 *第一次读取到数据的时间值
	 * @param lastReadTimeStamp
	 */
	public void setFirstReadTimeStamp() {
		if(this._lastReadTimeStamp==0){
			this._lastReadTimeStamp =System.currentTimeMillis();
		}
	}
	/**
	 * @see SnowAbstractConnection#setIdleTimeout(long)
	 */
	private long _idleTimeout = Snow.TIMEOUT_PARSE ;
	/**
	 * 这在时间里没有发生过数据传输就执行onConnectedIdle
	 * @param _idleTimeout
	 */
	public void setIdleTimeout(long _idleTimeout) {
		this._idleTimeout = _idleTimeout;
	}
	/*
	 * 没有数据
	 * snowserver 可以设置这个选项
	 * @see com.xoease.snowstorm.conn.SnowConnectionFactory.NoneSendData#onConnectedIdle()
	 */
	@Override
	public void onNoneTimeout() {
		//LOG.debug( "close "+this.toString());
		this.close();
	}
	public long get_idleTimeout() {
		return _idleTimeout;
	}
	
	
	//parse timeout 
		/**
		 * 解析协议数据过程中 发生超时
		 * @return
		 */
		public boolean parseTimeout(){
			return _parsetimeOut && getEndPoint().isOpen();
		}
		private long _parseTimeoutVal=30000;
		/**
		 * 解析超时的时候使用 将超时标识设置为TRUE 当超时然后会触发 {@link #onParseTimeout()}
		 */
		public void setParseTimeout(){
			_parsetimeOut = true ;
		}
		private boolean _parsetimeOut =false;
		public void onParseTimeout(){
			LOG.info("timeout for parsing protocol on {}", this);
		}
		
		private void startParsetimeoutSchedulr() {
			scheduleIdleTimeout(_parseTimeoutVal);
		}
		private final Runnable _idleTask= new Runnable()
	    {
	        @Override
	        public void run()
	        {
	           if(parseTimeout()){
	        	   onParseTimeout();
	           }
	           if(getEndPoint().isOpen()){
	        	   scheduleIdleTimeout(_parseTimeoutVal);
	           }
	        }
	    };
	    private final AtomicReference<Scheduler.Task> _timeout = new AtomicReference<>();
		private void scheduleIdleTimeout(long delay)
	    {
	        Scheduler.Task newTimeout = null;
	        
			if (delay > 0 )
	            newTimeout = _scheduler.schedule(_idleTask, delay, TimeUnit.MILLISECONDS);
	        Scheduler.Task oldTimeout = _timeout.getAndSet(newTimeout);
	        if (oldTimeout != null )
	            oldTimeout.cancel();
	    }
		//end timeout 
}
