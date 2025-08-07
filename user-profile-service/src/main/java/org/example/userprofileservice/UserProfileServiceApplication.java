package org.example.userprofileservice;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UserProfileServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserProfileServiceApplication.class, args);
    }

    @PostConstruct
    public void printSwaggerUrl() {
        System.out.println("Swagger UI: http://localhost:4002/swagger-ui/index.html");
    }
}
