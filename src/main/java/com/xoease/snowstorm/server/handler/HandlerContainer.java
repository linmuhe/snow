package com.xoease.snowstorm.server.handler;

import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.component.LifeCycle;

public interface HandlerContainer extends LifeCycle
{
    /* ------------------------------------------------------------ */
    /**
     * @return array of handlers directly contained by this handler.
     */
    @ManagedAttribute("handlers in this container")
    public Handler[] getHandlers();
    
    /* ------------------------------------------------------------ */
    /**
     * @return array of all handlers contained by this handler and it's children
     */
    @ManagedAttribute("all contained handlers")
    public Handler[] getChildHandlers();
    
    /* ------------------------------------------------------------ */
    /**
     * @param byclass the child handler class to get
     * @return array of all handlers contained by this handler and it's children of the passed type.
     */
    public Handler[] getChildHandlersByClass(Class<?> byclass);
    
    /* ------------------------------------------------------------ */
    /**
     * @param byclass the child handler class to get
     * @return first handler of all handlers contained by this handler and it's children of the passed type.
     * @param <T> the type of handler
     */
    public <T extends Handler> T getChildHandlerByClass(Class<T> byclass);
}
