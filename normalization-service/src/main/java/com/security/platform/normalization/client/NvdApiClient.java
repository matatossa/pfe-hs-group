package com.security.platform.normalization.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class NvdApiClient {

    private final WebClient webClient;
    private static final String NVD_URL = "https://services.nvd.nist.gov/rest/json/cves/2.0";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public NvdApiClient() {
        this.webClient = WebClient.builder()
                .baseUrl(NVD_URL)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
                .build();
    }

    /**
     * Queries NIST NVD for CVEs matching a specific product and published within a
     * time window.
     * Respects the public API rate limit (5 req / 30 sec without an API key).
     */
    public List<String> getRecentCvesForProduct(String product, LocalDateTime publishedAt) {
        if (product == null || product.equals("Unknown") || publishedAt == null) {
            return List.of();
        }

        // Query NVD for CVEs published between (publishedAt - 7 days) and (publishedAt
        // + 2 days)
        // This accounts for timezone differences and delays in NVD publishing
        String startDate = publishedAt.minusDays(7).format(DATE_FORMAT);
        String endDate = publishedAt.plusDays(2).format(DATE_FORMAT);

        log.info("Querying NIST NVD API for keyword '{}', between {} and {}", product, startDate, endDate);

        try {
            Map response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("keywordSearch", product)
                            .queryParam("pubStartDate", startDate)
                            .queryParam("pubEndDate", endDate)
                            .build())
                    .header("User-Agent", "SecureFeedPlatform/1.0")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("vulnerabilities")) {
                List<Map<String, Object>> vulns = (List<Map<String, Object>>) response.get("vulnerabilities");
                List<String> cveIds = new ArrayList<>();

                for (Map<String, Object> vulnMap : vulns) {
                    Map<String, Object> cveObj = (Map<String, Object>) vulnMap.get("cve");
                    if (cveObj != null && cveObj.containsKey("id")) {
                        cveIds.add((String) cveObj.get("id"));
                    }
                }
                log.info("NIST NVD Enrichment: Found {} CVEs for product '{}'", cveIds.size(), product);
                return cveIds;
            }
        } catch (WebClientResponseException e) {
            log.warn("NIST NVD API error: HTTP {} - {}", e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            log.warn("Failed to reach NIST NVD API: {}", e.getMessage());
        }

        return List.of();
    }
}
