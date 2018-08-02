package com.xoeae.dnd;

import com.xoease.snowstorm.config.Snow;
import com.xoease.snowstorm.conn.SnowServer;
import com.xoease.snowstorm.util.PropertyUtil;

public class App {
    public  static void main(String[] args) throws Exception {

        Snow.PublicKey =PropertyUtil.getProperty("publicKey");
        Snow.PrivateKey =PropertyUtil.getProperty("privateKey");
        Snow.port =Integer.valueOf(PropertyUtil.getProperty("port"));
        Snow.host =PropertyUtil.getProperty("host");
        System.out.println("############## Server start ###############");

        /**
         * 启动IM服务
         */
        SnowServer ser = new SnowServer();
        ser.setNoneSendDataIdle(true);
        ser.start();


        System.out.println("############## Server started ###############");
        ser.join();
    }
}
