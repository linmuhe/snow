package com.xoease.snowstorm.server.handler;

import java.util.List;

import org.eclipse.jetty.util.annotation.ManagedAttribute;

public class HandlerWrapper extends AbstractHandlerContainer
{
    protected Handler _handler;

    /* ------------------------------------------------------------ */
    /**
     *
     */
    public HandlerWrapper()
    {
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the handlers.
     */
    @ManagedAttribute(value="Wrapped Handler", readonly=true)
    public Handler getHandler()
    {
        return _handler;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the handlers.
     */
    @Override
    public Handler[] getHandlers()
    {
        if (_handler==null)
            return new Handler[0];
        return new Handler[] {_handler};
    }

    /* ------------------------------------------------------------ */
    /**
     * @param handler Set the {@link Handler} which should be wrapped.
     */
    public void setHandler(Handler handler)
    {
        if (isStarted())
            throw new IllegalStateException(STARTED);

        if (handler!=null)
            handler.setServer(getServer());
        
        Handler old=_handler;
        _handler=handler;
        updateBean(old,_handler,true);
    }

    /* ------------------------------------------------------------ */
    /** 
     * Replace the current handler with another HandlerWrapper
     * linked to the current handler.  
     * <p>
     * This is equivalent to:
     * <pre>
     *   wrapper.setHandler(getHandler());
     *   setHandler(wrapper);
     * </pre>
     * @param wrapper the wrapper to insert
     */
    public void insertHandler(HandlerWrapper wrapper)
    {
        if (wrapper==null || wrapper.getHandler()!=null)
            throw new IllegalArgumentException();
        wrapper.setHandler(getHandler());
        setHandler(wrapper);
    }
    /* ------------------------------------------------------------ */
    @Override
    protected void expandChildren(List<Handler> list, Class<?> byClass)
    {
        expandHandler(_handler,list,byClass);
    }

    /* ------------------------------------------------------------ */
    @Override
    public void destroy()
    {
        if (!isStopped())
            throw new IllegalStateException("!STOPPED");
        Handler child=getHandler();
        if (child!=null)
        {
            setHandler(null);
            child.destroy();
        }
        super.destroy();
    }

}