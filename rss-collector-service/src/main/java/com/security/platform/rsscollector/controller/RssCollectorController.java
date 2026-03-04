package com.security.platform.rsscollector.controller;

import com.security.platform.rsscollector.dto.CollectionStatusDTO;
import com.security.platform.rsscollector.entity.FeedSource;
import com.security.platform.rsscollector.entity.RawFeedEntry;
import com.security.platform.rsscollector.repository.FeedSourceRepository;
import com.security.platform.rsscollector.repository.RawFeedEntryRepository;
import com.security.platform.rsscollector.service.FeedCollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RssCollectorController {

    private final FeedCollectorService feedCollectorService;
    private final FeedSourceRepository feedSourceRepository;
    private final RawFeedEntryRepository rawFeedEntryRepository;

    // ─── Trigger & Status ──────────────────────────────

    /**
     * POST /api/collect/trigger — Manual trigger for immediate collection.
     */
    @PostMapping("/collect/trigger")
    public ResponseEntity<CollectionStatusDTO> triggerCollection() {
        CollectionStatusDTO status = feedCollectorService.triggerCollect();
        return ResponseEntity.ok(status);
    }

    /**
     * GET /api/collect/status — Get last collection run status.
     */
    @GetMapping("/collect/status")
    public ResponseEntity<CollectionStatusDTO> getCollectionStatus() {
        return ResponseEntity.ok(feedCollectorService.getStatus());
    }

    // ─── Feed Sources ──────────────────────────────────

    /**
     * GET /api/feeds — List all feed sources.
     */
    @GetMapping("/feeds")
    public ResponseEntity<List<FeedSource>> getAllFeeds() {
        return ResponseEntity.ok(feedSourceRepository.findAll());
    }

    /**
     * GET /api/feeds/{id} — Get a specific feed source.
     */
    @GetMapping("/feeds/{id}")
    public ResponseEntity<FeedSource> getFeed(@PathVariable Integer id) {
        return feedSourceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/feeds — Add a new feed source.
     */
    @PostMapping("/feeds")
    public ResponseEntity<FeedSource> addFeed(@RequestBody FeedSource feedSource) {
        FeedSource saved = feedSourceRepository.save(feedSource);
        return ResponseEntity.ok(saved);
    }

    /**
     * PUT /api/feeds/{id}/toggle — Toggle a feed source active/inactive.
     */
    @PutMapping("/feeds/{id}/toggle")
    public ResponseEntity<FeedSource> toggleFeed(@PathVariable Integer id) {
        return feedSourceRepository.findById(id)
                .map(feed -> {
                    feed.setActive(!feed.getActive());
                    return ResponseEntity.ok(feedSourceRepository.save(feed));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── Feed Entries ──────────────────────────────────

    /**
     * GET /api/feeds/{id}/entries — Get raw entries from a specific source.
     */
    @GetMapping("/feeds/{id}/entries")
    public ResponseEntity<List<RawFeedEntry>> getFeedEntries(@PathVariable Integer id) {
        List<RawFeedEntry> entries = rawFeedEntryRepository.findByFeedSourceIdOrderByFetchedAtDesc(id);
        return ResponseEntity.ok(entries);
    }

    /**
     * GET /api/entries/unprocessed — Get all unprocessed entries.
     */
    @GetMapping("/entries/unprocessed")
    public ResponseEntity<List<RawFeedEntry>> getUnprocessedEntries() {
        return ResponseEntity.ok(rawFeedEntryRepository.findByProcessedFalse());
    }

    /**
     * GET /api/entries/stats — Get entry statistics.
     */
    @GetMapping("/entries/stats")
    public ResponseEntity<Map<String, Object>> getEntryStats() {
        long total = rawFeedEntryRepository.count();
        long unprocessed = rawFeedEntryRepository.findByProcessedFalse().size();

        return ResponseEntity.ok(Map.of(
                "totalEntries", total,
                "processedEntries", total - unprocessed,
                "unprocessedEntries", unprocessed,
                "feedSources", feedSourceRepository.count()));
    }
}
