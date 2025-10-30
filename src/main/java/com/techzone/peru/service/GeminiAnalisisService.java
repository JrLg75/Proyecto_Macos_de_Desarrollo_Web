package com.techzone.peru.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import com.techzone.peru.model.dto.AnalisisReviewDTO;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class GeminiAnalisisService {

    private final String projectId = "tech-474010";
    private final String location = "us-central1"; // o la región que prefieras
    private final String modelName = "gemini-2.5-pro";

    public AnalisisReviewDTO analizarResena(String textoResena) throws IOException {
        // Inicializa el cliente de Vertex AI
        try (VertexAI vertexAi = new VertexAI(projectId, location)) {
            ObjectMapper mapper = new ObjectMapper();

            String prompt = String.format("""
                Analiza la siguiente reseña de un cliente para un producto de tecnología.
                Extrae los siguientes 3 puntos:
                1. 'sentimiento': El sentimiento general, que debe ser una de estas tres opciones: 'Positivo', 'Negativo' o 'Neutral'.
                2. 'temas': Una lista de los temas o características principales del producto mencionados, como "batería", "pantalla", "cámara", "precio", "envío", etc.
                3. 'resumen': Un resumen de la reseña en una sola frase concisa.

                Devuelve tu respuesta únicamente en formato JSON válido, sin comillas de bloque de código, ni texto o explicación adicional.
                
             

                Reseña a analizar: "%s"
                """, textoResena);

            GenerativeModel model = new GenerativeModel(modelName, vertexAi);
            GenerateContentResponse response = model.generateContent(prompt);

            // Extraemos el texto de la respuesta
            String jsonResponse = ResponseHandler.getText(response).replace("```json", "").replace("```", "").trim();
            System.out.println("Respuesta JSON de Gemini (Vertex AI): " + jsonResponse);

            return mapper.readValue(jsonResponse, AnalisisReviewDTO.class);

        } catch (Exception e) {
            e.printStackTrace();
            return new AnalisisReviewDTO("Error", List.of(), "No se pudo analizar la reseña con Gemini.");
        }
    }
}