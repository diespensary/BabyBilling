package com.example.brtservice;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRabbit
@EnableScheduling
public class BrtServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrtServiceApplication.class, args);
    }

}
