package com.xoease.snowstorm.xmp.handler;

import java.util.List;

import com.xoease.snowstorm.conn.SnowServer;
import com.xoease.snowstorm.server.SnowAbstractServer;
import com.xoease.snowstorm.server.handler.Handler;
import com.xoease.snowstorm.server.handler.HandlerWrapper;
import com.xoease.snowstorm.xmp.NextPparse;
import com.xoease.snowstorm.xmp.Pparser;

/**
 * @author sks
 *接受协议传过来的实体
 */
public class DefaultLastHandler  extends  NextPparse implements LastHandler{
	private LastHandler _last;
	public DefaultLastHandler(SnowServer snowServer,LastHandler last) {
		setServer(snowServer);
		_last= last;
	}
	
	@Override
	protected void doStart() throws Exception {
		super.doStart();
		this.onStart(getServer());
	}
	@Override
	public Object parse(Object atta, List<Object> streamObj) {
		atta=super.parse(atta, streamObj);
		if(_last!=null)
			atta=_last.parse(atta, streamObj);
		return atta;
	}
	@Override
	public void onStart(SnowAbstractServer server) {
		if(_last!=null)
			_last.onStart(server);
	}
}
