package com.security.platform.rsscollector.client;

import com.security.platform.rsscollector.dto.FeedItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Component
@Slf4j
public class NormalizationClient {

    private final WebClient webClient;

    public NormalizationClient(@Qualifier("normalizationWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Send a batch of feed items to normalization-service for processing.
     * POST /api/normalize/batch
     */
    public boolean sendBatch(List<FeedItemDTO> items) {
        if (items == null || items.isEmpty()) {
            log.debug("No items to send to normalization service");
            return true;
        }

        try {
            log.info("Sending batch of {} items to normalization-service", items.size());

            webClient.post()
                    .uri("/api/normalize/batch")
                    .bodyValue(items)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Successfully forwarded {} items to normalization-service", items.size());
            return true;

        } catch (WebClientResponseException e) {
            log.error("Normalization service returned error: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            return false;

        } catch (Exception e) {
            log.error("Failed to send batch to normalization-service: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Send a single feed item to normalization-service.
     * POST /api/normalize
     */
    public boolean sendItem(FeedItemDTO item) {
        try {
            webClient.post()
                    .uri("/api/normalize")
                    .bodyValue(item)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return true;

        } catch (Exception e) {
            log.error("Failed to send item to normalization-service: {}", e.getMessage());
            return false;
        }
    }
}
