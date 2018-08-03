package com.xoease.center;


import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 *  中心服务器 被serv调用 存放一些中心全局数据
   以及服务调用
 //https://www.programcreek.com/java-api-examples/?api=org.eclipse.jetty.servlet.ServletHandler
 //https://thrift.apache.org/tutorial/java
 http://thrift.apache.org/tutorial/
  */
public class App {
    private static final Logger logger = Log.getLogger(App.class);

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
                return null ;
/*
                TMultiplexedProcessor processor = new TMultiplexedProcessor();
                Map<String, > serviceMap =null ;
                for (String beanName : serviceMap.keySet()) {
                    IThriftServerService serverService = (IThriftServerService) serviceMap.getService(beanName);
                    String processorName = serverService.getName();
                    TProcessor tProcessor = serverService.getProcessor(serverService);
                    processor.registerProcessor(processorName, tProcessor);
                    logger.info("Register a processorName {} processorImpl {}", processorName, tProcessor);
                }*/
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
