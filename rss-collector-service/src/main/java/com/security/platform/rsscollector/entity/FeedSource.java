package com.security.platform.rsscollector.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "feed_sources")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 500)
    private String url;

    @Column(name = "feed_type", nullable = false, length = 50)
    @Builder.Default
    private String feedType = "RSS";

    @Column(name = "last_pulled")
    private LocalDateTime lastPulled;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
