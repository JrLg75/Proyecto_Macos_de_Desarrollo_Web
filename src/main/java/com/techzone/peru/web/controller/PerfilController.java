package com.techzone.peru.web.controller;

import com.techzone.peru.model.entity.Cliente;
import com.techzone.peru.model.entity.Pedido;
import com.techzone.peru.repository.ClienteRepository;
import com.techzone.peru.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Método auxiliar para obtener el cliente logueado
    private Cliente getClienteActual(Authentication auth) {
        if (auth == null) return null;
        return clienteRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }

    @GetMapping
    public String verPerfil(Model model, Authentication auth) {
        Cliente cliente = getClienteActual(auth);
        model.addAttribute("cliente", cliente);
        return "perfil"; // Renderiza perfil.html
    }

    @PostMapping("/actualizar")
    public String actualizarPerfil(@ModelAttribute Cliente clienteForm,
                                   @RequestParam(required = false) String newPassword,
                                   Authentication auth) {
        Cliente clienteDb = getClienteActual(auth);

        // Actualizar datos básicos
        clienteDb.setNombre(clienteForm.getNombre());
        clienteDb.setApellido(clienteForm.getApellido());
        clienteDb.setTelefono(clienteForm.getTelefono());
        //clienteDb.setDireccion(clienteForm.getDireccion()); // Asegúrate de tener este campo en tu entidad Cliente

        // Cambio de contraseña
        if (newPassword != null && !newPassword.isBlank()) {
            clienteDb.setPasswordHash(passwordEncoder.encode(newPassword));
        }

        clienteRepository.save(clienteDb);
        return "redirect:/perfil?exito";
    }

    // --- NUEVO MÉTODO PARA MIS ÓRDENES ---
    @GetMapping("/ordenes")
    public String misOrdenes(Model model, Authentication auth) {
        Cliente cliente = getClienteActual(auth);

        // Usamos el nuevo método del repositorio
        List<Pedido> pedidos = pedidoRepository.findByClienteOrderByFechaPedidoDesc(cliente);

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("cliente", cliente); // Para el nombre en el navbar
        return "perfil/ordenes";
    }
}