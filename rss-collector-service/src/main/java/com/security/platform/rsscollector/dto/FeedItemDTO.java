package com.security.platform.rsscollector.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedItemDTO {

    private String title;
    private String description;
    private String url;
    private String source;
    private LocalDateTime publishedAt;
}
