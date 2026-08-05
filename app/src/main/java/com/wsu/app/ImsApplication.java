package com.wsu.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.wsu")
@ComponentScan(basePackages = {
        "com.wsu.core.*",
        "com.wsu.app.*",
        "com.wsu.auth.*",
        "com.wsu.organization.*"
})
@EntityScan(basePackages = "com.wsu.core.model.entities")
@EnableJpaRepositories(basePackages = "com.wsu.core.repository")
public class ImsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImsApplication.class, args);
    }
}