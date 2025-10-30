package com.techzone.peru.repository;

import com.techzone.peru.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Encuentra todas las reseñas que aún no han sido analizadas por la IA.
     * @return Una lista de Reviews pendientes de análisis.
     */
    List<Review> findByIsAnalyzedFalse();
}