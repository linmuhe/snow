package com.xoease.snowstorm.listen.noned;

import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.thread.Scheduler;

/**
 * 定时任务类
 * 需要用 start 去启动 
 * @author muhe 
 * 
 */
public abstract class SnowTimerTask extends ContainerLifeCycle implements Runnable{
	private long seconds = 30000; //每隔多少秒检查一次 
	private  Scheduler _scheduler; 
	
	protected Scheduler getScheduler() {
		return _scheduler;
	}
	protected void setScheduler(Scheduler _scheduler) {
		this._scheduler = _scheduler;
	}
	public long getSeconds() {
		return seconds;
	}
	/**
	 * 设置定时间隔
	 * @param seconds
	 */
	public void setSeconds(long seconds) {
		this.seconds = seconds;
	}
	@Override
	protected void doStart() throws Exception {
		super.doStart();
	 //   Runnable task = new CheckNoneSendRun(this);
	    //java 默认有个timer  TimerScheduler 里封装了timer
	    //任务 - 延迟多少时间 -以后间隔时间
		_scheduler.schedule(this, getSeconds(), TimeUnit.MILLISECONDS) ; 
		/*	System.out.println(Thread.currentThread().getName());
		 * this is  thread about main  ,can not do as  below   
			while (true) {
				System.out.println(connSets.size());
				Thread.sleep(3000);
				
			}
		*/
		
	}
	public SnowTimerTask() {
		super();
		//调用java的ScheduledThreadPoolExecutor
		this._scheduler =  new ScheduledExecutorAtFixedRate(null,true);
		addBean(this._scheduler) ; //要加入 因为new 出来的时候没有init ...
	}
	/*private class CheckNoneSendRun implements  Runnable{
		private SnowTimerTask _monitor;
		public void run() {
			System.err.println(Thread.currentThread().getName()+"--xxxxxxxxxxxxxxxxxxxxxxxxxx111");
		}
		public CheckNoneSendRun( SnowTimerTask monitor) {
			super();
			this._monitor = monitor;
		}
		
	}*/
}