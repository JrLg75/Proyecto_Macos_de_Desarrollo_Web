package com.techzone.peru.config;

import com.google.cloud.vertexai.VertexAI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VertexAiConfig {

    @Value("${gcp.project-id}")
    private String projectId;

    @Value("${gcp.location}")
    private String location;

    @Bean
    public VertexAI vertexAI() {
        // Igual que en GeminiAnalisisService: constructor (projectId, location)
        // Las credenciales se toman por ADC (GOOGLE_APPLICATION_CREDENTIALS) ya configuradas en tu IDE
        return new VertexAI(projectId, location);
    }
}


