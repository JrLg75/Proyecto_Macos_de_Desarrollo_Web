package com.techzone.peru.web.rest;

import com.techzone.peru.model.dto.ChatRequest;
import com.techzone.peru.model.dto.ChatResponse;
import com.techzone.peru.service.ChatAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    @Autowired
    private ChatAgentService chatAgentService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        // CAMBIO AQUÍ: Pasamos el mensaje Y el sessionId al servicio
        ChatResponse response = chatAgentService.handleMessageWithProducts(
                request.message(),
                request.sessionId()
        );
        return ResponseEntity.ok(response);
    }
}