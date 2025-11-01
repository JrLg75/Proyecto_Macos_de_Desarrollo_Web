package com.techzone.peru.web.controller;

import com.techzone.peru.model.dto.SalesKpiDTO;
import com.techzone.peru.service.SalesDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.OffsetDateTime;

@Controller
public class SalesDashboardController {

    @Autowired
    private SalesDashboardService salesService;

    @GetMapping("/admin/ventas") // Nueva ruta para el dashboard de ventas
    public String showSalesDashboard(Model model) {

        // Por defecto, mostramos los últimos 30 días
        OffsetDateTime end = OffsetDateTime.now();
        OffsetDateTime start = end.minusDays(30);

        SalesKpiDTO kpis = salesService.getSalesKpis(start, end);

        model.addAttribute("kpis", kpis);
        model.addAttribute("fechaInicio", start);
        model.addAttribute("fechaFin", end);

        return "admin/sales_dashboard"; // Nueva plantilla HTML
    }
}