package com.techzone.peru.model.dto;

import java.util.List;

// DTO para mapear la respuesta JSON que le pedimos a OpenAI
public record AnalisisReviewDTO(
        String sentimiento,
        List<String> temas,
        String resumen) {
}