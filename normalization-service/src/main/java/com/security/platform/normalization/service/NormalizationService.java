package com.security.platform.normalization.service;

import com.security.platform.normalization.client.FilteringClient;
import com.security.platform.normalization.dto.FeedItemDTO;
import com.security.platform.normalization.dto.FilterResultDTO;
import com.security.platform.normalization.entity.Vulnerability;
import com.security.platform.normalization.kafka.VulnerabilityProducer;
import com.security.platform.normalization.repository.VulnerabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class NormalizationService {

    private final FilteringClient filteringClient;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final VulnerabilityProducer vulnerabilityProducer;

    private static final Pattern CVE_PATTERN = Pattern.compile("CVE-\\d{4}-\\d{4,7}", Pattern.CASE_INSENSITIVE);
    private static final Pattern CVSS_PATTERN = Pattern.compile("(?:CVSS|score)[:\\s]*([0-9]+\\.?[0-9]*)",
            Pattern.CASE_INSENSITIVE);

    // ── CERT-FR French title: "Multiples vulnérabilités dans [les produits|le
    // noyau Linux de/d'] PRODUCT (date)"
    private static final Pattern CERTFR_TITLE = Pattern.compile(
            "vulnérabilit[eéè][s]?\\s+dans\\s+" +
                    "(?:les\\s+produits\\s+|le\\s+noyau\\s+Linux\\s+(?:de\\s+|d'))?(.+?)(?:\\s*\\(|$)",
            Pattern.CASE_INSENSITIVE);

    // ── Cyber.gc.ca English: "ALxx - Critical vulnerability affecting PRODUCT -
    // CVE-..."
    private static final Pattern CYBER_TITLE = Pattern.compile(
            "vulnerabilit(?:y|ies)\\s+(?:affecting|impacting)\\s+(.+?)(?:\\s*-|\\s*\\(|\\s+products?|$)",
            Pattern.CASE_INSENSITIVE);

    // ── Canonical product map: lowercase keyword → display name ──────────
    private static final Map<String, String> PRODUCT_MAP = new LinkedHashMap<>();
    static {
        // OS / Linux distributions
        p("ubuntu", "Ubuntu");
        p("focal fossa", "Ubuntu");
        p("jammy jellyfish", "Ubuntu");
        p("noble numbat", "Ubuntu");
        p("windows 10", "Windows");
        p("windows 11", "Windows");
        p("windows 7", "Windows");
        p("windows kernel", "Windows");
        p("microsoft windows", "Windows");
        p("windows server 2022", "Windows Server");
        p("windows server 2019", "Windows Server");
        p("windows server 2016", "Windows Server");
        p("windows server", "Windows Server");
        p("active directory", "Windows Server");
        p("internet information services", "Windows Server");
        p("android", "Android");
        p("google android", "Android");
        p("apple ios", "iOS");
        p("iphone os", "iOS");
        p("ios", "iOS");
        p("iphone", "iOS");
        p("ipad", "iOS");
        p("ipados", "iOS");
        p("almalinux", "ALMA");
        p("alma linux", "ALMA");
        // Network / Security appliances
        p("fortinet", "Fortinet");
        p("fortios", "Fortinet");
        p("fortigate", "Fortinet");
        p("fortiweb", "Fortinet");
        p("fortimanager", "Fortinet");
        p("fortianalyzer", "Fortinet");
        p("forticloud", "Fortinet");
        p("forticlient", "Fortinet");
        p("openvpn access server", "OpenVPN");
        p("openvpn", "OpenVPN");
        p("cisco catalyst sd-wan", "Cisco");
        p("cisco catalyst", "Cisco");
        p("cisco sd-wan", "Cisco");
        p("cisco asa", "Cisco");
        p("cisco firepower", "Cisco");
        p("cisco webex", "Cisco");
        p("cisco anyconnect", "Cisco");
        p("cisco ios", "Cisco");
        p("cisco nx-os", "Cisco");
        p("cisco", "Cisco");
        // Virtualisation / Cloud
        p("vmware horizon", "VMware");
        p("vmware aria", "VMware");
        p("vmware tanzu", "VMware");
        p("vmware tools", "VMware");
        p("vmware workstation", "VMware");
        p("vmware fusion", "VMware");
        p("vmware", "VMware");
        p("vsphere", "VMware");
        p("vcenter", "VMware");
        p("esxi", "VMware");
        // Microsoft Office / Productivity
        p("microsoft 365", "Microsoft Office");
        p("office 365", "Microsoft Office");
        p("microsoft office", "Microsoft Office");
        p("ms office", "Microsoft Office");
        p("microsoft outlook", "Microsoft Office");
        p("microsoft word", "Microsoft Office");
        p("microsoft excel", "Microsoft Office");
        p("sharepoint", "Microsoft Office");
        p("microsoft teams", "Microsoft Office");
        p("onedrive", "Microsoft Office");
        p("microsoft dynamics 365", "Microsoft Dynamics 365");
        p("dynamics 365", "Microsoft Dynamics 365");
        p("dynamics crm", "Microsoft Dynamics 365");
        p("dynamics nav", "Microsoft Dynamics 365");
        p("microsoft ax 2012", "Microsoft AX 2012");
        p("dynamics ax", "Microsoft AX 2012");
        p("microsoft azure linux", "Microsoft");
        p("microsoft azure", "Microsoft");
        p("microsoft edge", "Microsoft Edge");
        p("msedge", "Microsoft Edge");
        p("edge browser", "Microsoft Edge");
        p("les produits microsoft", "Microsoft");
        p("produits microsoft", "Microsoft");
        p("microsoft", "Microsoft"); // generic fallback
        p("google workspace", "Google Workspace");
        p("g suite", "Google Workspace");
        p("gmail", "Google Workspace");
        p("google meet", "Google Workspace");
        p("google chrome", "Google Chrome");
        p("chromium", "Google Chrome");
        p("chrome browser", "Google Chrome");
        p("mozilla firefox", "Mozilla Firefox");
        p("firefox esr", "Mozilla Firefox");
        p("les produits mozilla", "Mozilla Firefox");
        p("produits mozilla", "Mozilla Firefox");
        p("mozilla", "Mozilla Firefox");
        // Databases
        p("microsoft sql server", "SQL Server");
        p("sql server 2022", "SQL Server");
        p("sql server", "SQL Server");
        p("mssql", "SQL Server");
        p("oracle weblogic", "Oracle Database");
        p("oracle database", "Oracle Database");
        p("oracle db", "Oracle Database");
        p("oracle 19c", "Oracle Database");
        p("postgresql", "PostgreSQL");
        p("postgres", "PostgreSQL");
        // Monitoring / IT tools
        p("glpi", "GLPI");
        p("les produits vmware", "VMware");
        p("produits vmware", "VMware");
        p("les produits cisco", "Cisco");
        p("produits cisco", "Cisco");
        // Specific apps
        p("whatsapp business", "WhatsApp");
        p("whatsapp", "WhatsApp");
        p("shopify plus", "Shopify");
        p("shopify", "Shopify");
        p("keepassxc", "Keepass2");
        p("keepass2", "Keepass2");
        p("keepass", "Keepass2");
        p("openai", "OpenAI");
        p("chatgpt", "OpenAI");
        p("gpt-4", "OpenAI");
        p("sage x3", "Sage");
        p("sage erp", "Sage");
        p("sage 100", "Sage");
        p("sage", "Sage");
    }

    private static void p(String key, String val) {
        PRODUCT_MAP.put(key.toLowerCase(), val);
    }

    // ─────────────────────────────────────────────────────────────────────
    public Vulnerability normalize(FeedItemDTO item) {
        log.info("Normalizing: '{}'", truncate(item.getTitle(), 80));

        // 1. Extract product directly from title (primary — most reliable)
        String product = extractProductFromTitle(item.getTitle());

        // 2. Call filtering-service for CVE/keyword analysis
        FilterResultDTO filterResult;
        try {
            filterResult = filteringClient.filter(
                    item.getTitle(), item.getDescription(), item.getUrl(), item.getSource());
        } catch (Exception e) {
            log.warn("Filtering service unavailable; using title-only product detection: {}", e.getMessage());
            filterResult = FilterResultDTO.builder()
                    .isRelevant(!product.equals("Unknown"))
                    .relevanceScore(product.equals("Unknown") ? 0.0 : 0.6)
                    .detectedProducts(Collections.emptyList())
                    .cveIds(Collections.emptyList())
                    .matchedKeywords(Collections.emptyList())
                    .build();
        }

        // 3. If title extraction missed, fall back to filtering-service NLP
        if (product.equals("Unknown") && filterResult.getDetectedProducts() != null
                && !filterResult.getDetectedProducts().isEmpty()) {
            product = String.join(", ", filterResult.getDetectedProducts());
        }

        // 4. Relevant = known product found in title or description
        boolean isRelevant = !product.equals("Unknown");

        // 5. Extract CVE IDs
        List<String> cveIds = filterResult.getCveIds();
        if (cveIds == null || cveIds.isEmpty()) {
            cveIds = extractCveIds(item.getTitle() + " " + item.getDescription());
        }

        // 6. Severity (English + French keywords)
        String severity = determineSeverity(item.getTitle(), item.getDescription(), filterResult);

        // 7. CVSS score
        BigDecimal cvssScore = extractCvssScore(item.getTitle() + " " + item.getDescription());

        // 8. Persist — skip if this URL already exists (deduplication guard)
        if (item.getUrl() != null && !item.getUrl().isBlank()
                && vulnerabilityRepository.existsByUrl(item.getUrl())) {
            log.info("Skipping duplicate vulnerability URL: {}", item.getUrl());
            return vulnerabilityRepository.findByUrl(item.getUrl()).orElse(null);
        }

        Vulnerability vulnerability = Vulnerability.builder()
                .source(item.getSource())
                .product(product)
                .cveId(cveIds.isEmpty() ? null : String.join(", ", cveIds))
                .title(item.getTitle())
                .description(item.getDescription())
                .severity(severity)
                .cvssScore(cvssScore)
                .url(item.getUrl())
                .publishedAt(item.getPublishedAt() != null ? item.getPublishedAt() : LocalDateTime.now())
                .fetchedAt(LocalDateTime.now())
                .isRelevant(isRelevant)
                .relevanceScore(BigDecimal.valueOf(filterResult.getRelevanceScore()))
                .rawData(item.toString())
                .build();

        Vulnerability saved = vulnerabilityRepository.save(vulnerability);
        log.info("Saved: id={}, product='{}', cve={}, severity={}, relevant={}",
                saved.getId(), saved.getProduct(), saved.getCveId(), saved.getSeverity(), saved.getIsRelevant());

        if (isRelevant) {
            vulnerabilityProducer.publish(saved);
        }
        return saved;
    }

    public List<Vulnerability> normalizeBatch(List<FeedItemDTO> items) {
        log.info("Normalizing batch of {} items", items.size());
        List<Vulnerability> results = new ArrayList<>();
        int relevant = 0;
        for (FeedItemDTO item : items) {
            try {
                Vulnerability v = normalize(item);
                results.add(v);
                if (v.getIsRelevant())
                    relevant++;
            } catch (Exception e) {
                log.error("Failed to normalize '{}': {}", truncate(item.getTitle(), 60), e.getMessage());
            }
        }
        log.info("Batch complete: {}/{} processed, {} relevant", results.size(), items.size(), relevant);
        return results;
    }

    // ── Private Helpers ──────────────────────────────────────────────────

    /**
     * Extracts a canonical product name from the vulnerability title.
     * Supports CERT-FR French format and Cyber.gc.ca English format.
     */
    private String extractProductFromTitle(String title) {
        if (title == null)
            return "Unknown";
        String t = title.trim();

        // Try CERT-FR French pattern
        Matcher m = CERTFR_TITLE.matcher(t);
        if (m.find()) {
            String candidate = m.group(1).trim();
            String mapped = mapToProduct(candidate);
            if (!mapped.equals("Unknown")) {
                log.debug("CERT-FR title match: '{}' → '{}'", candidate, mapped);
                return mapped;
            }
        }

        // Try Cyber.gc.ca English pattern
        m = CYBER_TITLE.matcher(t);
        if (m.find()) {
            String candidate = m.group(1).trim();
            String mapped = mapToProduct(candidate);
            if (!mapped.equals("Unknown")) {
                log.debug("Cyber.gc.ca title match: '{}' → '{}'", candidate, mapped);
                return mapped;
            }
        }

        // Last resort: scan entire title for any known keyword
        return mapToProduct(t);
    }

    /**
     * Maps any text to a canonical product name using the longest matching key in
     * PRODUCT_MAP.
     */
    private String mapToProduct(String text) {
        if (text == null || text.isBlank())
            return "Unknown";
        String lower = text.toLowerCase();
        String bestKey = null;
        for (String key : PRODUCT_MAP.keySet()) {
            if (lower.contains(key)) {
                if (bestKey == null || key.length() > bestKey.length()) {
                    bestKey = key;
                }
            }
        }
        return bestKey != null ? PRODUCT_MAP.get(bestKey) : "Unknown";
    }

    private List<String> extractCveIds(String text) {
        if (text == null)
            return List.of();
        List<String> cves = new ArrayList<>();
        Matcher matcher = CVE_PATTERN.matcher(text);
        while (matcher.find())
            cves.add(matcher.group().toUpperCase());
        return cves.stream().distinct().toList();
    }

    private BigDecimal extractCvssScore(String text) {
        if (text == null)
            return null;
        Matcher matcher = CVSS_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                double score = Double.parseDouble(matcher.group(1));
                if (score >= 0 && score <= 10)
                    return BigDecimal.valueOf(score);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private String determineSeverity(String title, String description, FilterResultDTO fr) {
        String text = ((title != null ? title : "") + " " + (description != null ? description : "")).toLowerCase();

        // English severity keywords
        if (text.contains("critical") || text.contains("critique"))
            return "CRITICAL";
        if (text.contains("high") || text.contains("haute"))
            return "HIGH";
        if (text.contains("medium") || text.contains("moderate") || text.contains("moyenne"))
            return "MEDIUM";
        if (text.contains("low") || text.contains("faible"))
            return "LOW";

        // French impact-based severity inference (CERT-FR description patterns)
        if (text.contains("exécution de code arbitraire") || text.contains("remote code execution")
                || text.contains("injection sql") || text.contains("prise de contrôle à distance"))
            return "CRITICAL";
        if (text.contains("élévation de privilèges") || text.contains("privilege escalation")
                || text.contains("falsification de requêtes") || text.contains("ssrf")
                || text.contains("exécution de code"))
            return "HIGH";
        if (text.contains("déni de service") || text.contains("denial of service")
                || text.contains("atteinte à la confidentialité") || text.contains("atteinte à l'intégrité")
                || text.contains("contournement de la politique") || text.contains("injection de code")
                || text.contains("xss") || text.contains("cross-site"))
            return "MEDIUM";

        // Fall back to relevance score
        if (fr.getRelevanceScore() >= 0.8)
            return "HIGH";
        if (fr.getRelevanceScore() >= 0.5)
            return "MEDIUM";
        return "LOW";
    }

    private String truncate(String s, int maxLen) {
        if (s == null)
            return "";
        return s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
    }
}
