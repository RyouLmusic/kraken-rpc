package com.kraken;

import org.kraken.spring.starter.annotation.EnableAutoKrakenServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoKrakenServer
public class KrakenProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(KrakenProviderApplication.class, args);
    }

}