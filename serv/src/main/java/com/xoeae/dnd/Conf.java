package com.xoeae.dnd;

import com.albedo.java.thrift.rpc.client.proxy.ServiceStarter;
import com.albedo.java.thrift.rpc.common.vo.ServiceApi;
import com.xoease.snowstorm.config.Snow;
import com.xoease.snowstorm.conn.SnowServer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@SpringBootConfiguration
public class Conf implements InitializingBean {
    @Value("${snow.publicKey}")
    private String publicKey;
    @Value("${snow.privateKey}")
    private  String privateKey ;
    @Value("${snow.port}")
    private  int port ;
    @Value("${snow.host}")
    private String host ;

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    /**
     * 创建thriftClient
     * @return
     */
   /*@Bean
    public ServiceStarter serviceMap(){
       ServiceStarter st = new ServiceStarter();
       *//*    .startService(EchoSerivce.Iface.class, ServiceApi.create("echoSerivce"));*//*
       return  st ;
    }*/
    /**
     * 启动IM服务
     */
    @Bean(initMethod = "start")
    public SnowServer start() throws Exception {
        SnowServer ser = new SnowServer(){
        };
        ser.setNoneSendDataIdle(true);
        return ser;
       // ser.start();
       // ser.join();
    }

    /**
     * 初始化IM参数
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Snow.PublicKey =this.publicKey;
        Snow.PrivateKey =this.privateKey;
        Snow.port =this.port;
        Snow.host =this.host;
    }
}
