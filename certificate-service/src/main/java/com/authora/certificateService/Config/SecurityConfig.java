package com.authora.certificateService.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    // Security filter chain for HTTP security
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Disable CSRF protection
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // Enable CORS
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/certificates/get/{userId}").permitAll()  // Public: Get certificate by userId
                        .requestMatchers("/api/users/certificates/*/download").permitAll()  // Public: Download certificate
                        .requestMatchers(HttpMethod.POST, "/api/users/certificates").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/certificates/*").authenticated()

                        .anyRequest().authenticated()  // All other requests require authentication
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);  // 🔥 Add JwtRequestFilter before UsernamePasswordAuthenticationFilter

        return http.build();
    }

    // CORS configuration to allow requests from any origin
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));  // Allow requests from any origin
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));  // Allow methods
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));  // Allow headers
        configuration.setExposedHeaders(Arrays.asList("Authorization"));  // Expose Authorization header

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // Apply to all paths

        return source;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
