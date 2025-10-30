package com.techzone.peru.service;

import com.techzone.peru.model.entity.Pedido;
import com.techzone.peru.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    @Autowired
    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Consulta el estado de un pedido a partir de su número.
     * @param numeroPedido El número del pedido a consultar.
     * @return Una cadena de texto con el estado del pedido o un mensaje de error.
     */
    public String consultarEstadoPedido(String numeroPedido) {
        return pedidoRepository.findByNumeroPedido(numeroPedido)
                .map(pedido -> String.format("El estado de su pedido %s es: %s.", pedido.getNumeroPedido(), pedido.getEstado()))
                .orElse("Lo siento, no pude encontrar ningún pedido con el número " + numeroPedido + ". Por favor, verifique el número e intente de nuevo.");
    }
}