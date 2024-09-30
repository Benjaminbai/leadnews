package com.heima.app.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AppGateWayApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppGateWayApplication.class, args);
    }
}
