package com.techzone.peru.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.techzone.peru.model.dto.AdvancedDashboardStatsDTO;
import com.techzone.peru.model.dto.SalesKpiDTO;
import com.techzone.peru.model.entity.ConversacionChatbot; // <-- Importar
import com.techzone.peru.repository.ConversacionChatbotRepository; // <-- Importar
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest; // <-- Importar
import org.springframework.data.domain.Sort; // <-- Importar
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminAgentService {

    @Autowired
    private VertexAI vertexAI;
    @Autowired
    private BIService biService;
    @Autowired
    private SalesDashboardService salesService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ConversacionChatbotRepository chatRepo; // <-- NUEVO: Repositorio de chats

    @Value("${vertex.model.name:gemini-2.5-pro}")
    private String modelName;

    public String getInsights(String adminQuery) {

        try {
            // 1. RECOLECTAR DATOS
            // a) Sentimiento
            AdvancedDashboardStatsDTO sentimentStats = biService.getDashboardStats();
            String sentimentContext = objectMapper.writeValueAsString(sentimentStats);

            // b) Ventas
            OffsetDateTime end = OffsetDateTime.now();
            OffsetDateTime start = end.minusDays(30);
            SalesKpiDTO salesKpis = salesService.getSalesKpis(start, end);
            String salesContext = objectMapper.writeValueAsString(salesKpis);

            // c) NUEVO: Últimos chats de clientes (ej. últimos 20 mensajes)
            List<ConversacionChatbot> recentChats = chatRepo.findAll(
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "fecha"))
            ).getContent();

            String chatHistoryContext = recentChats.stream()
                    .map(c -> "- Cliente: " + c.getMensajeUsuario() + " | Bot respondió: " + c.getRespuestaBot())
                    .collect(Collectors.joining("\n"));

            // 2. CONSTRUIR EL PROMPT COMPLETO
            String prompt = String.format(
                    """
                    Eres un analista de negocios senior para TechZone Perú.
                    Tu objetivo es dar respuestas estratégicas al administrador.
                    
                    PREGUNTA DEL ADMIN: "%s"
                    
                    --- FUENTES DE DATOS ---
                    
                    1. [SENTIMIENTO DE RESEÑAS] (Opiniones post-compra):
                    %s
                    
                    2. [VENTAS] (Últimos 30 días):
                    %s
                    
                    3. [BUSQUEDAS EN TIEMPO REAL] (Lo que los clientes están preguntando AHORA en el chat):
                    %s
                    
                    --- FIN DATOS ---
                    
                    Instrucciones:
                    - Cruza la información. Ejemplo: Si buscan mucho un producto en el chat pero no hay ventas, avisa de posible falta de stock o precio alto.
                    - Responde en español, sé directo y profesional.
                    """,
                    adminQuery,
                    sentimentContext,
                    salesContext,
                    chatHistoryContext
            );

            // 3. LLAMAR A LA IA
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
            GenerateContentResponse response = model.generateContent(prompt);
            String rawResponse = ResponseHandler.getText(response);

            return (rawResponse == null || rawResponse.isBlank())
                    ? "No pude generar una respuesta."
                    : rawResponse.trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error al analizar los datos: " + e.getMessage();
        }
    }
}