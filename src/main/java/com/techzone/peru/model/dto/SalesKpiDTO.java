package com.techzone.peru.model.dto;

import java.math.BigDecimal;
import java.util.List;

// DTO principal para el dashboard de ventas
public record SalesKpiDTO(
        BigDecimal ingresosTotales,
        long numeroDePedidos,
        BigDecimal ticketPromedio,
        List<TopSellerDTO> topSellers
) {
}