package com.techzone.peru.model.dto;

// DTO para mostrar el producto top en el dashboard
public record TopSellerDTO(
        String nombreProducto,
        String sku,
        long totalVendido
) {
}