package com.techzone.peru.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// NO CAMBIA NADA AQUÍ
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF (Esto estaba bien)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/v1/chatbot/**", "/api/v1/agent/**", "/test-analisis/**")
                )
                // 2. Autorización (Esto estaba bien)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/v1/chatbot/**", "/api/v1/agent/**", "/test-analisis/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/uploads/**", "/", "/registro", "/login").permitAll()
                        .anyRequest().authenticated()
                )
                // 3. REEMPLAZA .formLogin(withDefaults()) CON ESTO:
                .formLogin(form -> form
                        .loginPage("/login") // Le dice a Spring "Mi página de login está en GET /login"
                        .loginProcessingUrl("/login") // El <form action="/login" method="post"> será procesado por Spring
                        .usernameParameter("username") // El campo de usuario se llama "username" (coincide con tu login.html)
                        .passwordParameter("password") // El campo de password se llama "password" (coincide con tu login.html)
                        .defaultSuccessUrl("/", true) // A dónde ir si el login es exitoso
                        .failureUrl("/login?error=true") // A dónde ir si falla (le pasamos un parámetro de error)
                        .permitAll() // Asegura que todos (incluso anónimos) puedan ver /login
                );

        return http.build();
    }

    // (Tu bean de PasswordEncoder se queda igual, está perfecto)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}