package main.java.com.audit.main.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Publie des événements sur Kafka.
 *
 * Topics :
 *   audit-events         → lifecycle : AUDIT_CREATED, SCORE_UPDATED, AUDIT_COMPLETED
 *   jira-ticket-requests → JIRA_TICKET_REQUESTED vers jira-service
 *
 * Toutes les méthodes sont @Async pour ne pas bloquer le thread HTTP.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${audit.kafka.topic.events}") private String eventsTopic;
    @Value("${audit.kafka.topic.jira}")   private String jiraTopic;

    @Async
    public void auditCreated(UUID auditId, String org) {
        send(eventsTopic, auditId.toString(), event("AUDIT_CREATED",
                "audit_id", auditId.toString(),
                "organization", org));
    }

    @Async
    public void scoreUpdated(UUID auditId, String org, double score, String tier) {
        send(eventsTopic, auditId.toString(), event("SCORE_UPDATED",
                "audit_id", auditId.toString(),
                "organization", org,
                "global_score", score,
                "maturity_tier", tier));
    }

    @Async
    public void auditCompleted(UUID auditId, String org, double score, String tier) {
        send(eventsTopic, auditId.toString(), event("AUDIT_COMPLETED",
                "audit_id", auditId.toString(),
                "organization", org,
                "global_score", score,
                "maturity_tier", tier));
    }

    @Async
    public void requestJiraTicket(UUID auditId, String org, String scId,
                                   String title, String priority, String description) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event_type",     "JIRA_TICKET_REQUESTED");
        payload.put("audit_id",       auditId.toString());
        payload.put("organization",   org);
        payload.put("subcategory_id", scId);
        payload.put("title",          title);
        payload.put("priority",       priority);
        payload.put("description",    description);
        payload.put("project",        "CYBER");
        payload.put("timestamp",      LocalDateTime.now().toString());
        send(jiraTopic, auditId.toString(), payload);
    }

    // ── Helper ───────────────────────────────────────────────
    private Map<String, Object> event(String type, Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("event_type", type);
        m.put("timestamp",  LocalDateTime.now().toString());
        for (int i = 0; i < kv.length - 1; i += 2) {
            m.put(kv[i].toString(), kv[i + 1]);
        }
        return m;
    }

    private void send(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload).whenComplete((r, ex) -> {
            if (ex != null) log.warn("⚠️ Kafka '{}' key='{}': {}", topic, key, ex.getMessage());
            else log.debug("✅ Kafka '{}' p={} o={}",
                    topic, r.getRecordMetadata().partition(), r.getRecordMetadata().offset());
        });
    }
}