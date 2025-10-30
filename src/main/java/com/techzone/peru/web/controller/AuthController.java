package com.techzone.peru.web.controller;

import com.techzone.peru.model.dto.RegistroClienteDto;
import com.techzone.peru.model.entity.Cliente;
import com.techzone.peru.service.RegistroClientesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private RegistroClientesService registroClientesService;

    @GetMapping("/registro")
    public String showRegister(Model model) {
        model.addAttribute("form", new RegistroClienteDto("", "", "", "", ""));
        return "registro";
    }

    @PostMapping("/registro")
    public String doRegister(@ModelAttribute("form") RegistroClienteDto form, BindingResult br, Model model) {
        try {
            Cliente c = registroClientesService.registrarNuevoCliente(form);
            model.addAttribute("ok", true);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        }
    }
}


