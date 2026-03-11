package com.security.platform.normalization.controller;

import com.security.platform.normalization.client.CertFrApiClient;
import com.security.platform.normalization.entity.MonitoredProduct;
import com.security.platform.normalization.entity.Vulnerability;
import com.security.platform.normalization.repository.MonitoredProductRepository;
import com.security.platform.normalization.repository.VulnerabilityRepository;
import com.security.platform.normalization.util.VersionComparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CRUD REST API for the organization's monitored product list.
 * Employees use the dashboard to call these endpoints via ProductsPage.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ProductsController {

    private final MonitoredProductRepository repository;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final CertFrApiClient certFrApiClient;

    /** GET /api/products — list all monitored products (active or not). */
    @GetMapping
    public List<MonitoredProduct> getAll() {
        return repository.findAllByOrderByNameAsc();
    }

    /** POST /api/products — add a new product to monitor. */
    @PostMapping
    public ResponseEntity<MonitoredProduct> add(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        name = name.trim();

        String version = body.get("version");
        if (version != null) {
            version = version.trim();
            if (version.isBlank())
                version = null;
        }

        if (repository.existsByNameIgnoreCase(name)) {
            return repository.findByNameIgnoreCase(name)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.badRequest().build());
        }
        MonitoredProduct product = MonitoredProduct.builder()
                .name(name)
                .version(version)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(repository.save(product));
    }

    /**
     * PUT /api/products/{id} — update an existing monitored product.
     * Currently only the version is updatable from the dashboard.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MonitoredProduct> update(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return repository.findById(id).map(product -> {
            String version = body.get("version");
            if (version != null) {
                version = version.trim();
                if (version.isBlank()) {
                    version = null;
                }
            }
            product.setVersion(version);
            MonitoredProduct saved = repository.save(product);

            // Recompute version matching for existing vulnerabilities of this product
            try {
                recalculateVersionMatches(saved);
            } catch (Exception e) {
                log.warn("Failed to recalculate version matches for product {}: {}", saved.getName(), e.getMessage());
            }

            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/products/{id} — remove a monitored product. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!repository.existsById(id))
            return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /** PUT /api/products/{id}/toggle — enable/disable a monitored product. */
    @PutMapping("/{id}/toggle")
    public ResponseEntity<MonitoredProduct> toggle(@PathVariable UUID id) {
        return repository.findById(id).map(p -> {
            p.setActive(!p.isActive());
            return ResponseEntity.ok(repository.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/products/recalculate-version-matches
     * Recomputes isVersionMatch for all active monitored products.
     * Helper endpoint so the dashboard user doesn't need to delete/recollect data.
     */
    @GetMapping("/recalculate-version-matches")
    public ResponseEntity<String> recalculateAll() {
        List<MonitoredProduct> products = repository.findAllByOrderByNameAsc();
        int total = 0;
        for (MonitoredProduct p : products) {
            if (!p.isActive())
                continue;
            try {
                recalculateVersionMatches(p);
                total++;
            } catch (Exception e) {
                log.warn("Failed to recalc version matches for product {}: {}", p.getName(), e.getMessage());
            }
        }
        return ResponseEntity.ok("Recalculated version matches for " + total + " active products.");
    }

    /**
     * Re-evaluates isVersionMatch for all relevant vulnerabilities of the given product.
     * This lets the "Version Match Only" filter behave correctly even for
     * vulnerabilities that were normalized before the version existed.
     */
    private void recalculateVersionMatches(MonitoredProduct product) {
        String name = product.getName();
        String userVersion = product.getVersion();

        // If no version is configured, reset to default "unknown, assume affected".
        if (userVersion == null || userVersion.isBlank()) {
            List<Vulnerability> vulns = vulnerabilityRepository
                    .findByProductIgnoreCaseAndIsRelevantTrue(name);
            for (Vulnerability v : vulns) {
                v.setIsVersionMatch(true);
                vulnerabilityRepository.save(v);
            }
            log.info("Cleared version for product '{}', reset {} vulnerabilities to isVersionMatch=true",
                    name, vulns.size());
            return;
        }

        List<Vulnerability> vulns = vulnerabilityRepository
                .findByProductIgnoreCaseAndIsRelevantTrue(name);

        int updated = 0;
        for (Vulnerability v : vulns) {
            boolean isVersionMatch = true;
            List<String> extractedSystems = new ArrayList<>();

            try {
                if ("CERT-FR".equalsIgnoreCase(v.getSource()) && v.getUrl() != null) {
                    CertFrApiClient.CertFrAdvisoryData data = certFrApiClient.getAdvisoryDetails(v.getUrl());
                    if (data != null && data.affectedSystems() != null && !data.affectedSystems().isEmpty()) {
                        extractedSystems.addAll(data.affectedSystems());
                    }
                    // Fallback to description if JSON is missing/empty
                    if (extractedSystems.isEmpty() && v.getDescription() != null && !v.getDescription().isBlank()) {
                        extractedSystems.add(v.getDescription());
                    }
                } else if (v.getDescription() != null && !v.getDescription().isBlank()) {
                    extractedSystems.add(v.getDescription());
                }

                if (!extractedSystems.isEmpty()) {
                    String affectedSystemsStr = String.join("\n", extractedSystems);
                    v.setAffectedSystems(affectedSystemsStr);

                    // Same semantics as in NormalizationService:
                    // assume NOT affected unless one system suggests the user
                    // version may be vulnerable.
                    boolean possiblyAffected = false;
                    for (String sys : extractedSystems) {
                        if (VersionComparator.isAffected(userVersion, sys)) {
                            possiblyAffected = true;
                            break;
                        }
                    }
                    isVersionMatch = possiblyAffected;
                }

                v.setIsVersionMatch(isVersionMatch);
                vulnerabilityRepository.save(v);
                updated++;
            } catch (Exception e) {
                log.warn("Failed to recompute version match for vulnerability {}: {}",
                        v.getId(), e.getMessage());
            }
        }

        log.info("Recalculated version matches for product '{}': {} vulnerabilities updated", name, updated);
    }
}
