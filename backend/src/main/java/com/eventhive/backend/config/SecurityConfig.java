package com.eventhive.backend.config;

import com.eventhive.backend.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    // Password එක එන්ක්‍රිප්ට් කරන කෙනා
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Spring Boot එකෙන් ඉබේම හදන Authentication Manager ව පාවිච්චි කරනවා
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // ප්‍රධාන Security Rules ටික
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(org.springframework.security.config.Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // මෙන්න මෙතනට තමයි අපි අලුතින් "/error" එකතු කළේ
                        .requestMatchers("/api/auth/**", "/error").permitAll()
                        .requestMatchers("/api/users/create").permitAll()
                        .requestMatchers("/api/ai/chat").permitAll()
                        .requestMatchers("/api/reset-admin").permitAll()
                        .requestMatchers("/api/payments/notify").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/events").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/events/*").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/seats/event/*").permitAll()
                        .anyRequest().authenticated() // අනිත් හැම එකටම Token එක ඕන
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}