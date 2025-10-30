package com.techzone.peru.service;


import com.techzone.peru.model.dto.RegistroClienteDto;
import com.techzone.peru.model.entity.Cliente;
import com.techzone.peru.model.entity.Rol;
import com.techzone.peru.repository.ClienteRepository;
import com.techzone.peru.repository.RolRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.OffsetDateTime; // Para la fecha de creación

@Service // Marca esta clase como un servicio de Spring
public class RegistroClientesService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Inyecta el codificador de contraseñas

    @Transactional // Asegura que toda la operación sea atómica (o todo o nada)
    public Cliente registrarNuevoCliente(RegistroClienteDto clienteDto) {

        // 1. Verificar si el email ya existe
        if (clienteRepository.existsByEmail(clienteDto.email())) {
            throw new RuntimeException("El email '" + clienteDto.email() + "' ya está registrado.");
        }

        // 2. Buscar el rol por defecto (ajusta "ROLE_CLIENTE" si usas otro nombre)
        Rol rolCliente = rolRepository.findByNombre("ROLE_CLIENTE")
                .orElseThrow(() -> new RuntimeException("Error: Rol 'ROLE_CLIENTE' no encontrado."));

        // 3. Crear la nueva entidad Cliente
        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setNombre(clienteDto.nombre());
        nuevoCliente.setApellido(clienteDto.apellido());
        nuevoCliente.setEmail(clienteDto.email());
        nuevoCliente.setTelefono(clienteDto.telefono()); // Asigna el teléfono
        nuevoCliente.setFechaCreacion(OffsetDateTime.now()); // Establece la fecha actual

        // 4. ¡IMPORTANTE! Cifrar la contraseña
        nuevoCliente.setPasswordHash(passwordEncoder.encode(clienteDto.password()));

        // 5. Asignar el rol
        nuevoCliente.setRol(rolCliente);

        // 6. Guardar el cliente en la base de datos
        return clienteRepository.save(nuevoCliente);
    }
}