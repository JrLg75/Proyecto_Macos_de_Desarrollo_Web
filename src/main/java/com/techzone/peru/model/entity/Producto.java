package com.techzone.peru.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Entidad que representa un producto principal en el catálogo.
 * Un producto puede tener una o más variantes (ej. diferentes colores, tallas).
 */
@Entity
@Table(name = "productos")
@Data
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "activo", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean activo;

    @Column(name = "fecha_creacion", columnDefinition = "TIMESTAMPTZ DEFAULT NOW()")
    private OffsetDateTime fechaCreacion;

    // --- Relaciones ---

    // Muchos productos pueden pertenecer a una categoría.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // Un producto puede tener muchas variantes.
    // CascadeType.ALL: Si se borra un producto, se borran todas sus variantes.
    // orphanRemoval = true: Si se quita una variante de esta lista, se borra de la BD.
    @OneToMany(
            mappedBy = "producto",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private List<ProductoVariante> variants;
}