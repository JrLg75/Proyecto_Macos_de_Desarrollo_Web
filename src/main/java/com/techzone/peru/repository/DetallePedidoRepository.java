package com.techzone.peru.repository;

import com.techzone.peru.model.dto.TopSellerQueryResult; // <-- AÑADIR
import com.techzone.peru.model.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <-- AÑADIR
import org.springframework.data.repository.query.Param; // <-- AÑADIR

import java.time.OffsetDateTime; // <-- AÑADIR
import java.util.List; // <-- AÑADIR

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    /**
     * Busca las variantes de producto más vendidas (por cantidad) dentro de un rango de fechas.
     */
    @Query("SELECT new com.techzone.peru.model.dto.TopSellerQueryResult(d.variante.id, SUM(d.cantidad)) " +
            "FROM DetallePedido d " +
            "WHERE d.pedido.fechaPedido BETWEEN :start AND :end " +
            "GROUP BY d.variante.id " +
            "ORDER BY SUM(d.cantidad) DESC")
    List<TopSellerQueryResult> findTopSellersByDateRange(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );
}