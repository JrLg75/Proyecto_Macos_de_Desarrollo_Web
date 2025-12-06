package com.techzone.peru.web.controller;

import com.techzone.peru.model.entity.Categoria;
import com.techzone.peru.model.entity.Cliente;
import com.techzone.peru.model.entity.Producto;
import com.techzone.peru.repository.CategoriaRepository;
import com.techzone.peru.repository.ClienteRepository;
import com.techzone.peru.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoriaRepository categoriaRepository;
    // (El clienteRepository ya no es estrictamente necesario aquí gracias a GlobalControllerAdvice,
    // pero lo dejamos por si acaso usas lógica específica en el home)
    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping("/")
    public String home(Model model) {
        List<Producto> productosDestacados = productService.findAllProducts();
        List<Categoria> todasLasCategorias = categoriaRepository.findAll();
        model.addAttribute("productos", productosDestacados);
        model.addAttribute("categorias", todasLasCategorias);
        return "index";
    }

    // --- NUEVAS RUTAS AÑADIDAS ---

    @GetMapping("/contacto")
    public String contacto() {
        return "contacto"; // Renderiza templates/contacto.html
    }

    @GetMapping("/nosotros")
    public String nosotros() {
        return "nosotros"; // Renderiza templates/nosotros.html
    }

    @PostMapping("/enviar-contacto") // Para que el formulario de contacto no de error 404
    public String procesarContacto() {
        // Aquí podrías añadir lógica para enviar un correo real
        return "redirect:/contacto?enviado";
    }
}