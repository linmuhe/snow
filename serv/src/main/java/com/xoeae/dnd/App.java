package com.xoeae.dnd;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootConfiguration
@SpringBootApplication
public class App {

    public  static void main(String[] args) throws Exception {


        System.out.println("############## Server start ###############");
      final ApplicationContext applicationContext = SpringApplication.run(App.class,args);
      System.out.println("############## Server started ###############");

    }
}
