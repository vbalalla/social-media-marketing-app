package io.serendia.inbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InboxServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InboxServiceApplication.class, args);
    }
}
