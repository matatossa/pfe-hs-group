package main.java.com.audit.main.service;

import com.hsgroup.audit.entity.SubcategoryResult;
import com.hsgroup.audit.model.NistFramework;
import com.hsgroup.audit.model.NistQuestionsBank;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Feature B — Questionnaire guidé.
 *
 * Retourne les 106 questions métier FR groupées par fonction → catégorie.
 * Chaque question affiche son ID NIST visible : "GV.RM-01 — Les objectifs..."
 * Optionnellement pré-remplie avec les résultats en base (pour /audits/{id}/questionnaire).
 */
@Service
public class QuestionnaireService {

    /** Réponses standardisées présentées à l'auditeur pour chaque SC. */
    private static final List<Map<String, Object>> RESPONSE_OPTIONS = List.of(
            Map.of("value", "compliant",           "label", "Oui — Conforme",                  "score", 1.0),
            Map.of("value", "partially_compliant", "label", "Partiel — Partiellement conforme", "score", 0.5),
            Map.of("value", "non_compliant",       "label", "Non — Non conforme",               "score", 0.0),
            Map.of("value", "not_applicable",      "label", "N/A — Hors périmètre",             "score", "null")
    );

    /**
     * Construit le questionnaire complet, groupé par fonction → catégorie.
     *
     * @param auditScIndex Map sc_id → SubcategoryResult pour pré-remplissage (null si sans audit)
     */
    public Map<String, Object> build(Map<String, SubcategoryResult> auditScIndex) {

        Map<String, Object> questionnaire = new LinkedHashMap<>();

        for (Map.Entry<String, NistFramework.Function> fnEntry
                : NistFramework.getFunctions().entrySet()) {

            NistFramework.Function fn = fnEntry.getValue();
            Map<String, Object> fnMap = new LinkedHashMap<>();
            fnMap.put("name",        fn.name());
            fnMap.put("description", fn.description());
            fnMap.put("weight_pct",  (int)(ScoringService.WEIGHTS.getOrDefault(fnEntry.getKey(), 0.0) * 100) + "%");

            Map<String, Object> catsMap = new LinkedHashMap<>();

            for (NistFramework.Category cat : fn.categories()) {
                List<Map<String, Object>> questions = new ArrayList<>();

                for (NistFramework.Subcategory sc : cat.subcategories()) {
                    Optional<NistQuestionsBank.AuditQuestion> q = NistQuestionsBank.getQuestion(sc.id());
                    String questionText = q.map(NistQuestionsBank.AuditQuestion::getQuestion)
                                          .orElse(sc.description());

                    Map<String, Object> qMap = new LinkedHashMap<>();
                    qMap.put("sc_id",             sc.id());
                    // ─── ID NIST visible dans l'UI ────────────────────────
                    qMap.put("label",             sc.id() + " — " + questionText);
                    // ─────────────────────────────────────────────────────
                    qMap.put("question",          questionText);
                    qMap.put("nist_description",  sc.description());
                    qMap.put("help_text",         q.map(NistQuestionsBank.AuditQuestion::getHelpText).orElse(""));
                    qMap.put("evidence_examples", q.map(NistQuestionsBank.AuditQuestion::getEvidenceExamples).orElse(List.of()));
                    qMap.put("response_options",  RESPONSE_OPTIONS);

                    // Pré-remplissage
                    if (auditScIndex != null) {
                        SubcategoryResult r = auditScIndex.get(sc.id());
                        qMap.put("current_result",  r != null ? r.getResult().getValue() : "not_assessed");
                        qMap.put("evidence",        r != null ? r.getEvidence()      : "");
                        qMap.put("auditor_notes",   r != null ? r.getAuditorNotes()  : "");
                        qMap.put("status",          r != null ? r.getStatus()        : "todo");
                    } else {
                        qMap.put("current_result", "not_assessed");
                    }

                    questions.add(qMap);
                }

                Map<String, Object> catMap = new LinkedHashMap<>();
                catMap.put("name",        cat.name());
                catMap.put("questions",   questions);
                catMap.put("total",       questions.size());
                catsMap.put(cat.id(), catMap);
            }

            fnMap.put("categories", catsMap);
            questionnaire.put(fnEntry.getKey(), fnMap);
        }

        return questionnaire;
    }

    /** Liste plate des 106 questions (pour export). */
    public List<Map<String, Object>> flat() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, NistFramework.Function> fnEntry
                : NistFramework.getFunctions().entrySet()) {
            for (NistFramework.Category cat : fnEntry.getValue().categories()) {
                for (NistFramework.Subcategory sc : cat.subcategories()) {
                    Optional<NistQuestionsBank.AuditQuestion> q = NistQuestionsBank.getQuestion(sc.id());
                    String questionText = q.map(NistQuestionsBank.AuditQuestion::getQuestion)
                                          .orElse(sc.description());
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("sc_id",            sc.id());
                    row.put("function_id",       fnEntry.getKey());
                    row.put("function_name",     fnEntry.getValue().name());
                    row.put("category_id",       cat.id());
                    row.put("category_name",     cat.name());
                    row.put("label",             sc.id() + " — " + questionText);
                    row.put("question",          questionText);
                    row.put("nist_description",  sc.description());
                    row.put("help_text",         q.map(NistQuestionsBank.AuditQuestion::getHelpText).orElse(""));
                    row.put("evidence_examples", q.map(NistQuestionsBank.AuditQuestion::getEvidenceExamples).orElse(List.of()));
                    list.add(row);
                }
            }
        }
        return list;
    }
}