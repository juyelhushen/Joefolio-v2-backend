package com.juyel.joefolio_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableFeignClients
public class JoefolioBackendApplication {

  public static void main(String[] args) {
    System.out.println("Java version: " + System.getProperty("java.version"));
    System.out.println("Java home: " + System.getProperty("java.home"));

    SpringApplication.run(JoefolioBackendApplication.class, args);
  }
}
