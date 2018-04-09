package com.xoease.snowstorm.server;

import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.xoease.snowstorm.conn.SnowConnection;
import com.xoease.snowstorm.conn.SnowServer;

public abstract class  SnowAbstractConnection extends SnowTimeoutConnection {
	
	public ByteBufferPool getBufferPool() {
		return _bufferPool;
	}
	protected static final Logger LOG = Log.getLogger(SnowConnection.class);
	//这两个方法不要调用
	@Override
	@Deprecated
	public long getBytesIn() {
		return super.getBytesIn();
	}

	@Override
	@Deprecated
	public long getBytesOut() {
		return super.getBytesOut();
	}
/*	private static final ThreadLocal<SnowAbstractConnection> __currentConnection = new ThreadLocal<>();*/
    private final ByteBufferPool _bufferPool;
	

	/*public static SnowAbstractConnection getCurrentConnection()
    {
        return __currentConnection.get();
        
    }*/
   /* protected static SnowAbstractConnection setCurrentConnection(SnowAbstractConnection connection)
    {
    	SnowAbstractConnection last=__currentConnection.get();
        __currentConnection.set(connection);
        return last;
    }*/
	protected Connector _connector;
	protected SnowAbstractConnection(Connector connector, EndPoint endPoint) {
		super(endPoint, connector.getExecutor(),connector.getScheduler());
		_connector= connector;
		
		_bufferPool=connector.getByteBufferPool() ; 
	}
	
	@Override
	public void onOpen() {
		//will call listener 
		// add  to connectorBean<NoneSendMonitor.class>
		super.onOpen();
		fillInterested();
	}
	
/*	public void onFillable() {
		
	}*/
	
	/*@Override
	public void onFillable() {
		
		ArrayByteBufferPool buffer=new ArrayByteBufferPool();
		ByteBuffer sd = buffer.acquire(1034, false);
		
		//to  read  httpcopnn 375
		try {
			
			int c= getEndPoint().fill(sd);
			System.out.println(BufferUtil.toString(sd));
			System.out.println(c +"-"+sd.capacity());
	        if(c==-1){
	        	getEndPoint().shutdownOutput();
	        }
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	*/

       /* HttpConnection last=setCurrentConnection(this);
        try
        {
            while (true)
            {
                // Fill the request buffer (if needed)
                int filled = fillRequestBuffer();

                // Parse the request buffer
                boolean handle = parseRequestBuffer();
                // If there was a connection upgrade, the other
                // connection took over, nothing more to do here.
                if (getEndPoint().getConnection()!=this)
                    break;

                // Handle close parser
                if (_parser.isClose() || _parser.isClosed())
                {
                    close();
                    break;
                }

                // Handle channel event
                if (handle)
                {
                    boolean suspended = !_channel.handle();

                    // We should break iteration if we have suspended or changed connection or this is not the handling thread.
                    if (suspended || getEndPoint().getConnection() != this)
                        break;
                }

                // Continue or break?
                else if (filled<=0)
                {
                    if (filled==0)
                        fillInterested();
                    break;
                }
            }
        }
        finally
        {
            setCurrentConnection(last);
            if (LOG.isDebugEnabled())
                LOG.debug("{} onFillable exit {}", this, _channel.getState());
        }*/
	//}


	/*@Override
	protected void onFillInterestedFailed(Throwable cause) {
		//System.out.println("on fillIntegerstafailded");
		super.onFillInterestedFailed(cause);
		call onreadedtimeout
	}*/

}
