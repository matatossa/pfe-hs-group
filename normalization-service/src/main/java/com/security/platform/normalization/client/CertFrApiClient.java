package com.security.platform.normalization.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CertFrApiClient {

    private final WebClient webClient;

    public CertFrApiClient() {
        this.webClient = WebClient.builder().build();
    }

    /**
     * Represents the parsed data we care about from the CERT-FR JSON.
     */
    public record CertFrAdvisoryData(List<String> affectedSystems, List<String> cves) {
    }

    /**
     * Fetches the official JSON representation of a CERT-FR advisory.
     * 
     * @param advisoryUrl Example:
     *                    https://www.cert.ssi.gouv.fr/avis/CERTFR-2026-AVI-0222/
     * @return Extracted affected systems and cves, or null if it fails.
     */
    public CertFrAdvisoryData getAdvisoryDetails(String advisoryUrl) {
        String jsonUrl = advisoryUrl;
        if (!jsonUrl.endsWith("/")) {
            jsonUrl += "/";
        }
        jsonUrl += "json/";

        try {
            JsonNode root = webClient.get()
                    .uri(jsonUrl)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (root == null)
                return null;

            List<String> systems = new ArrayList<>();
            JsonNode affectedNodes = root.path("affected_systems");
            if (affectedNodes.isArray()) {
                for (JsonNode node : affectedNodes) {
                    if (node.has("description")) {
                        systems.add(node.get("description").asText());
                    } else if (node.has("product") && node.get("product").has("name")) {
                        systems.add(node.get("product").get("name").asText());
                    }
                }
            }

            List<String> cves = new ArrayList<>();
            JsonNode cveNodes = root.path("cves");
            if (cveNodes.isArray()) {
                for (JsonNode node : cveNodes) {
                    if (node.has("name")) {
                        cves.add(node.get("name").asText().trim());
                    }
                }
            }

            return new CertFrAdvisoryData(systems, cves);

        } catch (Exception e) {
            log.error("Failed to fetch CERT-FR JSON from {}: {}", jsonUrl, e.getMessage());
            return null;
        }
    }
}
