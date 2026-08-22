package br.com.ajudamutua;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AjudaMutuaApplication {
    public static void main(String[] args) {
        SpringApplication.run(AjudaMutuaApplication.class, args);
    }
}
