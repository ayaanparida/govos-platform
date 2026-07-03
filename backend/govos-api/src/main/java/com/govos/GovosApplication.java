package com.govos;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        info = @Info(
                title = "GovOS API",
                version = "0.1.0",
                description = "Enterprise Government Operating System — REST API",
                contact = @Contact(name = "GovOS Architecture Team"),
                license = @License(name = "Proprietary")
        )
)
@SpringBootApplication
public class GovosApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovosApplication.class, args);
    }
}
