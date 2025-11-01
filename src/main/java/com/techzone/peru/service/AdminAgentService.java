package com.techzone.peru.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.techzone.peru.model.dto.AdvancedDashboardStatsDTO;
import com.techzone.peru.model.dto.SalesKpiDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class AdminAgentService {

    @Autowired
    private VertexAI vertexAI; // El bean de IA que ya configuramos
    @Autowired
    private BIService biService; // Nuestro servicio de Fase 1
    @Autowired
    private SalesDashboardService salesService; // Nuestro servicio de Fase 2
    @Autowired
    private ObjectMapper objectMapper; // Para convertir los DTOs a JSON (texto)

    @Value("${vertex.model.name:gemini-2.5-pro}")
    private String modelName;

    public String getInsights(String adminQuery) {

        // 1. RECOLECTAR DATOS (Lógica de Backend)
        // Obtenemos los datos de sentimiento (Fase 1)
        AdvancedDashboardStatsDTO sentimentStats = biService.getDashboardStats();

        // Obtenemos los datos de ventas de los últimos 30 días (Fase 2)
        OffsetDateTime end = OffsetDateTime.now();
        OffsetDateTime start = end.minusDays(30);
        SalesKpiDTO salesKpis = salesService.getSalesKpis(start, end);

        try {
            // 2. CONVERTIR DATOS A TEXTO (JSON)
            // La IA no entiende objetos Java, pero entiende JSON perfectamente.
            String sentimentContext = objectMapper.writeValueAsString(sentimentStats);
            String salesContext = objectMapper.writeValueAsString(salesKpis);

            // 3. CONSTRUIR EL PROMPT
            String prompt = String.format(
                    """
                    Eres "Insight-Bot", un analista de negocios senior para un E-Commerce de tecnología.
                    Tu jefe (el administrador) te acaba de hacer una pregunta.
                    
                    Utiliza el siguiente CONTEXTO DE DATOS para formular tu respuesta.
                    Nunca inventes datos. Basa tu respuesta únicamente en este contexto.
                    
                    PREGUNTA DEL JEFE:
                    "%s"
                    
                    --- CONTEXTO DE DATOS ---
                    
                    1. DATOS DE SENTIMIENTO (de reseñas de clientes):
                    %s
                    
                    2. DATOS DE VENTAS (últimos 30 días):
                    %s
                    
                    --- FIN DEL CONTEXTO ---
                    
                    Sintetiza la información relevante de ambos contextos y responde a la pregunta del jefe
                    de forma clara, profesional y en lenguaje natural (español).
                    """,
                    adminQuery,
                    sentimentContext,
                    salesContext
            );

            // 4. LLAMAR A LA IA (Lógica de IA)
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
            GenerateContentResponse response = model.generateContent(prompt);
            String rawResponse = ResponseHandler.getText(response);

            return (rawResponse == null || rawResponse.isBlank())
                    ? "No pude procesar la solicitud con los datos actuales."
                    : rawResponse.trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "Error al recolectar o analizar los datos: " + e.getMessage();
        }
    }
}