package com.techzone.peru.web.rest;

import com.techzone.peru.model.entity.AiAnalysisResult;
import com.techzone.peru.service.BIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BITestController {

    @Autowired
    private BIService biService;

    @PostMapping("/test-analisis/review/{reviewId}") // Ruta más clara
    public ResponseEntity<String> testAnalisisReview(@PathVariable Long reviewId) {
        try {
            AiAnalysisResult resultado = biService.analizarYGuardarReview(reviewId);
            return ResponseEntity.ok("Análisis de Review completado y guardado con ID de resultado: " + resultado.getId());
        } catch (Exception e) {
            // Imprimimos el error en la consola para facilitar la depuración
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error durante el análisis: " + e.getMessage());
        }
    }
}