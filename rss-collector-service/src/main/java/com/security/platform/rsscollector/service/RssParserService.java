package com.security.platform.rsscollector.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.security.platform.rsscollector.entity.FeedSource;
import com.security.platform.rsscollector.entity.RawFeedEntry;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class RssParserService {

    @Value("${collector.timeout-seconds:30}")
    private int timeoutSeconds;

    /**
     * Parse a feed source and return raw entries.
     * Routes to RSS/Atom parser or HTML scraper based on feed_type.
     */
    public List<RawFeedEntry> parseFeed(FeedSource source) {
        try {
            if ("HTML".equalsIgnoreCase(source.getFeedType())) {
                return parseHtmlPage(source);
            } else {
                return parseRssFeed(source);
            }
        } catch (Exception e) {
            log.error("Failed to parse feed '{}' ({}): {}",
                    source.getName(), source.getUrl(), e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Parse standard RSS/Atom feeds using Rome library.
     * Uses HttpURLConnection with a proper User-Agent and timeout
     * to avoid 400/403 responses from government/security sites.
     */
    private List<RawFeedEntry> parseRssFeed(FeedSource source) throws Exception {
        log.info("Parsing RSS/Atom feed: {} ({})", source.getName(), source.getUrl());

        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new URL(source.getUrl()).openConnection();
        connection.setConnectTimeout(timeoutSeconds * 1000);
        connection.setReadTimeout(timeoutSeconds * 1000);
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (compatible; SecurityPlatformBot/1.0; RSS Reader)");
        connection.setRequestProperty("Accept",
                "application/rss+xml, application/atom+xml, application/xml, text/xml, */*");
        connection.setInstanceFollowRedirects(true);

        int status = connection.getResponseCode();
        if (status == java.net.HttpURLConnection.HTTP_MOVED_PERM
                || status == java.net.HttpURLConnection.HTTP_MOVED_TEMP
                || status == 307 || status == 308) {
            String location = connection.getHeaderField("Location");
            connection = (java.net.HttpURLConnection) new URL(location).openConnection();
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (compatible; SecurityPlatformBot/1.0; RSS Reader)");
            connection.setRequestProperty("Accept",
                    "application/rss+xml, application/atom+xml, application/xml, text/xml, */*");
        }

        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed;
        try (java.io.InputStream is = connection.getInputStream();
                XmlReader reader = new XmlReader(is)) {
            feed = input.build(reader);
        }

        List<RawFeedEntry> entries = new ArrayList<>();
        for (SyndEntry syndEntry : feed.getEntries()) {
            RawFeedEntry entry = RawFeedEntry.builder()
                    .title(syndEntry.getTitle() != null ? syndEntry.getTitle().trim() : "No Title")
                    .description(extractDescription(syndEntry))
                    .url(syndEntry.getLink() != null ? syndEntry.getLink().trim() : "")
                    .sourceName(source.getName())
                    .publishedAt(convertToLocalDateTime(syndEntry))
                    .feedSource(source)
                    .processed(false)
                    .fetchedAt(LocalDateTime.now())
                    .build();
            entries.add(entry);
        }

        log.info("Parsed {} entries from feed '{}'", entries.size(), source.getName());
        return entries;
    }

    /**
     * Scrape HTML page for security bulletins (used for DGSSI).
     * Extracts bulletin links from the DGSSI security bulletins page.
     */
    private List<RawFeedEntry> parseHtmlPage(FeedSource source) throws Exception {
        log.info("Scraping HTML page: {} ({})", source.getName(), source.getUrl());

        Document doc = Jsoup.connect(source.getUrl())
                .timeout(timeoutSeconds * 1000)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) SecurityPlatform/1.0")
                .get();

        List<RawFeedEntry> entries = new ArrayList<>();

        // DGSSI bulletins: look for bulletin links in the page
        // Target common patterns: article cards, list items with links
        Elements bulletinElements = doc.select(
                "article a[href], .view-content .views-row a[href], " +
                        ".bulletin a[href], .card a[href], .list-group-item a[href], " +
                        "table.views-table tbody tr, .field-content a[href]");

        if (bulletinElements.isEmpty()) {
            // Fallback: grab all links that look like security bulletins
            bulletinElements = doc.select("a[href*=bulletin], a[href*=securite], a[href*=security]");
        }

        for (Element el : bulletinElements) {
            String title;
            String link;

            if ("tr".equalsIgnoreCase(el.tagName())) {
                // Table row — extract from cells
                Elements cells = el.select("td");
                title = cells.size() > 0 ? cells.first().text().trim() : "";
                Element linkEl = el.selectFirst("a[href]");
                link = linkEl != null ? linkEl.absUrl("href") : "";
            } else if ("a".equalsIgnoreCase(el.tagName())) {
                title = el.text().trim();
                link = el.absUrl("href");
            } else {
                Element linkEl = el.selectFirst("a[href]");
                if (linkEl == null)
                    continue;
                title = linkEl.text().trim();
                link = linkEl.absUrl("href");
            }

            if (title.isEmpty() || link.isEmpty())
                continue;

            RawFeedEntry entry = RawFeedEntry.builder()
                    .title(title)
                    .description("") // HTML scraping — no description available inline
                    .url(link)
                    .sourceName(source.getName())
                    .publishedAt(LocalDateTime.now())
                    .feedSource(source)
                    .processed(false)
                    .fetchedAt(LocalDateTime.now())
                    .build();

            entries.add(entry);
        }

        log.info("Scraped {} entries from HTML page '{}'", entries.size(), source.getName());
        return entries;
    }

    /**
     * Extract description text from a SyndEntry, stripping HTML tags.
     */
    private String extractDescription(SyndEntry entry) {
        if (entry.getDescription() != null && entry.getDescription().getValue() != null) {
            // Strip HTML tags from description
            return Jsoup.parse(entry.getDescription().getValue()).text().trim();
        }
        // Fallback to contents
        if (entry.getContents() != null && !entry.getContents().isEmpty()) {
            return Jsoup.parse(entry.getContents().get(0).getValue()).text().trim();
        }
        return "";
    }

    /**
     * Convert SyndEntry date to LocalDateTime.
     */
    private LocalDateTime convertToLocalDateTime(SyndEntry entry) {
        if (entry.getPublishedDate() != null) {
            return entry.getPublishedDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }
        if (entry.getUpdatedDate() != null) {
            return entry.getUpdatedDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }
        return LocalDateTime.now();
    }
}
