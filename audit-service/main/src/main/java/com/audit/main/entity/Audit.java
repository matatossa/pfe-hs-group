package main.java.com.audit.main.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Table : audit_audits
 * Un audit NIST CSF 2.0 (entête + agrégats de scores).
 */
@Entity
@Table(name = "audit_audits", indexes = {
        @Index(name = "idx_audit_org",    columnList = "organization"),
        @Index(name = "idx_audit_status", columnList = "status")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String organization;

    @Column(length = 255)
    @Builder.Default private String auditor = "";

    /** Format ISO : "2025-03-11" */
    @Column(length = 20)
    @Builder.Default private String auditDate = "";

    @Column(columnDefinition = "TEXT")
    @Builder.Default private String scope = "";

    @Column(length = 50)
    @Builder.Default private String frameworkVersion = "NIST CSF 2.0";

    /** in_progress | completed */
    @Column(length = 30)
    @Builder.Default private String status = "in_progress";

    /** Score global pondéré 0–100 — dénormalisé, recalculé après chaque saisie */
    @Builder.Default private Double globalScore = 0.0;

    /** Tier 1 | Tier 2 | Tier 3 | Tier 4 */
    @Column(length = 50)
    @Builder.Default private String maturityTier = "Tier 1";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(
        mappedBy = "audit",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @OrderBy("subcategoryId ASC")
    @Builder.Default
    private List<SubcategoryResult> subcategoryResults = new ArrayList<>();

    @OneToMany(
        mappedBy = "audit",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<JiraTicket> jiraTickets = new ArrayList<>();
}