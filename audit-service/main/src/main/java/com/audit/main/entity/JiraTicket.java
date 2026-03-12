package main.java.com.audit.main.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Table : audit_jira_tickets
 * Ticket Jira généré à partir d'une non-conformité.
 */
@Entity
@Table(name = "audit_jira_tickets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JiraTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_id", nullable = false)
    private Audit audit;

    @Column(nullable = false, length = 20)
    private String subcategoryId;

    /** "CYBER-123" */
    @Column(unique = true, length = 50)
    private String ticketKey;

    @Column(length = 500)
    @Builder.Default private String ticketUrl = "";

    @Column(length = 500)
    @Builder.Default private String title = "";

    /** Blocker | Critical | Major | Minor */
    @Column(length = 20)
    @Builder.Default private String priority = "Major";

    @Column(length = 255)
    @Builder.Default private String assignee = "";

    @Column(length = 50)
    @Builder.Default private String status = "Open";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}