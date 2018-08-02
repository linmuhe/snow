package com.xoease.snowstorm.util;

import java.io.*;
import java.util.Properties;

public class PropertyUtil {
     private static Properties props;
    static{
        loadProps();
    }
    synchronized static private void loadProps(){
         props = new Properties();
        InputStream in = null;
        try {
             in = PropertyUtil.class.getClassLoader().getResourceAsStream("conf.properties");
            props.load(in);
        } catch ( Exception e) {
            System.out.println(e);
        } finally {
            try {
                if(null != in) {
                    in.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }

    }

    public static String getProperty(String key){
        if(null == props) {
            loadProps();
        }
        return props.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        if(null == props) {
            loadProps();
        }
        return props.getProperty(key, defaultValue);
    }
}