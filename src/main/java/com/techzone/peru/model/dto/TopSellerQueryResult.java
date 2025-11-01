package com.techzone.peru.model.dto;

// DTO simple para mapear el resultado de nuestra consulta JPQL
public record TopSellerQueryResult(Long varianteId, Long totalVendido) {
}