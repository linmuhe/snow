package com.xoease.snowstorm.xmp;

import java.util.Collection;
import java.util.List;

import com.xoease.snowstorm.server.handler.HandlerWrapper;
import com.xoease.snowstorm.xmp.handler.DefaultLastHandler;

/**
 * 会执行NEABS里的PARSEr
 * @author sks
 *
 */
public abstract class NextPparse extends HandlerWrapper implements Pparser{
	/*protected static Object  next(Pparser handl,Object msg,List<Object> stream){
		return handl.parse(msg, stream);
	}*/
	protected Collection<Pparser> getChild(){
		return  getBeans(Pparser.class);
	}
	
	/* 
	 * 
	 * @see com.xoease.snowstorm.xmp.Pparser#parse(java.lang.Object, java.util.List)
	 */
	@Override
	public Object parse(Object atta, List<Object> streamObj) {
		Collection<Pparser> child = getChild();
		for (Pparser pparser : child) {
			atta = pparser.parse(atta, streamObj);
		}
		return atta;
	}
}
