package com.security.platform.normalization.controller;

import com.security.platform.normalization.entity.MonitoredProduct;
import com.security.platform.normalization.repository.MonitoredProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
public class ProductsController {

    private final MonitoredProductRepository repository;

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
        if (repository.existsByNameIgnoreCase(name)) {
            return repository.findByNameIgnoreCase(name)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.badRequest().build());
        }
        MonitoredProduct product = MonitoredProduct.builder()
                .name(name)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(repository.save(product));
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
}
