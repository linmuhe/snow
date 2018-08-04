package com.xoease.center;

import com.albedo.java.thrift.rpc.common.config.AlbedoRpcProperties;
import com.albedo.java.thrift.rpc.common.zookeeper.ZookeeperFactory;
import com.albedo.java.thrift.rpc.server.map.ServiceMap;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
public class Conf {
    /*@Autowired
    private AlbedoRpcProperties properties ;*/
   /* @Bean
    public AlbedoRpcProperties albedoRpcProperties(){
        return  new AlbedoRpcProperties() ;
    }
    @Bean
    public CuratorFramework curatorFramework(){

        return  ZookeeperFactory. ;
    }*/

    /**
     * 创建服务
     * @return
     */
    @Bean
    public ServiceMap serviceMap(){
        ServiceMap serviceMap=new ServiceMap();


         //   serviceMap.addService(EchoSerivce.Iface.class.getName(),echoSerivce);

        return serviceMap;
    }
}
