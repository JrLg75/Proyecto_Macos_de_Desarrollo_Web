package com.techzone.peru.model.dto;

import java.util.List;

// Contiene el resumen de un producto específico
public record ProductSentimentStatsDTO(
        String productName,
        List<SentimentTopicDTO> topics
) {
}