package com.spectrayan.synaptiq.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Security-related beans shared across modules.
 */
@Configuration
public class SecurityBeanConfig {

    /**
     * Single BCryptPasswordEncoder instance with default cost factor (10).
     * Injected by LoginService and SignupService instead of creating their own instances.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
