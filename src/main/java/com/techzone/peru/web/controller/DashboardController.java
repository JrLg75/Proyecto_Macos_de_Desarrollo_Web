package com.techzone.peru.web.controller;

import com.techzone.peru.model.dto.DashboardStatsDTO;
import com.techzone.peru.service.BIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private BIService biService;

    @GetMapping("/dashboard")
    public String verDashboard(Model model) {
        DashboardStatsDTO stats = biService.getDashboardStats();
        model.addAttribute("stats", stats);
        return "dashboard";
    }
}