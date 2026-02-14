package com.example.shop.config;

import com.example.shop.logging.AccessLogFilter;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccessLogFilterRegistration {

    // IMPORTANT: method name MUST NOT be "accessLogFilterRegistration"
    @Bean
    public FilterRegistrationBean<AccessLogFilter> accessLogFilterRegistrationBean(AccessLogFilter filter) {
        FilterRegistrationBean<AccessLogFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(filter);

        // run BEFORE Spring Security's filter chain
        reg.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER - 1);

        reg.addUrlPatterns("/*");
        return reg;
    }
}
