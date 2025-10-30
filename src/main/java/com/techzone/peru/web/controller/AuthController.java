package com.techzone.peru.web.controller;
import com.techzone.peru.service.RegistroClientesService;
import com.techzone.peru.model.dto.RegistroClienteDto;
import com.techzone.peru.model.entity.Cliente;
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

    @GetMapping("/login")
    public String showLogin() {
        return "login"; // Esto le dice a Thymeleaf que renderice "templates/login.html"
    }

    @GetMapping("/registro")
    public String showRegister(Model model) {
        model.addAttribute("clienteDto", new RegistroClienteDto("", "", "", "", ""));
        return "registro";
    }

    @PostMapping("/registro")
    public String doRegister(@ModelAttribute("clienteDto") RegistroClienteDto clienteDto, BindingResult br, Model model) {
        try {
            Cliente c = registroClientesService.registrarNuevoCliente(clienteDto);
            // Redirigir con un parámetro de éxito es mejor que usar el modelo aquí.
            // Lo ajustaré para que funcione con la página de login.
            return "redirect:/login?registro=exitoso";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("clienteDto", clienteDto); // Devuelve el DTO para no borrar los campos
            return "registro"; // Devuelve a la página de registro
        }
    }
}