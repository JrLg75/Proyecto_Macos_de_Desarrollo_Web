package com.techzone.peru.web.controller;

import com.techzone.peru.model.dto.AdvancedDashboardStatsDTO;
import com.techzone.peru.service.BIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping; // Importar

@Controller
@RequestMapping("/admin") // Prefijo global para este controlador
public class DashboardController {

    @Autowired
    private BIService biService;

    // La ruta final será: /admin/dashboard
    @GetMapping("/dashboard")
    public String verDashboard(Model model) {
        AdvancedDashboardStatsDTO stats = biService.getDashboardStats();
        model.addAttribute("stats", stats);
        return "dashboard"; // El nombre del archivo HTML se mantiene igual (templates/dashboard.html)
    }
}