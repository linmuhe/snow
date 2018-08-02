package com.xoease.snowstorm.server.handler;

import java.io.IOException;

import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.xoease.snowstorm.server.SnowAbstractServer;

public abstract class AbstractHandler extends ContainerLifeCycle implements Handler
{
    private static final Logger LOG = Log.getLogger(AbstractHandler.class);

    private SnowAbstractServer _server;
    
    /* ------------------------------------------------------------ */
    /**
     * 
     */
    public AbstractHandler()
    {
    	
    	
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.eclipse.thread.LifeCycle#start()
     */
    @Override
    protected void doStart() throws Exception
    {
        if (LOG.isDebugEnabled())
            LOG.debug("starting {}",this);
        if (_server==null)
            LOG.warn("No Server set for {}",this);
        super.doStart();
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.eclipse.thread.LifeCycle#stop()
     */
    @Override
    protected void doStop() throws Exception
    {
        if (LOG.isDebugEnabled())
            LOG.debug("stopping {}",this);
        super.doStop();
    }

    /* ------------------------------------------------------------ */
    @Override
    public void setServer(SnowAbstractServer server)
    {
        if (_server==server)
            return;
        if (isStarted())
            throw new IllegalStateException(STARTED);
        _server=server;
    }

    /* ------------------------------------------------------------ */
    @Override
    public SnowAbstractServer getServer()
    {
        return _server;
    }

    /* ------------------------------------------------------------ */
    @Override
    public void destroy()
    {
        if (!isStopped())
            throw new IllegalStateException("!STOPPED");
        super.destroy();
    }

    /* ------------------------------------------------------------ */
    @Override
    public void dumpThis(Appendable out) throws IOException
    {
        out.append(toString()).append(" - ").append(getState()).append('\n');
    }
    
}