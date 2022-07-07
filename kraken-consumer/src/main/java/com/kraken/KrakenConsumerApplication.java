package com.kraken;

import org.kraken.spring.starter.annotation.EnableAutoKrakenClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoKrakenClient
public class KrakenConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KrakenConsumerApplication.class, args);
    }

}
