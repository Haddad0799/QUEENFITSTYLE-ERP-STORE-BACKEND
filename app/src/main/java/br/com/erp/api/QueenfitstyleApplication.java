package br.com.erp.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class QueenfitstyleApplication {
    public static void main(String[] args) {
        SpringApplication.run(QueenfitstyleApplication.class, args);
    }
}

