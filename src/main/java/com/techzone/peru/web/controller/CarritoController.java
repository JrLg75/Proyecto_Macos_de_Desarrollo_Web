package com.techzone.peru.web.controller;

import com.techzone.peru.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @GetMapping("/carrito")
    public String verCarrito(Model model) {
        model.addAttribute("cartItems", carritoService.getItems());
        model.addAttribute("cartTotal", carritoService.getTotal());
        return "carrito"; // Renderiza carrito.html
    }

    @PostMapping("/carrito/agregar")
    public String agregarAlCarrito(@RequestParam("varianteId") Long varianteId,
                                   @RequestParam(value = "cantidad", defaultValue = "1") int cantidad) {

        // Aquí deberías verificar el stock antes de añadir, pero lo omitimos por simplicidad

        carritoService.agregarItem(varianteId, cantidad);
        return "redirect:/carrito";
    }

    @PostMapping("/carrito/actualizar")
    public String actualizarCarrito(@RequestParam("variantId") Long varianteId,
                                    @RequestParam("cantidad") int cantidad) {
        carritoService.actualizarCantidad(varianteId, cantidad);
        return "redirect:/carrito";
    }

    @PostMapping("/carrito/eliminar")
    public String eliminarDelCarrito(@RequestParam("variantId") Long varianteId) {
        carritoService.eliminarItem(varianteId);
        return "redirect:/carrito";
    }
}