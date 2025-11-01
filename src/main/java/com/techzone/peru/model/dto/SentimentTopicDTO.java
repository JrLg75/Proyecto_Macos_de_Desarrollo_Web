package com.techzone.peru.model.dto;

// Guarda un tema específico (ej. "batería") y el sentimiento de esa reseña
public record SentimentTopicDTO(String tema, String sentimiento) {
}