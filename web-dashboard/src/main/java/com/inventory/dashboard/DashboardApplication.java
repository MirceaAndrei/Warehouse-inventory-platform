package com.inventory.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(DashboardApplication.class, args);
        System.out.println("\n===========================================");
        System.out.println("[OK] Inventory Dashboard Started Successfully!");
        System.out.println("🌐 Dashboard: http://localhost:8081");
        System.out.println("===========================================\n");
    }
}
