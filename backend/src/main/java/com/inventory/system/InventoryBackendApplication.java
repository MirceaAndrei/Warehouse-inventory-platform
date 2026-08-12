package com.inventory.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableScheduling
public class InventoryBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryBackendApplication.class, args);
        System.out.println("\n===========================================");
        System.out.println("[OK] Inventory Backend Started Successfully!");
        System.out.println("[API] http://localhost:8082/api");
        System.out.println("[H2]  http://localhost:8082/h2-console");
        System.out.println("===========================================\n");
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
