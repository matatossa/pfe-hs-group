package com.security.platform.normalization.client;

import com.security.platform.normalization.dto.FilterResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Component
@Slf4j
public class FilteringClient {

    private final WebClient webClient;

    public FilteringClient(@Qualifier("filteringWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Call the filtering-service to check if a feed item is relevant.
     * POST /filter with { title, description, url, source }
     */
    public FilterResultDTO filter(String title, String description, String url, String source) {
        try {
            Map<String, String> request = Map.of(
                    "title", title != null ? title : "",
                    "description", description != null ? description : "",
                    "url", url != null ? url : "",
                    "source", source != null ? source : "");

            FilterResultDTO result = webClient.post()
                    .uri("/filter")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(FilterResultDTO.class)
                    .block();

            if (result != null) {
                log.debug("Filter result: relevant={}, score={}, products={}",
                        result.isRelevant(), result.getRelevanceScore(), result.getDetectedProducts());
            }

            return result;

        } catch (WebClientResponseException e) {
            log.error("Filtering service error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return defaultResult();

        } catch (Exception e) {
            log.error("Failed to call filtering service: {}", e.getMessage());
            return defaultResult();
        }
    }

    /**
     * Default result when filtering service is unavailable — mark as potentially
     * relevant.
     */
    private FilterResultDTO defaultResult() {
        return FilterResultDTO.builder()
                .isRelevant(true)
                .relevanceScore(0.5)
                .matchedKeywords(java.util.List.of())
                .detectedProducts(java.util.List.of())
                .cveIds(java.util.List.of())
                .build();
    }
}
