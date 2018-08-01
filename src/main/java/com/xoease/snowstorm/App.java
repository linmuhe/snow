package com.xoease.snowstorm;

import com.xoease.snowstorm.conn.SnowServer;

public class App {
    public  static void main(String[] args) throws Exception {
        SnowServer ser = new SnowServer(8888, true);
        ser.setNoneSendDataIdle(true);
        ser.start();
    }
}
