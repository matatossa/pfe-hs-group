package com.security.platform.rsscollector.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionStatusDTO {

    private int totalSources;
    private int activeSources;
    private LocalDateTime lastRunTime;
    private int entriesCollected;
    private int entriesForwarded;
    private int duplicatesSkipped;
    private String status; // IDLE, RUNNING, COMPLETED, FAILED
}
