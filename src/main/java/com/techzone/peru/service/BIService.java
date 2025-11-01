package com.techzone.peru.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techzone.peru.model.dto.AnalisisReviewDTO;
import com.techzone.peru.model.dto.AdvancedDashboardStatsDTO; // <-- CAMBIO
import com.techzone.peru.model.dto.ProductSentimentStatsDTO; // <-- NUEVO
import com.techzone.peru.model.dto.SentimentTopicDTO; // <-- NUEVO
import com.techzone.peru.model.entity.AiAnalysisResult;
import com.techzone.peru.model.entity.Producto; // <-- NUEVO
import com.techzone.peru.model.entity.Review;
import com.techzone.peru.repository.AiAnalysisResultRepository;
import com.techzone.peru.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- IMPORTANTE

import java.util.List;
import java.util.Map; // <-- NUEVO
import java.util.Objects;
import java.util.ArrayList; // <-- NUEVO
import java.util.HashMap; // <-- NUEVO

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

    // (El método analizarYGuardarReview se mantiene igual)
    @Transactional
    public AiAnalysisResult analizarYGuardarReview(Long reviewId) {
        // ... (sin cambios en este método)
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
     * MÉTODO MEJORADO: Prepara estadísticas avanzadas para el Dashboard.
     * Lee todos los resultados, los agrupa por producto y extrae los temas.
     * @return Un DTO con estadísticas agregadas y por producto.
     */
    @Transactional(readOnly = true) // <-- IMPORTANTE: Para evitar LazyInitializationException
    public AdvancedDashboardStatsDTO getDashboardStats() {
        List<AiAnalysisResult> todosLosAnalisis = aiAnalysisResultRepository.findAll();

        long positivos = 0;
        long negativos = 0;
        long neutrales = 0;

        // Mapa para agrupar los temas por ID de producto
        Map<Long, ProductSentimentStatsDTO> statsMap = new HashMap<>();

        for (AiAnalysisResult analisis : todosLosAnalisis) {
            try {
                String jsonResponse = analisis.getRawResponse();
                AnalisisReviewDTO resultado = objectMapper.readValue(jsonResponse, AnalisisReviewDTO.class);
                String sentimiento = resultado.sentimiento();

                // 1. Contar sentimientos generales
                if (Objects.equals("Positivo", sentimiento)) {
                    positivos++;
                } else if (Objects.equals("Negativo", sentimiento)) {
                    negativos++;
                } else {
                    neutrales++;
                }

                // 2. Obtener el producto (¡Gracias a @Transactional!)
                Review review = analisis.getReview();
                if (review == null || review.getProduct() == null) {
                    continue; // Saltar si la reseña o el producto fue eliminado
                }
                Producto producto = review.getProduct();

                // 3. Agrupar temas por producto
                ProductSentimentStatsDTO productStats = statsMap.computeIfAbsent(
                        producto.getId(),
                        (id) -> new ProductSentimentStatsDTO(producto.getNombre(), new ArrayList<>())
                );

                // 4. Añadir los temas de esta reseña a ese producto
                if (resultado.temas() != null) {
                    for (String tema : resultado.temas()) {
                        productStats.topics().add(new SentimentTopicDTO(tema, sentimiento));
                    }
                }

            } catch (JsonProcessingException e) {
                System.err.println("Error al parsear el resultado de análisis con ID: " + analisis.getId());
            } catch (Exception e) {
                // Captura de otras excepciones, ej. Lazy Loading si @Transactional falta
                System.err.println("Error procesando análisis: " + e.getMessage());
            }
        }

        return new AdvancedDashboardStatsDTO(
                todosLosAnalisis.size(),
                positivos,
                negativos,
                neutrales,
                statsMap.values() // Pasamos la colección de estadísticas por producto
        );
    }
}