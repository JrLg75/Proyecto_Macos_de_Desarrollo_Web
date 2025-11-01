package com.techzone.peru.web.rest;

import com.techzone.peru.model.dto.ChatRequest;
import com.techzone.peru.model.dto.ChatResponse;
import com.techzone.peru.service.AdminAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin-agent") // La ruta que aseguramos
public class AdminAgentController {

    @Autowired
    private AdminAgentService adminAgentService;

    // Reutilizamos los DTOs del chatbot de cliente para simplicidad
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {

        String reply = adminAgentService.getInsights(request.message());

        // No devolvemos sugerencias de productos, solo la respuesta de texto
        ChatResponse response = new ChatResponse(reply, null);

        return ResponseEntity.ok(response);
    }
}