package com.swadeshitech.prodhub.config.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class Swagger {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Prodhub API")
                        .description("This API endpoint provides endpoints for managing Prodhub")
                        .version("1.0"));
    }
}
