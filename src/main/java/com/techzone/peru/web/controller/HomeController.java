package com.techzone.peru.web.controller;

import com.techzone.peru.model.entity.Categoria;
import com.techzone.peru.model.entity.Producto;
import com.techzone.peru.repository.CategoriaRepository;
import com.techzone.peru.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/") // Se activa en la URL raíz de la aplicación
    public String home(Model model) {
        // Obtenemos los datos de nuestros servicios/repositorios
        List<Producto> productosDestacados = productService.findAllProducts();
        List<Categoria> todasLasCategorias = categoriaRepository.findAll();

        // Añadimos los datos al modelo para que Thymeleaf pueda usarlos
        model.addAttribute("productos", productosDestacados);
        model.addAttribute("categorias", todasLasCategorias);

        return "index"; // Le decimos a Spring que renderice el archivo index.html
    }
}