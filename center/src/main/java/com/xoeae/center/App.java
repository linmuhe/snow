package com.xoeae.center;

import com.xoease.snowstorm.config.Snow;
import com.xoease.snowstorm.conn.SnowServer;
import com.xoease.snowstorm.server.SnowAbstractServer;
import com.xoease.snowstorm.util.PropertyUtil;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServlet;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerTransport;
import org.eclipse.jetty.annotations.WebServletAnnotationHandler;
 import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import java.security.Provider;
import java.util.Map;
import java.util.Set;

/**
 *  中心服务器 被serv调用 存放一些中心全局数据
   以及服务调用
 //https://www.programcreek.com/java-api-examples/?api=org.eclipse.jetty.servlet.ServletHandler
 //https://thrift.apache.org/tutorial/java
 http://thrift.apache.org/tutorial/
  */
public class App {
    private static final Logger logger = Log.getLogger(SnowAbstractServer.class);

    public  static void main(String[] args) throws Exception {

    }
    private static void runThriftServToIm(int port) throws Exception {
    /*    Server ser = new Server();
        TServlet ts = new TServlet();
        ServletHandler servlet = new ServletHandler();
        servlet.addmap
        ser.setHandler(servlet);
        ser.start();
        ser.join();*/
        Server s = new Server(8888) {

            @Override
            TProcessor processor() {

                TMultiplexedProcessor processor = new TMultiplexedProcessor();
                Map<String, > serviceMap =null ;
                for (String beanName : serviceMap.keySet()) {
                    IThriftServerService serverService = (IThriftServerService) serviceMap.getService(beanName);
                    String processorName = serverService.getName();
                    TProcessor tProcessor = serverService.getProcessor(serverService);
                    processor.registerProcessor(processorName, tProcessor);
                    logger.info("Register a processorName {} processorImpl {}", processorName, tProcessor);
                }
            }

            @Override
            TProtocolFactory protocolFactory() {
                return new TBinaryProtocol.Factory();
            }
        };
        s.start();
    }
    private static void runThriftServToHttpapi() throws Exception {
    /*    Server ser = new Server();
        TServlet ts = new TServlet();
        ServletHandler servlet = new ServletHandler();
        servlet.addmap
        ser.setHandler(servlet);
        ser.start();
        ser.join();*/
    }
}
