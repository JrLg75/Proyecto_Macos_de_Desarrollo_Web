package com.techzone.peru.service;

import com.techzone.peru.model.dto.SalesKpiDTO;
import com.techzone.peru.model.dto.TopSellerDTO;
import com.techzone.peru.model.dto.TopSellerQueryResult;
import com.techzone.peru.model.entity.Pedido;
import com.techzone.peru.model.entity.ProductoVariante;
import com.techzone.peru.repository.DetallePedidoRepository;
import com.techzone.peru.repository.PedidoRepository;
import com.techzone.peru.repository.ProductoVarianteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalesDashboardService {

    @Autowired
    private PedidoRepository pedidoRepository;
    @Autowired
    private DetallePedidoRepository detallePedidoRepository;
    @Autowired
    private ProductoVarianteRepository varianteRepository;

    @Transactional(readOnly = true)
    public SalesKpiDTO getSalesKpis(OffsetDateTime start, OffsetDateTime end) {

        // 1. Obtener todos los pedidos en el rango
        List<Pedido> pedidos = pedidoRepository.findAllByFechaPedidoBetween(start, end);

        // 2. Calcular KPIs básicos
        long numeroDePedidos = pedidos.size();
        BigDecimal ingresosTotales = pedidos.stream()
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ticketPromedio = BigDecimal.ZERO;
        if (numeroDePedidos > 0) {
            ticketPromedio = ingresosTotales.divide(BigDecimal.valueOf(numeroDePedidos), 2, RoundingMode.HALF_UP);
        }

        // 3. Obtener Top Sellers
        List<TopSellerQueryResult> topSellersResult = detallePedidoRepository.findTopSellersByDateRange(start, end);

        List<TopSellerDTO> topSellersDTOs = topSellersResult.stream()
                .limit(5) // Tomamos solo el Top 5
                .map(result -> {
                    // Buscamos la variante para obtener su nombre
                    ProductoVariante variante = varianteRepository.findById(result.varianteId())
                            .orElse(null); // Manejar nulos si la variante fue borrada

                    String nombre = "Producto Desconocido";
                    String sku = "N/A";

                    if (variante != null) {
                        nombre = variante.getProducto().getNombre();
                        sku = variante.getSku();
                    }

                    return new TopSellerDTO(nombre, sku, result.totalVendido());
                })
                .collect(Collectors.toList());

        // 4. Ensamblar DTO final
        return new SalesKpiDTO(ingresosTotales, numeroDePedidos, ticketPromedio, topSellersDTOs);
    }
}