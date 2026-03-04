package com.security.platform.normalization.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.security.platform.normalization.dto.FeedItemDTO;
import com.security.platform.normalization.service.NormalizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FeedItemConsumer {

    private final NormalizationService normalizationService;
    private final ObjectMapper objectMapper;

    public FeedItemConsumer(@Lazy NormalizationService normalizationService) {
        this.normalizationService = normalizationService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Consume raw feed items from Kafka topic published by rss-collector-service.
     */
    @KafkaListener(topics = "${kafka.topic.raw-feeds:raw-feed-items}", groupId = "normalization-group")
    public void consume(String message) {
        try {
            FeedItemDTO item = objectMapper.readValue(message, FeedItemDTO.class);
            log.info("Consumed from Kafka: '{}'", truncate(item.getTitle(), 80));

            normalizationService.normalize(item);

        } catch (Exception e) {
            log.error("Failed to process Kafka message: {}", e.getMessage());
            log.debug("Raw message: {}", message);
        }
    }

    private String truncate(String s, int maxLen) {
        if (s == null)
            return "";
        return s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
    }
}
