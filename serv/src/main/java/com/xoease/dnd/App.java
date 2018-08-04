package com.xoease.dnd;
import com.xoease.centerinterface.SecService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication
@ComponentScan(value = {"com.albedo.java.thrift.rpc","com.xoease.dnd"})
public class App {
   // private static final Logger log = LoggerFactory.getLogger(App.class);

    public  static void main(String[] args) throws Exception {
/*
        log.info("############## Server start ###############");
*/
      System.out.println("############## Server start ###############");
      final ApplicationContext applicationContext = SpringApplication.run(App.class,args);
       SecService.Iface xx = applicationContext.getBean(SecService.Iface.class);
     //  log.debug(xx.isOk("xx","xxxx")+"");
      System.out.println("############## Server started ###############");

    }
}
