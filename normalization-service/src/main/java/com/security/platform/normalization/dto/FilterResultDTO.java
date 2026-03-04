package com.security.platform.normalization.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterResultDTO {

    private boolean isRelevant;
    private double relevanceScore;
    private List<String> matchedKeywords;
    private List<String> detectedProducts;
    private List<String> cveIds;
}
