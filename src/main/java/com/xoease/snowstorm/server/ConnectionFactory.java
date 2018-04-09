package com.xoease.snowstorm.server;

import java.util.List;

import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;

public interface ConnectionFactory
{
    /* ------------------------------------------------------------ */
    /**
     * @return A string representing the primary protocol name.
     */
    public String getProtocol();

    /* ------------------------------------------------------------ */
    /**
     * @return A list of alternative protocol names/versions including the primary protocol.
     */
    public List<String> getProtocols();
    
    /**
     * <p>Creates a new {@link Connection} with the given parameters</p>
     * @param connector The {@link Connector} creating this connection
     * @param endPoint the {@link EndPoint} associated with the connection
     * @return a new {@link Connection}
     */
    public Connection newConnection(Connector connector, EndPoint endPoint);
    
    
   /* public interface Upgrading extends ConnectionFactory
    {
         ------------------------------------------------------------ 
        *//** Create a connection for an upgrade request.
         * <p>This is a variation of {@link #newConnection(Connector, EndPoint)} that can create (and/or customise)
         * a connection for an upgrade request.  Implementations may call {@link #newConnection(Connector, EndPoint)} or 
         * may construct the connection instance themselves.</p>
         *  
         * @param connector  The connector to upgrade for.
         * @param endPoint The endpoint of the connection.
         * @param upgradeRequest The meta data of the upgrade request.
         * @param responseFields  The fields to be sent with the 101 response
         * @return Null to indicate that request processing should continue normally without upgrading. A new connection instance to
         * indicate that the upgrade should proceed.
         * @throws BadMessageException Thrown to indicate the upgrade attempt was illegal and that a bad message response should be sent.
         *//*
        public Connection upgradeConnection(Connector connector, EndPoint endPoint, MetaData.Request upgradeRequest,HttpFields responseFields) throws BadMessageException;
    }*/
}