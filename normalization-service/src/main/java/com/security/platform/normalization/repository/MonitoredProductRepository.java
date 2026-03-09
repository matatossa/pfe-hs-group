package com.security.platform.normalization.repository;

import com.security.platform.normalization.entity.MonitoredProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MonitoredProductRepository extends JpaRepository<MonitoredProduct, UUID> {

    List<MonitoredProduct> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);

    /** Used by NormalizationService to determine isRelevant. */
    boolean existsByNameIgnoreCaseAndActiveTrue(String name);

    Optional<MonitoredProduct> findByNameIgnoreCase(String name);
}
