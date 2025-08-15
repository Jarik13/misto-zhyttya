package org.example.mediaservice;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MediaServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediaServiceApplication.class, args);
    }

    @PostConstruct
    public void printSwaggerUrl() {
        System.out.println("Swagger UI: http://localhost:4003/swagger-ui/index.html");
    }
}
