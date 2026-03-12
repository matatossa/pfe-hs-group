package main.java.com.audit.main.entity;

import com.hsgroup.audit.model.ComplianceResult;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Table : audit_subcategory_results
 * 106 lignes créées automatiquement à la création de l'audit (une par SC NIST CSF 2.0).
 * Initialisées à NOT_ASSESSED, remplies par l'auditeur via le questionnaire guidé.
 */
@Entity
@Table(name = "audit_subcategory_results", indexes = {
        @Index(name = "idx_scr_audit_id",     columnList = "audit_id"),
        @Index(name = "idx_scr_sc_id",        columnList = "subcategory_id"),
        @Index(name = "idx_scr_function_id",  columnList = "function_id"),
        @Index(name = "idx_scr_result",       columnList = "result")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubcategoryResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_id", nullable = false)
    private Audit audit;

    /** Ex: "GV.RM-01" */
    @Column(nullable = false, length = 20)
    private String subcategoryId;

    /** Ex: "GV" */
    @Column(nullable = false, length = 5)
    private String functionId;

    /** Ex: "GV.RM" */
    @Column(nullable = false, length = 10)
    private String categoryId;

    /** Description officielle NIST */
    @Column(columnDefinition = "TEXT")
    @Builder.Default private String description = "";

    /** Résultat de conformité — 5 valeurs possibles */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default private ComplianceResult result = ComplianceResult.NOT_ASSESSED;

    /** todo | in_progress | done */
    @Column(length = 20)
    @Builder.Default private String status = "todo";

    /** Preuves collectées sur le terrain */
    @Column(columnDefinition = "TEXT")
    @Builder.Default private String evidence = "";

    /** Notes internes de l'auditeur */
    @Column(columnDefinition = "TEXT")
    @Builder.Default private String auditorNotes = "";

    /** Recommandation de remédiation */
    @Column(columnDefinition = "TEXT")
    @Builder.Default private String recommendation = "";

    /** Clé du ticket Jira créé — "CYBER-123" */
    @Column(length = 50)
    @Builder.Default private String jiraTicket = "";

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ── Helpers ──────────────────────────────────────────────

    public boolean needsAction()         { return result != null && result.needsAction(); }
    public Double  getScoreContribution(){ return result != null ? result.getScoreWeight() : 0.0; }
}