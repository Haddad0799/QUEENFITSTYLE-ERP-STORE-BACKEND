package br.com.erp.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class QueenfitstyleApplication {
    public static void main(String[] args) {
        SpringApplication.run(QueenfitstyleApplication.class, args);
    }
}

