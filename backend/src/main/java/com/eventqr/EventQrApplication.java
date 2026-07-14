package com.eventqr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EventQrApplication {

    private static final Logger logger = LoggerFactory.getLogger(EventQrApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EventQrApplication.class, args);
        logger.info("🚀 Server đang chạy tại http://localhost:8080");
    }
}
