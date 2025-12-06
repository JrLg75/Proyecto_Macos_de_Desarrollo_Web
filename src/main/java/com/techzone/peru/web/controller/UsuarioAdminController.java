package com.techzone.peru.web.controller;

import com.techzone.peru.model.dto.UsuarioAdminDTO;
import com.techzone.peru.model.entity.Cliente;
import com.techzone.peru.model.entity.Rol;
import com.techzone.peru.repository.ClienteRepository;
import com.techzone.peru.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioAdminController {

    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", clienteRepository.findAll());
        return "admin/usuarios/gestion-usuarios";
    }

    @GetMapping("/nuevo")
    public String nuevoUsuario(Model model) {
        model.addAttribute("usuarioDto", new UsuarioAdminDTO(null, "", "", "", "", null, ""));
        model.addAttribute("roles", rolRepository.findAll());
        return "admin/usuarios/formulario-usuario";
    }

    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute UsuarioAdminDTO dto) {
        Cliente cliente;

        // Modo Edición vs Creación
        if (dto.id() != null) {
            cliente = clienteRepository.findById(dto.id()).orElseThrow();
            // Solo actualizamos password si no viene vacío
            if (dto.password() != null && !dto.password().isBlank()) {
                cliente.setPasswordHash(passwordEncoder.encode(dto.password()));
            }
        } else {
            cliente = new Cliente();
            cliente.setFechaCreacion(OffsetDateTime.now());
            // En creación el password es obligatorio
            cliente.setPasswordHash(passwordEncoder.encode(dto.password()));
        }

        cliente.setNombre(dto.nombre());
        cliente.setApellido(dto.apellido());
        cliente.setEmail(dto.email());
        cliente.setTelefono(dto.telefono());

        // Asignar Rol
        if (dto.rolId() != null) {
            Rol rol = rolRepository.findById(dto.rolId()).orElseThrow();
            cliente.setRol(rol);
        }

        clienteRepository.save(cliente);
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String editarUsuario(@PathVariable Long id, Model model) {
        Cliente c = clienteRepository.findById(id).orElseThrow();

        UsuarioAdminDTO dto = new UsuarioAdminDTO(
                c.getId(),
                c.getNombre(),
                c.getApellido(),
                c.getEmail(),
                c.getTelefono(),
                c.getRol() != null ? c.getRol().getId() : null,
                null // No enviamos el password hash a la vista
        );

        model.addAttribute("usuarioDto", dto);
        model.addAttribute("roles", rolRepository.findAll());
        return "admin/usuarios/formulario-usuario";
    }
}