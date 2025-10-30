package com.techzone.peru.repository;

import com.techzone.peru.model.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findTop5ByNombreContainingIgnoreCase(String nombre);
}