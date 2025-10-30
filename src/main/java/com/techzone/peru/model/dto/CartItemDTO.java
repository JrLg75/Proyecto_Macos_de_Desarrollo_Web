package com.techzone.peru.model.dto;

import java.math.BigDecimal;

public class CartItemDTO {

    private final Long variantId;
    private final String nombre;
    private int cantidad;
    private final BigDecimal precioUnitario;
    private final String imagenUrl;

    public CartItemDTO(Long variantId, String nombre, int cantidad, BigDecimal precioUnitario, String imagenUrl) {
        this.variantId = variantId;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.imagenUrl = imagenUrl;
    }

    // Getters
    public Long getVariantId() { return variantId; }
    public String getNombre() { return nombre; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public String getImagenUrl() { return imagenUrl; }

    // Setter solo para cantidad
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    // Getter calculado para el subtotal del item
    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}