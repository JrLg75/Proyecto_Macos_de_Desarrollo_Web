package com.techzone.peru.repository;

import com.techzone.peru.model.entity.DashboardKpi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DashboardKpiRepository extends JpaRepository<DashboardKpi, Integer> {
    Optional<DashboardKpi> findByKpiName(String kpiName);
}