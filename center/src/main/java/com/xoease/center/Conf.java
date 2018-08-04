package com.xoease.center;

import com.albedo.java.thrift.rpc.server.map.ServiceMap;
import com.xoease.centerinterface.SecService;
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
    public ServiceMap serviceMap(SecService.Iface secService){
        ServiceMap serviceMap=new ServiceMap();
        serviceMap.addService(SecService.Iface.class.getName(),secService);

        return serviceMap;
    }
}
