package com.security.platform.normalization.controller;

import com.security.platform.normalization.dto.FeedItemDTO;
import com.security.platform.normalization.entity.Vulnerability;
import com.security.platform.normalization.repository.VulnerabilityRepository;
import com.security.platform.normalization.service.NormalizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NormalizationController {

    private final NormalizationService normalizationService;
    private final VulnerabilityRepository vulnerabilityRepository;

    // ─── Normalize Endpoints (REST fallback) ───────

    /**
     * POST /api/normalize — Normalize a single feed item.
     */
    @PostMapping("/normalize")
    public ResponseEntity<Vulnerability> normalize(@RequestBody FeedItemDTO item) {
        Vulnerability result = normalizationService.normalize(item);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/normalize/batch — Normalize a batch of feed items.
     */
    @PostMapping("/normalize/batch")
    public ResponseEntity<List<Vulnerability>> normalizeBatch(@RequestBody List<FeedItemDTO> items) {
        List<Vulnerability> results = normalizationService.normalizeBatch(items);
        return ResponseEntity.ok(results);
    }

    // ─── Vulnerability Query Endpoints ─────────────

    /**
     * GET /api/vulnerabilities — List all vulnerabilities (recent first, max 50).
     */
    @GetMapping("/vulnerabilities")
    public ResponseEntity<List<Vulnerability>> getAll() {
        return ResponseEntity.ok(vulnerabilityRepository.findAllByOrderByPublishedAtDesc());
    }

    /**
     * GET /api/vulnerabilities/relevant — Get only relevant vulnerabilities.
     */
    @GetMapping("/vulnerabilities/relevant")
    public ResponseEntity<List<Vulnerability>> getRelevant() {
        return ResponseEntity.ok(vulnerabilityRepository.findByIsRelevantTrueOrderByPublishedAtDesc());
    }

    /**
     * GET /api/vulnerabilities/{id} — Get a specific vulnerability.
     */
    @GetMapping("/vulnerabilities/{id}")
    public ResponseEntity<Vulnerability> getById(@PathVariable UUID id) {
        return vulnerabilityRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/vulnerabilities/search?product=xxx — Search by product.
     */
    @GetMapping("/vulnerabilities/search")
    public ResponseEntity<List<Vulnerability>> searchByProduct(@RequestParam String product) {
        return ResponseEntity.ok(vulnerabilityRepository.findByProductContainingIgnoreCase(product));
    }

    /**
     * GET /api/vulnerabilities/severity/{level} — Filter by severity.
     */
    @GetMapping("/vulnerabilities/severity/{level}")
    public ResponseEntity<List<Vulnerability>> getBySeverity(@PathVariable String level) {
        return ResponseEntity.ok(vulnerabilityRepository.findBySeverityOrderByPublishedAtDesc(level.toUpperCase()));
    }

    /**
     * GET /api/vulnerabilities/cve/{cveId} — Search by CVE ID.
     */
    @GetMapping("/vulnerabilities/cve/{cveId}")
    public ResponseEntity<List<Vulnerability>> getByCve(@PathVariable String cveId) {
        return ResponseEntity.ok(vulnerabilityRepository.findByCveId(cveId.toUpperCase()));
    }

    /**
     * GET /api/vulnerabilities/stats — Vulnerability statistics.
     */
    @GetMapping("/vulnerabilities/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long total = vulnerabilityRepository.count();
        long relevant = vulnerabilityRepository.findByIsRelevantTrueOrderByPublishedAtDesc().size();

        return ResponseEntity.ok(Map.of(
                "totalVulnerabilities", total,
                "relevantVulnerabilities", relevant,
                "irrelevantVulnerabilities", total - relevant));
    }
}
