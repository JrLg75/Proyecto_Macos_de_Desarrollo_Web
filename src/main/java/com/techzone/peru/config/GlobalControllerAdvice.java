package com.techzone.peru.config;

import com.techzone.peru.model.entity.Cliente;
import com.techzone.peru.repository.ClienteRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private ClienteRepository clienteRepository;

    @ModelAttribute
    public void addAttributes(HttpServletRequest request, Model model, Authentication auth) {
        // 1. URL actual (para resaltar el menú activo en el Admin)
        model.addAttribute("currentUri", request.getRequestURI());

        // 2. Usuario Logueado (para el Navbar público)
        // Verificamos si hay un usuario autenticado y que no sea un usuario anónimo
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            Optional<Cliente> clienteOpt = clienteRepository.findByEmail(email);

            if (clienteOpt.isPresent()) {
                // ¡Esto es lo que espera tu HTML!
                model.addAttribute("nombreUsuario", clienteOpt.get().getNombre());

                // También pasamos el objeto cliente completo por si se necesita (ej. para el ID)
                model.addAttribute("clienteLogueado", clienteOpt.get());
            }
        }
    }
}