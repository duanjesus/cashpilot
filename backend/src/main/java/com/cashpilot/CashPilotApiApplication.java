package com.cashpilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CashPilotApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CashPilotApiApplication.class, args);
    }

}
