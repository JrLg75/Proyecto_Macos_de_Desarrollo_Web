package com.techzone.peru.web.controller;

import com.techzone.peru.model.entity.Categoria;
import com.techzone.peru.model.entity.Cliente; // <-- AÑADIR
import com.techzone.peru.model.entity.Producto;
import com.techzone.peru.repository.CategoriaRepository;
import com.techzone.peru.repository.ClienteRepository; // <-- AÑADIR
import com.techzone.peru.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication; // <-- AÑADIR
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private ClienteRepository clienteRepository; // <-- AÑADIR

    @GetMapping("/")
    public String home(Model model, Authentication authentication) { // <-- AÑADIR Authentication

        // --- Lógica para el nombre de usuario ---
        if (authentication != null && authentication.isAuthenticated()) {
            // authentication.getName() nos da el email (username)
            clienteRepository.findByEmail(authentication.getName()).ifPresent(cliente -> {
                // Añadimos el nombre real al modelo
                model.addAttribute("nombreUsuario", cliente.getNombre());
            });
        }
        // --- Fin lógica de usuario ---

        List<Producto> productosDestacados = productService.findAllProducts();
        List<Categoria> todasLasCategorias = categoriaRepository.findAll();

        model.addAttribute("productos", productosDestacados);
        model.addAttribute("categorias", todasLasCategorias);

        return "index";
    }
}