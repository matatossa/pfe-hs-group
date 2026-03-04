package com.security.platform.rsscollector.service;

import com.security.platform.rsscollector.dto.CollectionStatusDTO;
import com.security.platform.rsscollector.dto.FeedItemDTO;
import com.security.platform.rsscollector.entity.FeedSource;
import com.security.platform.rsscollector.entity.RawFeedEntry;
import com.security.platform.rsscollector.kafka.FeedItemProducer;
import com.security.platform.rsscollector.repository.FeedSourceRepository;
import com.security.platform.rsscollector.repository.RawFeedEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedCollectorService {

    private final FeedSourceRepository feedSourceRepository;
    private final RawFeedEntryRepository rawFeedEntryRepository;
    private final RssParserService rssParserService;
    private final FeedItemProducer feedItemProducer;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicReference<CollectionStatusDTO> lastStatus = new AtomicReference<>(CollectionStatusDTO.builder()
            .status("IDLE")
            .build());

    /**
     * Scheduled feed collection — runs every 30 minutes (configurable via cron).
     */
    @Scheduled(cron = "${collector.cron}")
    public void scheduledCollect() {
        log.info("=== Scheduled feed collection triggered ===");
        collectAll();
    }

    /**
     * Manual trigger for immediate collection.
     * Returns the collection status after completion.
     */
    public CollectionStatusDTO triggerCollect() {
        log.info("=== Manual feed collection triggered ===");
        return collectAll();
    }

    /**
     * Get the last collection status.
     */
    public CollectionStatusDTO getStatus() {
        return lastStatus.get();
    }

    /**
     * Main collection logic:
     * 1. Fetch all active feed sources
     * 2. For each source: parse → deduplicate → save → publish to Kafka
     * 3. Update status
     */
    private CollectionStatusDTO collectAll() {
        if (!isRunning.compareAndSet(false, true)) {
            log.warn("Collection already in progress, skipping");
            return lastStatus.get();
        }

        List<FeedSource> activeSources = feedSourceRepository.findByActiveTrue();
        int totalCollected = 0;
        int totalForwarded = 0;
        int totalDuplicates = 0;

        log.info("Starting collection from {} active feed sources", activeSources.size());

        // Update status to RUNNING
        lastStatus.set(CollectionStatusDTO.builder()
                .totalSources((int) feedSourceRepository.count())
                .activeSources(activeSources.size())
                .lastRunTime(LocalDateTime.now())
                .status("RUNNING")
                .build());

        try {
            for (FeedSource source : activeSources) {
                try {
                    log.info("──── Collecting from: {} ({}) ────", source.getName(), source.getFeedType());

                    // 1. Parse the feed
                    List<RawFeedEntry> entries = rssParserService.parseFeed(source);

                    // 2. Deduplicate & save
                    List<RawFeedEntry> newEntries = new ArrayList<>();
                    for (RawFeedEntry entry : entries) {
                        if (entry.getUrl() != null && !entry.getUrl().isEmpty()
                                && rawFeedEntryRepository.existsByUrl(entry.getUrl())) {
                            totalDuplicates++;
                            continue;
                        }
                        RawFeedEntry saved = rawFeedEntryRepository.save(entry);
                        newEntries.add(saved);
                        totalCollected++;
                    }

                    log.info("Source '{}': {} new entries, {} duplicates skipped",
                            source.getName(), newEntries.size(), entries.size() - newEntries.size());

                    // 3. Publish to Kafka topic
                    if (!newEntries.isEmpty()) {
                        List<FeedItemDTO> dtos = newEntries.stream()
                                .map(this::toDTO)
                                .toList();

                        boolean sent = feedItemProducer.sendBatch(dtos);
                        if (sent) {
                            // Mark as processed
                            newEntries.forEach(e -> {
                                e.setProcessed(true);
                                rawFeedEntryRepository.save(e);
                            });
                            totalForwarded += dtos.size();
                            log.info("Published {} entries from '{}' to Kafka",
                                    dtos.size(), source.getName());
                        } else {
                            log.warn("Failed to publish entries from '{}' — will retry next cycle",
                                    source.getName());
                        }
                    }

                    // 4. Update last_pulled timestamp
                    source.setLastPulled(LocalDateTime.now());
                    feedSourceRepository.save(source);

                } catch (Exception e) {
                    log.error("Error collecting from '{}': {}", source.getName(), e.getMessage());
                }
            }

            // Try to forward any previously unprocessed entries
            retryUnprocessed();

            CollectionStatusDTO status = CollectionStatusDTO.builder()
                    .totalSources((int) feedSourceRepository.count())
                    .activeSources(activeSources.size())
                    .lastRunTime(LocalDateTime.now())
                    .entriesCollected(totalCollected)
                    .entriesForwarded(totalForwarded)
                    .duplicatesSkipped(totalDuplicates)
                    .status("COMPLETED")
                    .build();

            lastStatus.set(status);

            log.info("=== Collection complete: {} collected, {} published to Kafka, {} duplicates ===",
                    totalCollected, totalForwarded, totalDuplicates);

            return status;

        } catch (Exception e) {
            log.error("Fatal error during collection: {}", e.getMessage(), e);
            lastStatus.set(CollectionStatusDTO.builder()
                    .status("FAILED")
                    .lastRunTime(LocalDateTime.now())
                    .build());
            return lastStatus.get();

        } finally {
            isRunning.set(false);
        }
    }

    /**
     * Retry publishing unprocessed entries from previous failed runs.
     */
    private void retryUnprocessed() {
        List<RawFeedEntry> unprocessed = rawFeedEntryRepository.findByProcessedFalse();
        if (unprocessed.isEmpty())
            return;

        log.info("Retrying {} unprocessed entries from previous runs", unprocessed.size());

        List<FeedItemDTO> dtos = unprocessed.stream()
                .map(this::toDTO)
                .toList();

        boolean sent = feedItemProducer.sendBatch(dtos);
        if (sent) {
            unprocessed.forEach(e -> {
                e.setProcessed(true);
                rawFeedEntryRepository.save(e);
            });
            log.info("Successfully retried {} unprocessed entries via Kafka", unprocessed.size());
        }
    }

    /**
     * Convert a RawFeedEntry to a FeedItemDTO.
     */
    private FeedItemDTO toDTO(RawFeedEntry entry) {
        return FeedItemDTO.builder()
                .title(entry.getTitle())
                .description(entry.getDescription())
                .url(entry.getUrl())
                .source(entry.getSourceName())
                .publishedAt(entry.getPublishedAt())
                .build();
    }
}
