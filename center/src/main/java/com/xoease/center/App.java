package com.xoease.center;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

import org.eclipse.jetty.util.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.net.UnknownHostException;

/**
 *  中心服务器 被serv调用 存放一些中心全局数据
   以及服务调用
 //https://www.programcreek.com/java-api-examples/?api=org.eclipse.jetty.servlet.ServletHandler
 //https://thrift.apache.org/tutorial/java
 http://thrift.apache.org/tutorial/
  */

@SpringBootApplication
@Configuration
@ComponentScan(value = {"com.albedo.java.thrift.rpc","com.xoease.center"})
public class App {
   protected static final org.eclipse.jetty.util.log.Logger log = Log.getLogger(App.class);

    @Autowired
    private Environment env;

    /**
     * Main method, used to run the application.
     *
     * @param args the command line arguments
     * @throws UnknownHostException if the local host name could not be resolved into an address
     */
    public static void main(String[] args) throws Exception {

       /* SpringApplication app = new SpringApplication(App.class);
        final ApplicationContext applicationContext = app.run(args);*/
        final ApplicationContext applicationContext=  SpringApplication.run(App.class,args);
        Environment env = applicationContext.getEnvironment();
        log.info("Application '{}' is running! ",
                env.getProperty("spring.application.name"));

    }
    @Deprecated
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
            public TProcessor processor() {
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
            public TProtocolFactory protocolFactory() {
                return new TBinaryProtocol.Factory();
            }
        };
        s.start();
    }
    @Deprecated
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
