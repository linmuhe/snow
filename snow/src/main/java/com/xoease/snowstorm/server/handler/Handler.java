package com.xoease.snowstorm.server.handler;

import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.annotation.ManagedOperation;
import org.eclipse.jetty.util.component.Destroyable;
import org.eclipse.jetty.util.component.LifeCycle;

import com.xoease.snowstorm.server.SnowAbstractServer;

public interface Handler extends LifeCycle, Destroyable
{
    /**
     * Handle a request.
     * 
     * @param target
     *            The target of the request - either a URI or a name.
     * @param baseRequest
     *            The original unwrapped request object.
     * @param request
     *            The request either as the {@link Request} object or a wrapper of that request. The
     *            <code>{@link HttpConnection#getCurrentConnection()}.{@link HttpConnection#getHttpChannel() getHttpChannel()}.{@link HttpChannel#getRequest() getRequest()}</code>
     *            method can be used access the Request object if required.
     * @param response
     *            The response as the {@link Response} object or a wrapper of that request. The
     *            <code>{@link HttpConnection#getCurrentConnection()}.{@link HttpConnection#getHttpChannel() getHttpChannel()}.{@link HttpChannel#getResponse() getResponse()}</code>
     *            method can be used access the Response object if required.
     * @throws IOException
     *             if unable to handle the request or response processing
     * @throws ServletException
     *             if unable to handle the request or response due to underlying servlet issue
     */
   /* public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException;*/

    public void setServer(SnowAbstractServer server);

    @ManagedAttribute(value="the jetty server for this handler", readonly=true)
    public SnowAbstractServer getServer();

    @ManagedOperation(value="destroy associated resources", impact="ACTION")
    public void destroy();
}
