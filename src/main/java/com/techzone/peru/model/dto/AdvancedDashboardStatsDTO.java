package com.techzone.peru.model.dto;

import java.util.Collection;

// El DTO principal para el dashboard, incluye los contadores y las estadísticas por producto
public record AdvancedDashboardStatsDTO(
        long totalAnalizados,
        long positivos,
        long negativos,
        long neutrales,
        Collection<ProductSentimentStatsDTO> statsPorProducto
) {
}