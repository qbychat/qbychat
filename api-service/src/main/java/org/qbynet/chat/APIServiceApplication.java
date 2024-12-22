package org.qbynet.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class APIServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(APIServiceApplication.class, args);
    }

}
