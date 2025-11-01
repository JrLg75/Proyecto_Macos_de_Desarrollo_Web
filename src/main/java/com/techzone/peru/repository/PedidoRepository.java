package com.techzone.peru.repository;

import com.techzone.peru.model.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByNumeroPedido(String numeroPedido);

    // --- ¡AÑADIR ESTE MÉTODO! ---
    List<Pedido> findAllByFechaPedidoBetween(OffsetDateTime start, OffsetDateTime end);
}