package com.security.platform.rsscollector.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "raw_feed_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawFeedEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 1000)
    private String url;

    @Column(name = "source_name", length = 100)
    private String sourceName;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "fetched_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fetchedAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean processed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_source_id")
    private FeedSource feedSource;
}
