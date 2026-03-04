package com.security.platform.rsscollector.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.security.platform.rsscollector.dto.FeedItemDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class FeedItemProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.raw-feeds:raw-feed-items}")
    private String rawFeedsTopic;

    public FeedItemProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Publish a single feed item to the Kafka topic.
     */
    public boolean sendItem(FeedItemDTO item) {
        try {
            String json = objectMapper.writeValueAsString(item);
            String key = item.getSource() + "-" + item.getUrl().hashCode();

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(rawFeedsTopic, key, json);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish to Kafka topic '{}': {}",
                            rawFeedsTopic, ex.getMessage());
                } else {
                    log.debug("Published to Kafka: partition={}, offset={}",
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });

            return true;

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize FeedItemDTO: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Publish a batch of feed items to the Kafka topic.
     */
    public boolean sendBatch(List<FeedItemDTO> items) {
        if (items == null || items.isEmpty()) {
            log.debug("No items to publish to Kafka");
            return true;
        }

        log.info("Publishing batch of {} items to Kafka topic '{}'", items.size(), rawFeedsTopic);

        int successCount = 0;
        for (FeedItemDTO item : items) {
            if (sendItem(item)) {
                successCount++;
            }
        }

        log.info("Successfully published {}/{} items to Kafka", successCount, items.size());
        return successCount == items.size();
    }
}
