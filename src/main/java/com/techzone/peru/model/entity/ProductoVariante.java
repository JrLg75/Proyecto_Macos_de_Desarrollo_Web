package com.techzone.peru.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "productos_variantes")
@Data
public class ProductoVariante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 100)
    private String sku;

    @Column(nullable = false)
    private int stock;

    @Column(length = 50)
    private String color;

    @Column(length = 50)
    private String talla;

    @Column(nullable = false)
    private BigDecimal precio;

    private boolean descuentoActivo;
    private BigDecimal precioDescuento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    private String imagenPrincipalUrl;
}