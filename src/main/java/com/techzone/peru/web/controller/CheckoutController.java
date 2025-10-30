package com.techzone.peru.web.controller;

import com.techzone.peru.model.dto.CartItemDTO;
import com.techzone.peru.model.entity.Cliente;
import com.techzone.peru.model.entity.DetallePedido;
import com.techzone.peru.model.entity.Pedido;
import com.techzone.peru.model.entity.ProductoVariante;
import com.techzone.peru.repository.ClienteRepository;
import com.techzone.peru.repository.PedidoRepository;
import com.techzone.peru.repository.ProductoVarianteRepository;
import com.techzone.peru.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class CheckoutController {

    @Autowired
    private CarritoService carritoService;
    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ProductoVarianteRepository varianteRepository;

    // Muestra la página de checkout (que es la misma del carrito)
    @GetMapping("/checkout")
    public String mostrarCheckout(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login"; // Debe estar logueado para pagar
        }

        // Reutilizamos la vista del carrito como página de checkout
        model.addAttribute("cartItems", carritoService.getItems());
        model.addAttribute("cartTotal", carritoService.getTotal());
        return "carrito";
    }

    @Transactional // ¡Importante! Múltiples operaciones de BD
    @PostMapping("/checkout/simular") // Usamos una ruta específica para la simulación
    public String simularCompra(Authentication authentication, RedirectAttributes redirectAttributes) {

        // 1. Obtener al Cliente autenticado
        Cliente cliente = clienteRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no autenticado"));

        List<CartItemDTO> itemsDelCarrito = carritoService.getItems();
        if (itemsDelCarrito.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tu carrito está vacío.");
            return "redirect:/carrito";
        }

        // 2. Crear el Pedido
        Pedido nuevoPedido = new Pedido();
        nuevoPedido.setCliente(cliente);
        nuevoPedido.setFechaPedido(OffsetDateTime.now());
        nuevoPedido.setEstado("PENDIENTE");
        nuevoPedido.setNumeroPedido("TZ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        nuevoPedido.setDireccionEnvio("Av. Ficticia 123, Lima"); // Dirección de simulación

        List<DetallePedido> detalles = new ArrayList<>();
        BigDecimal totalCalculado = BigDecimal.ZERO;

        // 3. Convertir items del carrito en Detalles de Pedido
        for (CartItemDTO itemDTO : itemsDelCarrito) {
            ProductoVariante variante = varianteRepository.findById(itemDTO.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            // Aquí deberías verificar el stock, lo omitimos por la simulación

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(nuevoPedido);
            detalle.setVariante(variante);
            detalle.setCantidad(itemDTO.getCantidad());
            detalle.setPrecioUnitario(itemDTO.getPrecioUnitario());

            detalles.add(detalle);
            totalCalculado = totalCalculado.add(itemDTO.getSubtotal());
        }

        nuevoPedido.setDetalles(detalles);
        nuevoPedido.setTotal(totalCalculado);

        // 4. Guardar el Pedido (y sus detalles en cascada)
        pedidoRepository.save(nuevoPedido);

        // 5. Limpiar el carrito
        carritoService.limpiarCarrito();

        redirectAttributes.addFlashAttribute("successMessage",
                "¡Compra simulada con éxito! Tu número de pedido es: " + nuevoPedido.getNumeroPedido());

        return "redirect:/carrito"; // Redirigir a la pág. del carrito (que estará vacía)
    }
}