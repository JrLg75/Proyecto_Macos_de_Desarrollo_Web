package com.techzone.peru.model.dto;

import java.math.BigDecimal;

public record VariantDTO(
        Long id,
        String sku,
        int stock,
        BigDecimal precio,
        String color,
        String talla
) {}