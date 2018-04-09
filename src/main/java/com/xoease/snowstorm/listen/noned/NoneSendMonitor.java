package com.xoease.snowstorm.listen.noned;

import java.util.concurrent.ConcurrentLinkedDeque;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.xoease.snowstorm.config.Snow;
import com.xoease.snowstorm.server.SnowAbstractConnection;

/**
 * @author muhe
 * 
 * 客户连接成功但是长时间不发送任何数据的情况
 */
public class NoneSendMonitor extends SnowTimerTask implements InterNoneSendMonitor{
	private static final Logger LOG = Log.getLogger(NoneSendMonitor.class);

	private ConcurrentLinkedDeque<SnowAbstractConnection> connSets= new ConcurrentLinkedDeque<SnowAbstractConnection>() ; 
	@Override
	public void add(SnowAbstractConnection con){
		connSets.add(con);
	}
	{
		setSeconds(Snow.TIMEOUT_NODATA);//30秒
	}
	@Override
	public void run() {
				for (SnowAbstractConnection conn : connSets) {
					//在超时的情况下
					if(conn.timeout()){
						//如果没有发生数据交换 
						if(conn.IsHappened()){
							connSets.remove(conn);
							conn.onNoneTimeout();
							LOG.info("close {}", conn);
						}
					}
				}
	}
}
