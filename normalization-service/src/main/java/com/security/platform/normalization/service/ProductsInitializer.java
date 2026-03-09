package com.security.platform.normalization.service;

import com.security.platform.normalization.entity.MonitoredProduct;
import com.security.platform.normalization.repository.MonitoredProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds the monitored_products table on first startup with default products.
 * Runs only if the table is empty.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductsInitializer implements CommandLineRunner {

    private final MonitoredProductRepository repository;

    // Default products the organization monitors — employees can add/remove via UI
    private static final List<String> DEFAULT_PRODUCTS = List.of(
            "Ubuntu", "Windows", "Windows Server", "Android", "iOS", "ALMA",
            "Fortinet", "OpenVPN", "Cisco", "VMware",
            "Microsoft Office", "Microsoft Dynamics 365", "Microsoft AX 2012",
            "Microsoft Edge", "Microsoft", "Google Workspace", "Google Chrome",
            "Mozilla Firefox", "SQL Server", "Oracle Database", "PostgreSQL",
            "GLPI", "WhatsApp", "Shopify", "Keepass2", "OpenAI", "Sage");

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            log.info("Seeding monitored_products table with {} default products", DEFAULT_PRODUCTS.size());
            DEFAULT_PRODUCTS.forEach(name -> {
                try {
                    repository.save(MonitoredProduct.builder()
                            .name(name)
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .build());
                } catch (Exception e) {
                    log.warn("Could not seed product '{}': {}", name, e.getMessage());
                }
            });
        } else {
            log.info("monitored_products table already has {} entries, skipping seed",
                    repository.count());
        }
    }
}
