package com.xoeae.dnd;

import com.xoease.snowstorm.config.Snow;
import com.xoease.snowstorm.conn.SnowServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@SpringBootConfiguration
public class Conf {
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

    @PostConstruct
    public void start() throws Exception {
        Snow.PublicKey =this.publicKey;
        Snow.PrivateKey =this.privateKey;
        Snow.port =this.port;
        Snow.host =this.host;
        /**
         * 启动IM服务
         */
        SnowServer ser = new SnowServer(){

        };
        ser.setNoneSendDataIdle(true);
        ser.start();
       // ser.join();
    }
}
