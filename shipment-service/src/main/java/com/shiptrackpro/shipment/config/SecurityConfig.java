package com.shiptrackpro.shipment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Shipment Service.
 *
 * Key decisions:
 * 1. /api/v1/shipments/track/** is public — anyone can track by tracking number
 * 2. All other /api/v1/** endpoints are permit-all at Spring Security level —
 *    JWT validation is done by the API Gateway, not by this service.
 *    Role-based checks use X-User-Role header injected by the Gateway.
 * 3. CSRF disabled — stateless REST API
 * 4. Stateless sessions — no server-side session storage
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/shipments/track/**").permitAll()
                        .requestMatchers("/api/v1/shipments/**").permitAll()
                        .requestMatchers("/api/v1/drivers/**").permitAll()
                        .requestMatchers("/api/v1/vehicles/**").permitAll()
                        .requestMatchers("/api/v1/pod/**").permitAll()
                        .requestMatchers("/internal/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
