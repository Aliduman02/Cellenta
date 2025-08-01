package com.i2i.intern.cellenta.aom.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("OCS Model - AOM/MW Service Code Documentation")
                        .description("This document describes the endpoints used by the OCS model.")
                        .contact(new Contact()
                                .name("Onur Ekrem Yıldırım")
                                .email("oekremyildirim@outlook.com")
                                .url("www.linkedin.com/in/onurekremyildirim")
                        )
                        .version("v1.0"));
    }
}