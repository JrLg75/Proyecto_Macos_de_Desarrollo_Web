package com.techzone.peru.model.dto;

public record UsuarioAdminDTO(
        Long id,
        String nombre,
        String apellido,
        String email,
        String telefono,
        Integer rolId,
        String password // Opcional en edición
) {}