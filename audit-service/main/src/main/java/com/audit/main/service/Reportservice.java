package main.java.com.audit.main.service;

import com.hsgroup.audit.entity.Audit;
import com.hsgroup.audit.entity.SubcategoryResult;
import com.hsgroup.audit.model.ComplianceResult;
import com.hsgroup.audit.model.NistFramework;
import com.hsgroup.audit.model.NistQuestionsBank;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Feature A — Génération de rapports professionnels.
 *
 * PDF  : iText 7 (AGPL) — structure complète : couverture, executive summary,
 *         dashboard, détail par fonction, plan d'action
 * DOCX : Apache POI — document Word éditable, même structure
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final AuditService    auditService;
    private final ScoringService  scoringService;

    // Couleurs H&S Group
    private static final DeviceRgb COLOR_PRIMARY     = new DeviceRgb(0x1A, 0x23, 0x7E); // bleu foncé
    private static final DeviceRgb COLOR_COMPLIANT   = new DeviceRgb(0x4C, 0xAF, 0x50); // vert
    private static final DeviceRgb COLOR_PARTIAL      = new DeviceRgb(0xFF, 0x98, 0x00); // orange
    private static final DeviceRgb COLOR_NON_COMPLIANT= new DeviceRgb(0xF4, 0x43, 0x36); // rouge
    private static final DeviceRgb COLOR_NOT_ASSESSED = new DeviceRgb(0x9E, 0x9E, 0x9E); // gris
    private static final DeviceRgb COLOR_NA           = new DeviceRgb(0x60, 0x7D, 0x8B); // bleu-gris
    private static final DeviceRgb COLOR_LIGHT_GREY   = new DeviceRgb(0xF5, 0xF5, 0xF5);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ══════════════════════════════════════════════════════════
    // DONNÉES COMMUNES
    // ══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public Map<String, Object> getReportData(String auditId) {
        Audit audit = auditService.getAudit(auditId);
        List<SubcategoryResult> scList = audit.getSubcategoryResults();

        Map<String, Object> scores = scoringService.recalculate(audit, scList);
        @SuppressWarnings("unchecked")
        Map<String, Double> fnScores = (Map<String, Double>) scores.get("function_scores");

        // Distribution
        Map<String, Long> dist = scList.stream()
                .collect(Collectors.groupingBy(sc -> sc.getResult().getValue(), Collectors.counting()));

        // Action items
        List<SubcategoryResult> actionItems = scList.stream()
                .filter(SubcategoryResult::needsAction)
                .sorted(Comparator.comparingInt(sc ->
                        sc.getResult() == ComplianceResult.NON_COMPLIANT ? 0 : 1))
                .toList();

        return Map.of(
                "audit_id",       audit.getId(),
                "organization",   audit.getOrganization(),
                "auditor",        audit.getAuditor(),
                "audit_date",     audit.getAuditDate(),
                "scope",          audit.getScope(),
                "global_score",   scores.get("global_score"),
                "maturity_tier",  scores.get("maturity_tier"),
                "tier_label",     scoringService.tierLabel((double) scores.get("global_score")),
                "function_scores",fnScores,
                "distribution",   dist,
                "action_count",   actionItems.size(),
                "generated_at",   LocalDateTime.now().toString()
        );
    }

    // ══════════════════════════════════════════════════════════
    // PDF — iText 7
    // ══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public byte[] generatePdf(String auditId) {
        Audit audit = auditService.getAudit(auditId);
        List<SubcategoryResult> scList = audit.getSubcategoryResults();
        Map<String, Object> scores = scoringService.recalculate(audit, scList);
        double globalScore = (double) scores.get("global_score");
        String tier = (String) scores.get("maturity_tier");
        @SuppressWarnings("unchecked")
        Map<String, Double> fnScores = (Map<String, Double>) scores.get("function_scores");

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(bos);
            PdfDocument pdf  = new PdfDocument(writer);
            Document doc     = new Document(pdf);

            PdfFont fontBold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // ── Page de couverture ────────────────────────────
            doc.add(new Paragraph("\n\n\n\n"));
            doc.add(new Paragraph("H&S GROUP")
                    .setFont(fontBold).setFontSize(14).setFontColor(COLOR_PRIMARY)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("RAPPORT D'AUDIT")
                    .setFont(fontBold).setFontSize(26).setFontColor(COLOR_PRIMARY)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("NIST Cybersecurity Framework 2.0")
                    .setFont(fontNormal).setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("\n"));
            doc.add(new Paragraph(audit.getOrganization())
                    .setFont(fontBold).setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("\n\n"));

            // Score global sur la page de couverture
            DeviceRgb scoreColor = globalScore >= 75 ? COLOR_COMPLIANT
                    : globalScore >= 50 ? COLOR_PARTIAL
                    : globalScore >= 25 ? COLOR_PARTIAL : COLOR_NON_COMPLIANT;

            doc.add(new Paragraph(String.format("Score Global : %.1f%%", globalScore))
                    .setFont(fontBold).setFontSize(22).setFontColor(scoreColor)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph(tier)
                    .setFont(fontBold).setFontSize(14).setFontColor(scoreColor)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("\n\n"));

            // Infos audit
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{40, 60})).useAllAvailableWidth();
            addInfoRow(infoTable, "Organisation",   audit.getOrganization(), fontBold, fontNormal);
            addInfoRow(infoTable, "Auditeur",        audit.getAuditor(),     fontBold, fontNormal);
            addInfoRow(infoTable, "Date d'audit",    audit.getAuditDate(),   fontBold, fontNormal);
            addInfoRow(infoTable, "Périmètre",       audit.getScope(),       fontBold, fontNormal);
            addInfoRow(infoTable, "Framework",       audit.getFrameworkVersion(), fontBold, fontNormal);
            addInfoRow(infoTable, "Généré le",       LocalDateTime.now().format(DATE_FMT), fontBold, fontNormal);
            doc.add(infoTable);

            doc.add(new AreaBreak());

            // ── Executive Summary ─────────────────────────────
            doc.add(new Paragraph("1. EXECUTIVE SUMMARY")
                    .setFont(fontBold).setFontSize(16).setFontColor(COLOR_PRIMARY));
            doc.add(new Paragraph(scoringService.tierLabel(globalScore))
                    .setFont(fontNormal).setFontSize(11));
            doc.add(new Paragraph("\n"));

            // Tableau récapitulatif par fonction
            doc.add(new Paragraph("Scores par fonction NIST CSF 2.0")
                    .setFont(fontBold).setFontSize(12));
            Table fnTable = new Table(UnitValue.createPercentArray(new float[]{20, 15, 10, 55})).useAllAvailableWidth();
            addTableHeader(fnTable, fontBold, "Fonction", "Score", "Poids", "Maturité");

            for (Map.Entry<String, NistFramework.Function> e : NistFramework.getFunctions().entrySet()) {
                String fnId = e.getKey();
                double fnScore = fnScores.getOrDefault(fnId, 0.0);
                DeviceRgb c = fnScore >= 75 ? COLOR_COMPLIANT : fnScore >= 50 ? COLOR_PARTIAL
                        : fnScore >= 25 ? COLOR_PARTIAL : COLOR_NON_COMPLIANT;
                fnTable.addCell(new Cell().add(new Paragraph(fnId + " — " + e.getValue().name()).setFont(fontNormal).setFontSize(9)));
                fnTable.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", fnScore)).setFont(fontBold).setFontSize(9).setFontColor(c)).setTextAlignment(TextAlignment.CENTER));
                fnTable.addCell(new Cell().add(new Paragraph((int)(ScoringService.WEIGHTS.getOrDefault(fnId,0.0)*100) + "%").setFont(fontNormal).setFontSize(9)).setTextAlignment(TextAlignment.CENTER));
                fnTable.addCell(new Cell().add(new Paragraph(scoringService.tier(fnScore)).setFont(fontNormal).setFontSize(9)));
            }
            doc.add(fnTable);
            doc.add(new Paragraph("\n"));

            // Distribution
            doc.add(new Paragraph("Distribution des résultats").setFont(fontBold).setFontSize(12));
            Table distTable = new Table(UnitValue.createPercentArray(new float[]{50,25,25})).useAllAvailableWidth();
            addTableHeader(distTable, fontBold, "Statut", "Nombre", "Pourcentage");

            Map<String, String> resultLabels = Map.of(
                    "compliant","Conforme","partially_compliant","Partiellement Conforme",
                    "non_compliant","Non Conforme","not_assessed","Non Évalué","not_applicable","Non Applicable");
            for (ComplianceResult r : ComplianceResult.values()) {
                long count = scList.stream().filter(sc -> sc.getResult() == r).count();
                if (count == 0) continue;
                String label = resultLabels.getOrDefault(r.getValue(), r.getValue());
                distTable.addCell(new Cell().add(new Paragraph(label).setFont(fontNormal).setFontSize(9)));
                distTable.addCell(new Cell().add(new Paragraph(String.valueOf(count)).setFont(fontBold).setFontSize(9)).setTextAlignment(TextAlignment.CENTER));
                distTable.addCell(new Cell().add(new Paragraph(String.format("%.1f%%", count * 100.0 / scList.size())).setFont(fontNormal).setFontSize(9)).setTextAlignment(TextAlignment.CENTER));
            }
            doc.add(distTable);

            doc.add(new AreaBreak());

            // ── Détail par fonction ───────────────────────────
            doc.add(new Paragraph("2. DÉTAIL PAR FONCTION").setFont(fontBold).setFontSize(16).setFontColor(COLOR_PRIMARY));

            Map<String, SubcategoryResult> scIdx = scList.stream()
                    .collect(Collectors.toMap(SubcategoryResult::getSubcategoryId, sc -> sc));

            for (Map.Entry<String, NistFramework.Function> fnEntry : NistFramework.getFunctions().entrySet()) {
                String fnId = fnEntry.getKey();
                NistFramework.Function fn = fnEntry.getValue();

                doc.add(new Paragraph(fnId + " — " + fn.name() + " (" + String.format("%.1f%%", fnScores.getOrDefault(fnId, 0.0)) + ")")
                        .setFont(fontBold).setFontSize(13).setFontColor(COLOR_PRIMARY));
                doc.add(new Paragraph(fn.description()).setFont(fontNormal).setFontSize(10));
                doc.add(new Paragraph("\n"));

                for (NistFramework.Category cat : fn.categories()) {
                    doc.add(new Paragraph(cat.id() + " — " + cat.name())
                            .setFont(fontBold).setFontSize(11));

                    Table catTable = new Table(UnitValue.createPercentArray(new float[]{18,12,70})).useAllAvailableWidth();
                    addTableHeader(catTable, fontBold, "SC", "Résultat", "Détails");

                    for (NistFramework.Subcategory sc : cat.subcategories()) {
                        SubcategoryResult r = scIdx.get(sc.id());
                        if (r == null) continue;

                        DeviceRgb rc = getResultColor(r.getResult());
                        catTable.addCell(new Cell().add(new Paragraph(sc.id()).setFont(fontBold).setFontSize(8)));
                        catTable.addCell(new Cell().add(new Paragraph(r.getResult().getLabelFr()).setFont(fontNormal).setFontSize(8).setFontColor(rc)));

                        StringBuilder detail = new StringBuilder();
                        if (!r.getEvidence().isBlank())      detail.append("Preuves: ").append(r.getEvidence()).append("\n");
                        if (!r.getAuditorNotes().isBlank())  detail.append("Notes: ").append(r.getAuditorNotes()).append("\n");
                        if (!r.getRecommendation().isBlank())detail.append("Recommandation: ").append(r.getRecommendation());
                        catTable.addCell(new Cell().add(new Paragraph(detail.toString()).setFont(fontNormal).setFontSize(8)));
                    }
                    doc.add(catTable);
                    doc.add(new Paragraph("\n"));
                }
            }

            doc.add(new AreaBreak());

            // ── Plan d'action ─────────────────────────────────
            doc.add(new Paragraph("3. PLAN D'ACTION PRIORISÉ").setFont(fontBold).setFontSize(16).setFontColor(COLOR_PRIMARY));

            List<SubcategoryResult> actionItems = scList.stream()
                    .filter(SubcategoryResult::needsAction)
                    .sorted(Comparator.comparingInt(sc ->
                            sc.getResult() == ComplianceResult.NON_COMPLIANT &&
                            Set.of("PR","DE").contains(sc.getFunctionId()) ? 0
                            : sc.getResult() == ComplianceResult.NON_COMPLIANT ? 1 : 2))
                    .toList();

            doc.add(new Paragraph(actionItems.size() + " sous-catégories nécessitent des actions correctives.")
                    .setFont(fontNormal).setFontSize(11));
            doc.add(new Paragraph("\n"));

            Table actionTable = new Table(UnitValue.createPercentArray(new float[]{15,12,12,61})).useAllAvailableWidth();
            addTableHeader(actionTable, fontBold, "SC", "Résultat", "Priorité", "Recommandation");

            for (SubcategoryResult sc : actionItems) {
                boolean isCritical = Set.of("PR","DE").contains(sc.getFunctionId()) && sc.getResult() == ComplianceResult.NON_COMPLIANT;
                String priority = isCritical ? "Critique" : sc.getResult() == ComplianceResult.NON_COMPLIANT ? "Élevée" : "Moyenne";
                DeviceRgb pc = isCritical ? COLOR_NON_COMPLIANT : sc.getResult() == ComplianceResult.NON_COMPLIANT ? COLOR_NON_COMPLIANT : COLOR_PARTIAL;

                actionTable.addCell(new Cell().add(new Paragraph(sc.getSubcategoryId()).setFont(fontBold).setFontSize(8)));
                actionTable.addCell(new Cell().add(new Paragraph(sc.getResult().getLabelFr()).setFont(fontNormal).setFontSize(8).setFontColor(getResultColor(sc.getResult()))));
                actionTable.addCell(new Cell().add(new Paragraph(priority).setFont(fontBold).setFontSize(8).setFontColor(pc)));

                String rec = !sc.getRecommendation().isBlank() ? sc.getRecommendation()
                        : NistQuestionsBank.getQuestion(sc.getSubcategoryId())
                            .map(q -> "Mettre en place : " + q.getEvidenceExamples().stream().findFirst().orElse("Voir guide"))
                            .orElse("Action corrective requise");
                actionTable.addCell(new Cell().add(new Paragraph(rec).setFont(fontNormal).setFontSize(8)));
            }
            doc.add(actionTable);

            doc.close();
            log.info("✅ PDF généré : {} octets", bos.size());
            return bos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF : " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════════════
    // DOCX — Apache POI
    // ══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public byte[] generateDocx(String auditId) {
        Audit audit = auditService.getAudit(auditId);
        List<SubcategoryResult> scList = audit.getSubcategoryResults();
        Map<String, Object> scores = scoringService.recalculate(audit, scList);
        double globalScore = (double) scores.get("global_score");
        String tier = (String) scores.get("maturity_tier");
        @SuppressWarnings("unchecked")
        Map<String, Double> fnScores = (Map<String, Double>) scores.get("function_scores");

        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            // ── Couverture ────────────────────────────────────
            addDocxHeading(doc, "H&S GROUP — RAPPORT D'AUDIT NIST CSF 2.0", 1);
            addDocxParagraph(doc, "Organisation : " + audit.getOrganization());
            addDocxParagraph(doc, "Auditeur : " + audit.getAuditor());
            addDocxParagraph(doc, "Date d'audit : " + audit.getAuditDate());
            addDocxParagraph(doc, "Périmètre : " + audit.getScope());
            addDocxParagraph(doc, String.format("Score Global : %.1f%% — %s", globalScore, tier));
            addDocxParagraph(doc, "Généré le : " + LocalDateTime.now().format(DATE_FMT));

            // ── Executive Summary ─────────────────────────────
            addDocxHeading(doc, "1. EXECUTIVE SUMMARY", 1);
            addDocxParagraph(doc, scoringService.tierLabel(globalScore));

            addDocxHeading(doc, "Scores par fonction", 2);
            for (Map.Entry<String, NistFramework.Function> e : NistFramework.getFunctions().entrySet()) {
                String fnId = e.getKey();
                double s = fnScores.getOrDefault(fnId, 0.0);
                addDocxParagraph(doc, String.format("  %s — %s : %.1f%% (%s)",
                        fnId, e.getValue().name(), s, scoringService.tier(s)));
            }

            // ── Détail par fonction ───────────────────────────
            addDocxHeading(doc, "2. DÉTAIL PAR FONCTION", 1);

            Map<String, SubcategoryResult> scIdx = scList.stream()
                    .collect(Collectors.toMap(SubcategoryResult::getSubcategoryId, sc -> sc));

            for (Map.Entry<String, NistFramework.Function> fnEntry : NistFramework.getFunctions().entrySet()) {
                String fnId = fnEntry.getKey();
                NistFramework.Function fn = fnEntry.getValue();

                addDocxHeading(doc, fnId + " — " + fn.name() + " (" +
                        String.format("%.1f%%", fnScores.getOrDefault(fnId, 0.0)) + ")", 2);
                addDocxParagraph(doc, fn.description());

                for (NistFramework.Category cat : fn.categories()) {
                    addDocxHeading(doc, cat.id() + " — " + cat.name(), 3);

                    for (NistFramework.Subcategory sc : cat.subcategories()) {
                        SubcategoryResult r = scIdx.get(sc.id());
                        if (r == null) continue;
                        StringBuilder sb = new StringBuilder();
                        sb.append(sc.id()).append(" [").append(r.getResult().getLabelFr()).append("]\n");
                        sb.append("Description : ").append(sc.description()).append("\n");
                        if (!r.getEvidence().isBlank())       sb.append("Preuves : ").append(r.getEvidence()).append("\n");
                        if (!r.getAuditorNotes().isBlank())   sb.append("Notes : ").append(r.getAuditorNotes()).append("\n");
                        if (!r.getRecommendation().isBlank()) sb.append("Recommandation : ").append(r.getRecommendation()).append("\n");
                        addDocxParagraph(doc, sb.toString());
                    }
                }
            }

            // ── Plan d'action ─────────────────────────────────
            addDocxHeading(doc, "3. PLAN D'ACTION PRIORISÉ", 1);

            scList.stream().filter(SubcategoryResult::needsAction)
                    .sorted(Comparator.comparingInt(sc ->
                            sc.getResult() == ComplianceResult.NON_COMPLIANT ? 0 : 1))
                    .forEach(sc -> {
                        boolean crit = Set.of("PR","DE").contains(sc.getFunctionId())
                                && sc.getResult() == ComplianceResult.NON_COMPLIANT;
                        String priority = crit ? "🔴 CRITIQUE" : sc.getResult() == ComplianceResult.NON_COMPLIANT ? "🟠 ÉLEVÉE" : "🟡 MOYENNE";
                        String rec = !sc.getRecommendation().isBlank() ? sc.getRecommendation()
                                : NistQuestionsBank.getQuestion(sc.getSubcategoryId())
                                    .map(q -> q.getEvidenceExamples().stream().findFirst().orElse("Action corrective requise"))
                                    .orElse("Action corrective requise");
                        addDocxParagraph(doc, priority + " — " + sc.getSubcategoryId() +
                                " [" + sc.getResult().getLabelFr() + "]\nAction : " + rec);
                    });

            doc.write(bos);
            log.info("✅ DOCX généré : {} octets", bos.size());
            return bos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération DOCX : " + e.getMessage(), e);
        }
    }

    // ══════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════

    private DeviceRgb getResultColor(ComplianceResult r) {
        return switch (r) {
            case COMPLIANT           -> COLOR_COMPLIANT;
            case PARTIALLY_COMPLIANT -> COLOR_PARTIAL;
            case NON_COMPLIANT       -> COLOR_NON_COMPLIANT;
            case NOT_APPLICABLE      -> COLOR_NA;
            default                  -> COLOR_NOT_ASSESSED;
        };
    }

    private void addInfoRow(Table t, String label, String value, PdfFont bold, PdfFont normal) {
        t.addCell(new Cell().add(new Paragraph(label).setFont(bold).setFontSize(10)).setBackgroundColor(COLOR_LIGHT_GREY));
        t.addCell(new Cell().add(new Paragraph(value != null ? value : "").setFont(normal).setFontSize(10)));
    }

    private void addTableHeader(Table t, PdfFont bold, String... headers) {
        for (String h : headers) {
            t.addHeaderCell(new Cell().add(
                    new Paragraph(h).setFont(bold).setFontSize(9).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(COLOR_PRIMARY));
        }
    }

    private void addDocxHeading(XWPFDocument doc, String text, int level) {
        XWPFParagraph p = doc.createParagraph();
        p.setStyle("Heading" + level);
        XWPFRun run = p.createRun();
        run.setText(text);
        run.setBold(true);
    }

    private void addDocxParagraph(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun run = p.createRun();
        run.setText(text);
        run.setFontSize(10);
    }
}