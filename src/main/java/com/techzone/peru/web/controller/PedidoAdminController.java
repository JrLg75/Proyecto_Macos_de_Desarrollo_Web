package com.techzone.peru.web.controller;

import com.techzone.peru.model.entity.Pedido;
import com.techzone.peru.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/pedidos")
public class PedidoAdminController {

    @Autowired
    private PedidoRepository pedidoRepository;

    // Listar todos los pedidos (descendente por fecha)
    @GetMapping
    public String listarPedidos(Model model) {
        List<Pedido> pedidos = pedidoRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaPedido"));
        model.addAttribute("pedidos", pedidos);
        return "admin/pedidos/gestion-pedidos";
    }

    // Ver detalle de un pedido
    @GetMapping("/{id}")
    public String verDetallePedido(@PathVariable Long id, Model model) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        model.addAttribute("pedido", pedido);
        return "admin/pedidos/detalles-pedidos";
    }
}