package com.techzone.peru.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Deshabilitamos CSRF para ambas rutas de API/prueba
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/v1/chatbot/**", "/api/v1/agent/**", "/test-analisis/**")
                )
                // 2. Definimos las reglas de autorización.
                .authorizeHttpRequests(authz -> authz
                        // Permitimos el acceso público al chatbot, al agente conversacional y a la ruta de prueba
                        .requestMatchers("/api/v1/chatbot/**", "/api/v1/agent/**", "/test-analisis/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/uploads/**", "/", "/registro", "/login").permitAll()
                        // Cualquier otra solicitud debe ser autenticada.
                        .anyRequest().authenticated()
                )
                // 3. Configuramos el formulario de login por defecto para las rutas que sí están protegidas.
                .formLogin(withDefaults());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}