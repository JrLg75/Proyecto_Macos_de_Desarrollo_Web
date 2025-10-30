package com.techzone.peru.repository;

import com.techzone.peru.model.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByEmail(String email);

    Optional<Cliente> findByEmail(String email);

}