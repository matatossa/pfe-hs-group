package main.java.com.audit.main.service;

import com.hsgroup.audit.dto.request.*;
import com.hsgroup.audit.entity.*;
import com.hsgroup.audit.exception.*;
import com.hsgroup.audit.model.*;
import com.hsgroup.audit.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service principal — orchestre la création, la saisie et la consultation des audits.
 *
 * Pattern : le service récupère les données via les repositories, délègue le scoring
 * au ScoringService et la publication d'événements au KafkaProducerService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditRepository              auditRepo;
    private final SubcategoryResultRepository  scRepo;
    private final ScoringService               scoringService;
    private final KafkaProducerService         kafka;
    private final QuestionnaireService         questionnaireService;

    // ══════════════════════════════════════════════════════════
    // CRUD AUDITS
    // ══════════════════════════════════════════════════════════

    @Transactional
    public Audit createAudit(CreateAuditRequest req) {

        Audit audit = Audit.builder()
                .organization(req.getOrganization())
                .auditor(req.getAuditor() != null ? req.getAuditor() : "")
                .auditDate(req.getAuditDate() != null && !req.getAuditDate().isBlank()
                        ? req.getAuditDate() : LocalDate.now().toString())
                .scope(req.getScope() != null ? req.getScope() : "")
                .build();

        auditRepo.save(audit);

        // Initialise les 106 SC en NOT_ASSESSED
        List<SubcategoryResult> scList = new ArrayList<>();
        for (Map.Entry<String, NistFramework.Function> fnEntry : NistFramework.getFunctions().entrySet()) {
            for (NistFramework.Category cat : fnEntry.getValue().categories()) {
                for (NistFramework.Subcategory sc : cat.subcategories()) {
                    scList.add(SubcategoryResult.builder()
                            .audit(audit)
                            .subcategoryId(sc.id())
                            .functionId(sc.functionId())
                            .categoryId(sc.categoryId())
                            .description(sc.description())
                            .result(ComplianceResult.NOT_ASSESSED)
                            .status("todo")
                            .build());
                }
            }
        }
        scRepo.saveAll(scList);

        log.info("✅ Audit créé : {} pour '{}'", audit.getId(), req.getOrganization());
        kafka.auditCreated(audit.getId(), req.getOrganization());

        return audit;
    }

    @Transactional(readOnly = true)
    public List<Audit> listAudits(String org, String status) {
        return auditRepo.findWithFilters(org, status);
    }

    @Transactional(readOnly = true)
    public Audit getAudit(String auditId) {
        return auditRepo.findById(parseUUID(auditId))
                .orElseThrow(() -> new AuditNotFoundException(auditId));
    }

    @Transactional
    public void deleteAudit(String auditId) {
        Audit a = getAudit(auditId);
        auditRepo.delete(a);
        log.info("🗑️ Audit supprimé : {}", auditId);
    }

    // ══════════════════════════════════════════════════════════
    // SAISIE DES RÉSULTATS
    // ══════════════════════════════════════════════════════════

    @Transactional
    public Map<String, Object> updateSubcategory(String auditId, String scId,
                                                  UpdateSubcategoryRequest req) {
        Audit audit = getAudit(auditId);

        SubcategoryResult sc = scRepo.findByAuditIdAndSubcategoryId(audit.getId(), scId)
                .orElseThrow(() -> new SubcategoryNotFoundException(scId));

        sc.setResult(req.getResult());
        sc.setEvidence(req.getEvidence() != null ? req.getEvidence() : "");
        sc.setAuditorNotes(req.getAuditorNotes() != null ? req.getAuditorNotes() : "");
        sc.setRecommendation(req.getRecommendation() != null ? req.getRecommendation() : "");
        sc.setStatus(req.getStatus() != null ? req.getStatus() : "done");
        scRepo.save(sc);

        // Recalcul du score global
        List<SubcategoryResult> allSc = scRepo.findByAuditIdOrderBySubcategoryIdAsc(audit.getId());
        Map<String, Object> scores = scoringService.recalculate(audit, allSc);
        auditRepo.save(audit);

        // Vérifier si l'audit est completé
        long assessed = allSc.stream().filter(s -> s.getResult().isAssessed()).count();
        if (assessed == allSc.size() && !"completed".equals(audit.getStatus())) {
            audit.setStatus("completed");
            auditRepo.save(audit);
            kafka.auditCompleted(audit.getId(), audit.getOrganization(),
                    (double) scores.get("global_score"), (String) scores.get("maturity_tier"));
        } else {
            kafka.scoreUpdated(audit.getId(), audit.getOrganization(),
                    (double) scores.get("global_score"), (String) scores.get("maturity_tier"));
        }

        log.info("✏️ {} → {} | audit={} | score={}", scId, req.getResult().getValue(),
                auditId, scores.get("global_score"));

        return Map.of("subcategory", sc, "audit_scores", scores);
    }

    @Transactional
    public Map<String, Object> bulkUpdate(String auditId, BulkUpdateRequest req) {
        Audit audit = getAudit(auditId);
        List<SubcategoryResult> allSc = scRepo.findByAuditIdOrderBySubcategoryIdAsc(audit.getId());

        Map<String, SubcategoryResult> idx = allSc.stream()
                .collect(Collectors.toMap(SubcategoryResult::getSubcategoryId, sc -> sc));

        List<String> saved    = new ArrayList<>();
        List<String> notFound = new ArrayList<>();

        for (BulkUpdateRequest.BulkItem item : req.getItems()) {
            SubcategoryResult sc = idx.get(item.getSubcategoryId());
            if (sc == null) { notFound.add(item.getSubcategoryId()); continue; }

            sc.setResult(item.getResult());
            sc.setEvidence(item.getEvidence() != null ? item.getEvidence() : "");
            sc.setAuditorNotes(item.getAuditorNotes() != null ? item.getAuditorNotes() : "");
            sc.setRecommendation(item.getRecommendation() != null ? item.getRecommendation() : "");
            sc.setStatus(item.getStatus() != null ? item.getStatus() : "done");
            saved.add(item.getSubcategoryId());
        }
        scRepo.saveAll(allSc);

        Map<String, Object> scores = scoringService.recalculate(audit, allSc);
        auditRepo.save(audit);

        return Map.of("saved", saved.size(), "not_found", notFound, "audit_scores", scores);
    }

    @Transactional(readOnly = true)
    public SubcategoryResult getSubcategory(String auditId, String scId) {
        UUID uid = parseUUID(auditId);
        if (!auditRepo.existsById(uid)) throw new AuditNotFoundException(auditId);
        return scRepo.findByAuditIdAndSubcategoryId(uid, scId)
                .orElseThrow(() -> new SubcategoryNotFoundException(scId));
    }

    // ══════════════════════════════════════════════════════════
    // SCORES & DASHBOARD
    // ══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public Map<String, Object> getScores(String auditId) {
        Audit audit = getAudit(auditId);
        List<SubcategoryResult> scList = scRepo.findByAuditIdOrderBySubcategoryIdAsc(audit.getId());
        Map<String, Object> scores = scoringService.recalculate(audit, scList);

        return new LinkedHashMap<>() {{
            put("audit_id",     audit.getId());
            put("organization", audit.getOrganization());
            putAll(scores);
            put("improvement_potential", scoringService.improvementPotential(scList));
            put("next_tier",             scoringService.nextTierGap((double) scores.get("global_score")));
            put("tier_label",            scoringService.tierLabel((double) scores.get("global_score")));
        }};
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProgress(String auditId) {
        Audit audit = getAudit(auditId);
        List<SubcategoryResult> scList = scRepo.findByAuditIdOrderBySubcategoryIdAsc(audit.getId());

        Map<String, Map<String, Object>> byFn = new LinkedHashMap<>();
        int totalDone = 0;

        for (Map.Entry<String, NistFramework.Function> fnEntry : NistFramework.getFunctions().entrySet()) {
            String fnId = fnEntry.getKey();
            List<String> fnScIds = fnEntry.getValue().allSubcategories().stream()
                    .map(NistFramework.Subcategory::id).toList();

            List<SubcategoryResult> fnScs = scList.stream()
                    .filter(sc -> fnScIds.contains(sc.getSubcategoryId())).toList();

            int done = (int) fnScs.stream().filter(sc -> sc.getResult().isAssessed()).count();
            totalDone += done;

            Map<String, Integer> byResult = new LinkedHashMap<>();
            for (String r : List.of("compliant","partially_compliant","non_compliant","not_assessed","not_applicable")) {
                String val = r;
                byResult.put(r, (int) fnScs.stream()
                        .filter(sc -> sc.getResult().getValue().equals(val)).count());
            }

            byFn.put(fnId, Map.of(
                    "name",      fnEntry.getValue().name(),
                    "total",     fnScs.size(),
                    "done",      done,
                    "remaining", fnScs.size() - done,
                    "pct",       fnScs.isEmpty() ? 0.0 : Math.round(done * 100.0 / fnScs.size() * 10) / 10.0,
                    "by_result", byResult
            ));
        }

        final int finalTotalDone = totalDone;
        return Map.of(
                "audit_id",        audit.getId(),
                "total_sc",        106,
                "total_done",      finalTotalDone,
                "total_remaining", 106 - finalTotalDone,
                "completion_pct",  Math.round(finalTotalDone * 100.0 / 106 * 10) / 10.0,
                "by_function",     byFn
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboard(String auditId) {
        Audit audit = getAudit(auditId);
        List<SubcategoryResult> scList = scRepo.findByAuditIdOrderBySubcategoryIdAsc(audit.getId());
        Map<String, Object> scores = scoringService.recalculate(audit, scList);

        @SuppressWarnings("unchecked")
        Map<String, Double> fnScores = (Map<String, Double>) scores.get("function_scores");

        // Radar
        List<Map<String, Object>> radar = NistFramework.getFunctions().keySet().stream()
                .map(fnId -> Map.<String, Object>of(
                        "function", fnId,
                        "score",    fnScores.getOrDefault(fnId, 0.0),
                        "fullMark", 100))
                .toList();

        // Barres avec couleur selon score
        List<Map<String, Object>> bars = NistFramework.getFunctions().entrySet().stream()
                .map(e -> {
                    double s = fnScores.getOrDefault(e.getKey(), 0.0);
                    return Map.<String, Object>of(
                            "function", e.getKey(),
                            "name",     e.getValue().name(),
                            "score",    s,
                            "color",    s >= 75 ? "#4CAF50" : s >= 50 ? "#8BC34A" : s >= 25 ? "#FF9800" : "#F44336");
                }).toList();

        // Distribution
        Map<String, Integer> dist = new LinkedHashMap<>();
        for (String r : List.of("compliant","partially_compliant","non_compliant","not_assessed","not_applicable")) {
            String val = r;
            dist.put(r, (int) scList.stream().filter(sc -> sc.getResult().getValue().equals(val)).count());
        }

        // Pie
        Map<String, String> colors = Map.of(
                "compliant","#4CAF50","partially_compliant","#FF9800",
                "non_compliant","#F44336","not_assessed","#9E9E9E","not_applicable","#607D8B");
        Map<String, String> labels = Map.of(
                "compliant","Conforme","partially_compliant","Partiellement Conforme",
                "non_compliant","Non Conforme","not_assessed","Non Évalué","not_applicable","Non Applicable");
        List<Map<String, Object>> pie = dist.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(e -> Map.<String, Object>of(
                        "name",  labels.get(e.getKey()),
                        "value", e.getValue(),
                        "color", colors.get(e.getKey())))
                .toList();

        return Map.of(
                "audit_id",      audit.getId(),
                "global_score",  scores.get("global_score"),
                "maturity_tier", scores.get("maturity_tier"),
                "radar_data",    radar,
                "bar_data",      bars,
                "pie_data",      pie,
                "distribution",  dist
        );
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecommendations(String auditId, int limit) {
        Audit audit = getAudit(auditId);
        List<SubcategoryResult> actionItems = scRepo.findActionItems(audit.getId());

        Comparator<SubcategoryResult> priorityOrder = Comparator.comparingInt(sc -> {
            boolean isCriticalFn = Set.of("PR","DE").contains(sc.getFunctionId());
            if (sc.getResult() == ComplianceResult.NON_COMPLIANT && isCriticalFn) return 0;
            if (sc.getResult() == ComplianceResult.NON_COMPLIANT)                 return 1;
            if (sc.getResult() == ComplianceResult.PARTIALLY_COMPLIANT && isCriticalFn) return 2;
            return 3;
        });

        String[] priorities = {"Critique","Élevée","Moyenne haute","Moyenne"};

        return actionItems.stream().sorted(priorityOrder).limit(limit).map(sc -> {
            int pRank = priorityOrder.compare(sc, sc); // calcul réutilisé
            Optional<NistQuestionsBank.AuditQuestion> q = NistQuestionsBank.getQuestion(sc.getSubcategoryId());

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("subcategory_id",   sc.getSubcategoryId());
            m.put("function_id",      sc.getFunctionId());
            m.put("result",           sc.getResult().getValue());
            m.put("result_label",     sc.getResult().getLabelFr());
            m.put("priority",         priorities[Math.min(pRank, 3)]);
            m.put("question",         q.map(NistQuestionsBank.AuditQuestion::getQuestion).orElse(sc.getDescription()));
            m.put("help_text",        q.map(NistQuestionsBank.AuditQuestion::getHelpText).orElse(""));
            m.put("evidence_needed",  q.map(NistQuestionsBank.AuditQuestion::getEvidenceExamples).orElse(List.of()));
            m.put("evidence",         sc.getEvidence());
            m.put("auditor_notes",    sc.getAuditorNotes());
            m.put("recommendation",   sc.getRecommendation());
            return m;
        }).toList();
    }

    // ══════════════════════════════════════════════════════════
    // QUESTIONNAIRE
    // ══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public Map<String, Object> getQuestionnaire(String auditId) {
        Audit audit = getAudit(auditId);
        List<SubcategoryResult> scList = scRepo.findByAuditIdOrderBySubcategoryIdAsc(audit.getId());
        Map<String, SubcategoryResult> idx = scList.stream()
                .collect(Collectors.toMap(SubcategoryResult::getSubcategoryId, sc -> sc));
        return questionnaireService.build(idx);
    }

    // ══════════════════════════════════════════════════════════
    // JIRA
    // ══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public Map<String, Object> generateJiraTickets(String auditId) {
        Audit audit = getAudit(auditId);
        List<SubcategoryResult> actionItems = scRepo.findActionItems(audit.getId());

        List<Map<String, Object>> preview = new ArrayList<>();
        for (SubcategoryResult sc : actionItems) {
            boolean isCritical = Set.of("PR","DE").contains(sc.getFunctionId())
                    && sc.getResult() == ComplianceResult.NON_COMPLIANT;
            String priority = isCritical ? "Blocker"
                    : sc.getResult() == ComplianceResult.NON_COMPLIANT ? "Critical" : "Major";

            Optional<NistQuestionsBank.AuditQuestion> q = NistQuestionsBank.getQuestion(sc.getSubcategoryId());
            String title = "[NIST " + sc.getSubcategoryId() + "] " +
                    q.map(NistQuestionsBank.AuditQuestion::getQuestion).orElse(sc.getDescription());

            kafka.requestJiraTicket(audit.getId(), audit.getOrganization(),
                    sc.getSubcategoryId(), title, priority, sc.getRecommendation());

            preview.add(Map.of(
                    "subcategory_id", sc.getSubcategoryId(),
                    "title",          title,
                    "priority",       priority,
                    "result",         sc.getResult().getValue()
            ));
        }

        return Map.of(
                "message",          preview.size() + " tickets Jira demandés via Kafka",
                "tickets_preview",  preview
        );
    }

    // ── Helper ────────────────────────────────────────────────
    private UUID parseUUID(String id) {
        try { return UUID.fromString(id); }
        catch (IllegalArgumentException e) { throw new AuditNotFoundException(id); }
    }
}