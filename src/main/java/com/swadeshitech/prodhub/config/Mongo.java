package com.swadeshitech.prodhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@EnableMongoAuditing
@Configuration
public class Mongo {

    @Bean
    public AuditorAware<String> springSecurityAuditorAware() {
        return new AuditorAwareImpl();
    }
}
