package com.techzone.peru.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techzone.peru.model.dto.AnalisisReviewDTO;
import com.techzone.peru.model.dto.DashboardStatsDTO;
import com.techzone.peru.model.entity.AiAnalysisResult;
import com.techzone.peru.model.entity.Review;
import com.techzone.peru.repository.AiAnalysisResultRepository;
import com.techzone.peru.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class BIService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private GeminiAnalisisService analisisService;

    @Autowired
    private AiAnalysisResultRepository aiAnalysisResultRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public AiAnalysisResult analizarYGuardarReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review no encontrada con ID: " + reviewId));

        if (review.getComentario() == null || review.getComentario().isBlank()) {
            throw new RuntimeException("La review no tiene ningún comentario para analizar.");
        }

        AnalisisReviewDTO resultadoAnalisis;
        try {
            resultadoAnalisis = analisisService.analizarResena(review.getComentario());
        } catch (Exception e) {
            throw new RuntimeException("Error al llamar a la API de análisis: " + e.getMessage(), e);
        }

        AiAnalysisResult analysisResult = new AiAnalysisResult();
        analysisResult.setReview(review);
        analysisResult.setModelName("gemini-pro");

        try {
            analysisResult.setRawResponse(objectMapper.writeValueAsString(resultadoAnalisis));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al convertir el resultado del análisis a JSON", e);
        }

        review.setAnalyzed(true);
        reviewRepository.save(review);

        return aiAnalysisResultRepository.save(analysisResult);
    }

    /**
     * NUEVO MÉTODO CORREGIDO: Prepara las estadísticas para el Dashboard.
     * Lee todos los resultados de la IA, parsea el JSON y cuenta los sentimientos.
     * @return Un DTO con las estadísticas agregadas.
     */
    public DashboardStatsDTO getDashboardStats() {
        List<AiAnalysisResult> todosLosAnalisis = aiAnalysisResultRepository.findAll();

        long positivos = 0;
        long negativos = 0;
        long neutrales = 0;

        for (AiAnalysisResult analisis : todosLosAnalisis) {
            try {
                // Leemos el string JSON de la base de datos
                String jsonResponse = analisis.getRawResponse();
                // Lo convertimos de vuelta a nuestro objeto DTO
                AnalisisReviewDTO resultado = objectMapper.readValue(jsonResponse, AnalisisReviewDTO.class);

                // Contamos según el sentimiento encontrado en el JSON
                if (Objects.equals("Positivo", resultado.sentimiento())) {
                    positivos++;
                } else if (Objects.equals("Negativo", resultado.sentimiento())) {
                    negativos++;
                } else {
                    neutrales++;
                }
            } catch (JsonProcessingException e) {
                // Si un JSON no se puede leer, lo ignoramos y lo reportamos en la consola
                System.err.println("Error al parsear el resultado de análisis con ID: " + analisis.getId());
            }
        }

        return new DashboardStatsDTO(todosLosAnalisis.size(), positivos, negativos, neutrales);
    }
}