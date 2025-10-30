package com.techzone.peru.model.dto;

// Puedes añadir anotaciones de validación directamente a los componentes
// import jakarta.validation.constraints.Email;
// import jakarta.validation.constraints.NotBlank; // Usa @NotBlank en vez de @NotEmpty para Strings
// import jakarta.validation.constraints.Size;



public record RegistroClienteDto(
        // @NotBlank(message = "El nombre no puede estar vacío")
        String nombre,

        // @NotBlank(message = "El apellido no puede estar vacío")
        String apellido,

        // @NotBlank(message = "El email no puede estar vacío")
        // @Email(message = "Debe ser un email válido")
        String email,

        String telefono, // Sigue siendo opcional

        // @NotBlank(message = "La contraseña no puede estar vacía")
        // @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password

) {}