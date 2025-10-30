package com.techzone.peru.service;

import com.techzone.peru.model.dto.CartItemDTO; // Crearemos este DTO
import com.techzone.peru.model.entity.ProductoVariante;
import com.techzone.peru.repository.ProductoVarianteRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@SessionScope // ¡Muy importante! Crea una instancia de este bean por cada sesión de usuario
public class CarritoService {

    @Autowired
    private ProductoVarianteRepository varianteRepository;

    private Map<Long, CartItemDTO> items = new HashMap<>();

    // DTO Interno para el carrito
    // (Puedes moverlo a un archivo .java separado en model/dto si prefieres)
    public record CartTotalDTO(BigDecimal subtotal, String shipping, double discount, BigDecimal total) {}

    public void agregarItem(Long varianteId, int cantidad) {
        ProductoVariante variante = varianteRepository.findById(varianteId)
                .orElseThrow(() -> new RuntimeException("Variante no encontrada"));

        CartItemDTO item = items.get(varianteId);
        if (item != null) {
            item.setCantidad(item.getCantidad() + cantidad);
        } else {
            item = new CartItemDTO(
                    variante.getId(),
                    variante.getProducto().getNombre(),
                    cantidad,
                    variante.getPrecio(),
                    variante.getImagenPrincipalUrl()
            );
            items.put(varianteId, item);
        }
    }

    public void eliminarItem(Long varianteId) {
        items.remove(varianteId);
    }

    public void actualizarCantidad(Long varianteId, int cantidad) {
        CartItemDTO item = items.get(varianteId);
        if (item != null) {
            if (cantidad > 0) {
                item.setCantidad(cantidad);
            } else {
                eliminarItem(varianteId);
            }
        }
    }

    public List<CartItemDTO> getItems() {
        return new ArrayList<>(items.values());
    }

    public CartTotalDTO getTotal() {
        BigDecimal subtotal = items.values().stream()
                .map(item -> item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Simulación de envío y descuento
        String shipping = "Gratis";
        double discount = 0.0;
        BigDecimal total = subtotal; // Añadir lógica de envío/descuento si existe

        return new CartTotalDTO(subtotal, shipping, discount, total);
    }

    public void limpiarCarrito() {
        items.clear();
    }
}