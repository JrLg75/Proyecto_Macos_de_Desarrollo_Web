package com.techzone.peru.web.rest;

import com.techzone.peru.model.dto.ChatRequest;
import com.techzone.peru.model.dto.ChatResponse;
import com.techzone.peru.service.ChatAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private final ChatAgentService chatAgentService;

    @Autowired
    public AgentController(ChatAgentService chatAgentService) {
        this.chatAgentService = chatAgentService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = chatAgentService.handleMessageWithProducts(request.message());
        return ResponseEntity.ok(response);
    }
}


