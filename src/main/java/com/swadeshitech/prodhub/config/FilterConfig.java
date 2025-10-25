package com.swadeshitech.prodhub.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<UserIdentityFilter> userIdentityFilterRegistration() {
        FilterRegistrationBean<UserIdentityFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new UserIdentityFilter());
        registrationBean.addUrlPatterns("/*"); // Intercept all requests
        registrationBean.setOrder(1); // Optional: define order if multiple filters

        return registrationBean;
    }
}
