package org.qbynet.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ChatResourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatResourceApplication.class, args);
    }

}
