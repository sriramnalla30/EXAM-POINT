package com.project.exam_point.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors() // Enable CORS processing with the WebConfig settings
            .and()
            .csrf().disable()
            .authorizeRequests()
            .requestMatchers(
                "/api/users/signup",
                "/api/users/login",
                "/api/exams",
                "/api/exams/title/**",
                "/api/exams/editExam/**",
                "/api/exams/updateExam/**",
                "/api/exams/deleteQuestion/**",
                "/api/exams/{examId}",
                "/api/exams/submit",
                "/api/evaluation/progress/**",  // Add new evaluation endpoint
                "/api/evaluation/validateAnswer",
                "/api/evaluation/details/**",
                "/api/evaluation/{examId}"// Add the new endpoint for fetching evaluation details
            ).permitAll() // Allow unrestricted access to these paths
            .anyRequest().authenticated() // Require authentication for all other requests
            .and()
            .formLogin().disable() // Disable form-based login
            .httpBasic().disable(); // Disable HTTP Basic authentication

        return http.build();
    }
}
