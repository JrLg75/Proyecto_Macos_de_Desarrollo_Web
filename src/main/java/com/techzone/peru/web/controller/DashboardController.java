package com.techzone.peru.web.controller;

import com.techzone.peru.model.dto.AdvancedDashboardStatsDTO; // <-- CAMBIO
import com.techzone.peru.service.BIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private BIService biService;

    // La ruta "/dashboard" se mantiene, pero ahora apunta a la vista mejorada
    @GetMapping("/dashboard")
    public String verDashboard(Model model) {
        // El DTO ahora contiene los contadores Y los insights por producto
        AdvancedDashboardStatsDTO stats = biService.getDashboardStats();
        model.addAttribute("stats", stats);
        return "dashboard"; // Renderiza dashboard.html
    }
}