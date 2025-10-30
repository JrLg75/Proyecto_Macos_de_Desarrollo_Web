package com.techzone.peru.model.dto;

public record DashboardStatsDTO(
        long totalAnalizados,
        long positivos,
        long negativos,
        long neutrales) {
}