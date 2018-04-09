package com.xoease.snowstorm.listen.noned;

import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;

/**
 * 通过一个线程任务 ScheduledExecutorScheduler 模拟出类似的TimerScheduler
 * @author muhe
 *
 */
public class ScheduledExecutorAtFixedRate extends ScheduledExecutorScheduler  {

	
	public ScheduledExecutorAtFixedRate() {
		super();
	}

	public ScheduledExecutorAtFixedRate(String name, boolean daemon, ClassLoader threadFactoryClassLoader,
			ThreadGroup threadGroup) {
		super(name, daemon, threadFactoryClassLoader, threadGroup);
	}

	public ScheduledExecutorAtFixedRate(String name, boolean daemon, ClassLoader threadFactoryClassLoader) {
		super(name, daemon, threadFactoryClassLoader);
	}

	public ScheduledExecutorAtFixedRate(String name, boolean daemon) {
		super(name, daemon);
	}
	@Override
	/* 
	 * @see org.eclipse.jetty.util.thread.ScheduledExecutorScheduler#schedule(java.lang.Runnable, long, java.util.concurrent.TimeUnit)
	 * @param fixedRate 间隔时间
	 * @param unit 时间单位
	 */
	public Task schedule(Runnable task, long fixedRate,TimeUnit unit) {
		Task taskre = super.schedule(new TimerScheduledFutureRunn(task,fixedRate), fixedRate, unit);
		return taskre  ;
	}
	/**
	 * 实现生命周期 的一个任务
	 * 
	 * @author muhe
	 */
	private static class TimerScheduledFutureRunn extends AbstractLifeCycle implements Runnable{
		private Runnable _runn ; 
		private  long _fixedRate;
		public void run() {
			try {
				this.start();
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}

		private void RunFixed(){
			_runn.run();
			if(isStarting()){
				try {
					Thread.sleep(_fixedRate);
					RunFixed(); 
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		@Override
		protected void doStart() throws Exception {
			super.doStart();
			RunFixed();
		}
		public TimerScheduledFutureRunn(Runnable r ,long fixedRate) {
			super();
			this._runn=r ; 
			this._fixedRate=fixedRate;
		}
	}
}
