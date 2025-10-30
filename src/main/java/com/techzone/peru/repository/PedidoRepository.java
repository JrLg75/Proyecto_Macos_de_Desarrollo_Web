package com.techzone.peru.repository;

import com.techzone.peru.model.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /**
     * Busca un pedido por su número de pedido único.
     * El número de pedido es el que se le proporciona al cliente.
     * @param numeroPedido El identificador del pedido.
     * @return Un Optional que contiene el Pedido si se encuentra.
     */
    Optional<Pedido> findByNumeroPedido(String numeroPedido);
}