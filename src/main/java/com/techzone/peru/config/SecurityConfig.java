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
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/v1/chatbot/**", "/api/v1/agent/**", "/test-analisis/**", "/api/v1/admin-agent/**")
                )
                .authorizeHttpRequests(authz -> authz
                        // 1. Rutas Públicas (Todo el mundo puede verlas)
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/uploads/**").permitAll()
                        .requestMatchers("/", "/registro", "/login", "/logout", "/productos/**", "/producto/**", "/buscar", "/contacto", "/nosotros", "/suscribirse").permitAll()
                        .requestMatchers("/api/v1/chatbot/**", "/api/v1/agent/**", "/test-analisis/**").permitAll()

                        // 2. Rutas de Administrador (SOLO para ADMIN)
                        // Spring Security convierte automáticamente hasRole("ADMIN") a buscar "ROLE_ADMIN" en la BD
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/admin-agent/**").hasRole("ADMIN")

                        // 3. Todo lo demás requiere autenticación (ej. /carrito, /checkout, /perfil)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true) // Podrías cambiar esto para que los admins vayan directo a /admin/dashboard
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                // Asegúrate de tener el logout configurado
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    // (Tu bean de PasswordEncoder se queda igual, está perfecto)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}