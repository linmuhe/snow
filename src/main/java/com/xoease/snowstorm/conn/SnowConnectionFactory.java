package com.xoease.snowstorm.conn;

import com.xoease.snowstorm.server.SnowAbstractServer;
import org.eclipse.jetty.io.AbstractConnection;
import org.eclipse.jetty.io.Connection;
import org.eclipse.jetty.io.EndPoint;

import com.xoease.snowstorm.config.Snow;
import com.xoease.snowstorm.listen.noned.NoneDataConnListener;
import com.xoease.snowstorm.server.AbstractConnectionFactory;
import com.xoease.snowstorm.server.Connector;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import java.nio.ByteBuffer;

/**
 * factory 会在connector里调用创建 然后转到snowserver的方法里创建
 * @author linkedfun1
 *  验证流程
 *     Ser -> HalloSnow;{publickey}\n
 *     clien-> 客户发向服务器送 和des publiv过密钥(KK);KK加密过的铭文(B) /
 *     ser-> 验证OK发送 OkSnow;\n or FailSnow;\n 并断开连接
 *
 *     下面数据通信采用 AES对称加密
 */
public class SnowConnectionFactory extends AbstractConnectionFactory implements Connection.Listener{
    private static final Logger LOG = Log.getLogger(SnowConnectionFactory.class);

    @Override
	public void onOpened(Connection connection) {
		//发送需要验证指令HalloLin
		String halo="HalloSnow;%s\n";
       halo= String.format(halo, Snow.PublicKey);
		connection.getEndPoint().write(new Callback() {
            @Override
            public void failed(Throwable x) {
                LOG.warn("发送握手指令失败{}",x.getMessage());
            }
        }, ByteBuffer.wrap(halo.getBytes()));
	}

	@Override
	public void onClosed(Connection connection) {

	}

	/**
	 * use in connection 
	 * @author linkedfun1
	 *
	 */
	public interface NoneSendData {
		/**
		 * 连接了成功后 但是在一段时间内从没有发送过数据 就调用这个方法
		 */
		void onNoneTimeout();
		/**
		 * =true 代表没有交换过数据
		 *  才会执行方法onConnectedIdle
		 * 
		 */
		boolean IsHappened();
		boolean timeout();
	}
	public SnowConnectionFactory() {
		super(Snow.PROTOCAL);

	}

	@Override
	public Connection newConnection(Connector connector, EndPoint endPoint) {

		AbstractConnection connection = this.configure( connector, endPoint);

		return connection ;
	}

	protected AbstractConnection configure(Connector connector, EndPoint endPoint) {
		SnowConnection connection=new SnowConnection( connector,  endPoint);
        SnowServer s= (SnowServer) connector.getServer();
        if(s.isNoneSendDataIdle() && getBeans(NoneDataConnListener.class).size()== 0){
            NoneDataConnListener  connListener = new NoneDataConnListener(connector);
            addBean(connListener);

        }
		//connection.addListener(connListener);
		//设置缓冲区的大小
		//会把Facotry 和 connector 里实现了 Connection.Listener 的bean  加到connection的Listener
		return super.configure(connection, connector, endPoint);
	}

	
	
}
