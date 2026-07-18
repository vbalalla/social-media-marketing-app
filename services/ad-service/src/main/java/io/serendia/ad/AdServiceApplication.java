package io.serendia.ad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AdServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdServiceApplication.class, args);
    }
}
