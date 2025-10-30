package com.techzone.peru.web.controller;

import com.techzone.peru.model.dto.ProductDTO;
import com.techzone.peru.model.dto.VariantDTO;
import com.techzone.peru.model.entity.Producto;
import com.techzone.peru.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin/productos")
public class ProductAdminController {

    @Autowired
    private ProductService productService;

    /**
     * Muestra la lista de todos los productos base.
     */
    @GetMapping
    public String listarProductos(Model model) {
        List<Producto> listaProductos = productService.findAllProducts();
        model.addAttribute("productos", listaProductos);
        return "admin/productos/lista";
    }
    /**
     * Muestra el formulario para crear un nuevo producto base.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioProducto(Model model) {
        model.addAttribute("productDto", new ProductDTO(null, "", "", null));
        return "admin/productos/formulario";
    }

    /**
     * Guarda el producto base y redirige a la página de gestión de variantes.
     */
    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute("productDto") ProductDTO productDTO) {
        Producto productoGuardado = productService.saveBaseProduct(productDTO);
        // Redirigimos al usuario a la nueva página para que añada las variantes
        return "redirect:/admin/productos/" + productoGuardado.getId() + "/variantes";
    }

    /**
     * Muestra la página para ver/añadir variantes de un producto específico.
     */
    @GetMapping("/{id}/variantes")
    public String gestionarVariantes(@PathVariable Long id, Model model) {
        Producto producto = productService.findProductById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        model.addAttribute("producto", producto);
        model.addAttribute("variantDto", new VariantDTO(null, "", 0, null, "", "")); // Para el form de nueva variante
        return "admin/productos/variantes"; // Apunta a la nueva vista de variantes
    }

    /**
     * Guarda una nueva variante para un producto existente.
     */
    @PostMapping("/{id}/variantes/guardar")
    public String guardarVariante(@PathVariable Long id,
                                  @ModelAttribute("variantDto") VariantDTO variantDTO,
                                  @RequestParam("imagenFile") MultipartFile imagenFile) { // Recibimos el archivo
        productService.addVariantToProduct(id, variantDTO, imagenFile);
        return "redirect:/admin/productos/" + id + "/variantes";
    }


    /**
     * Muestra el formulario para editar un producto base existente.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        // Buscamos el producto y lo convertimos a su DTO para el formulario
        Producto producto = productService.findProductById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        ProductDTO productDto = new ProductDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getCategoria().getId()
        );

        model.addAttribute("productDto", productDto);
        return "admin/productos/formulario"; // Reutilizamos la misma vista del formulario
    }

    /**
     * Elimina una variante específica de un producto.
     */
    @PostMapping("/{productId}/variantes/eliminar/{variantId}")
    public String eliminarVariante(@PathVariable Long productId, @PathVariable Long variantId) {
        productService.deleteVariantById(variantId);
        // Redirigimos de vuelta a la página de gestión de variantes
        return "redirect:/admin/productos/" + productId + "/variantes";
    }
}