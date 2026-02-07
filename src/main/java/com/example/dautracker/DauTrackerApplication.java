package com.example.dautracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DauTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DauTrackerApplication.class, args);
        System.out.println("\n==================================");
        System.out.println("  DAU Tracker 应用已启动");
        System.out.println("  访问： http://localhost:9000");
        System.out.println("\n==================================");
    }

}
