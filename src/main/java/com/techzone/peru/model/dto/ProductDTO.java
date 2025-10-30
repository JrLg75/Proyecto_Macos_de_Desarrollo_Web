package com.techzone.peru.model.dto;

public record ProductDTO(
        Long id,
        String nombre,
        String descripcion,
        Integer categoriaId
) {}