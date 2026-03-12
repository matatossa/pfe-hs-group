package main.java.com.audit.main.model;

import java.util.*;

/**
 * Référentiel NIST CSF 2.0 complet.
 * Source officielle : https://doi.org/10.6028/NIST.CSWP.29 (février 2024)
 *
 * Structure : 6 fonctions → 21 catégories → 106 sous-catégories
 *
 * Rôles :
 *  - Initialise les 106 SubcategoryResult à la création d'un audit
 *  - Valide les sc_id entrants dans les requêtes
 *  - Expose le référentiel via GET /framework/nist
 */
public final class NistFramework {

    // ── Records immuables ─────────────────────────────────────

    public record Subcategory(String id, String functionId, String categoryId, String description) {}

    public record Category(String id, String name, List<Subcategory> subcategories) {}

    public record Function(String id, String name, String description, List<Category> categories) {
        public List<Subcategory> allSubcategories() {
            return categories.stream().flatMap(c -> c.subcategories().stream()).toList();
        }
    }

    // ── Registres ─────────────────────────────────────────────

    /** Ordre officiel NIST : GV → ID → PR → DE → RS → RC */
    private static final LinkedHashMap<String, Function> FUNCTIONS = new LinkedHashMap<>();

    /** Index plat sc_id → Subcategory — accès O(1) */
    private static final Map<String, Subcategory> SC_INDEX = new HashMap<>();

    static { buildFramework(); }

    // ══════════════════════════════════════════════════════════
    // FRAMEWORK DATA
    // ══════════════════════════════════════════════════════════

    private static void buildFramework() {

        // ── GV — GOVERN (31 SC) ───────────────────────────────
        add(fn("GV", "GOVERN",
            "Établit et surveille la stratégie de gestion des risques cybersécurité de l'organisation.",
            List.of(
            cat("GV.OC", "Organizational Context", List.of(
                sc("GV.OC-01","La mission organisationnelle est comprise et informe la cybersécurité"),
                sc("GV.OC-02","Les parties prenantes internes et externes sont identifiées"),
                sc("GV.OC-03","Les exigences légales, réglementaires et contractuelles sont identifiées"),
                sc("GV.OC-04","Les objectifs et services critiques sont définis"),
                sc("GV.OC-05","Les dépendances et services dont dépend l'organisation sont identifiés")
            )),
            cat("GV.RM", "Risk Management Strategy", List.of(
                sc("GV.RM-01","Les objectifs de gestion des risques cybersécurité sont établis"),
                sc("GV.RM-02","L'appétit au risque et les tolérances sont établis"),
                sc("GV.RM-03","La gestion des risques cyber est intégrée dans l'ERM"),
                sc("GV.RM-04","La direction stratégique pour les décisions de traitement des risques est établie"),
                sc("GV.RM-05","Les canaux de communication sur les risques sont établis"),
                sc("GV.RM-06","Une méthodologie standardisée d'évaluation des risques est établie"),
                sc("GV.RM-07","Les opportunités cyber sont identifiées et intégrées")
            )),
            cat("GV.RR", "Roles, Responsibilities, and Authorities", List.of(
                sc("GV.RR-01","La direction est responsable de la gestion des risques cyber"),
                sc("GV.RR-02","Les rôles et responsabilités cyber sont définis et communiqués"),
                sc("GV.RR-03","Des ressources adéquates sont allouées à la cybersécurité"),
                sc("GV.RR-04","La cybersécurité est intégrée dans les pratiques RH")
            )),
            cat("GV.PO", "Policy", List.of(
                sc("GV.PO-01","Une politique de gestion des risques cybersécurité est établie"),
                sc("GV.PO-02","La politique de cybersécurité est révisée et mise à jour")
            )),
            cat("GV.OV", "Oversight", List.of(
                sc("GV.OV-01","Les résultats de la gestion des risques sont examinés"),
                sc("GV.OV-02","La stratégie de gestion des risques est révisée"),
                sc("GV.OV-03","La performance de la gestion des risques est évaluée")
            )),
            cat("GV.SC", "Cybersecurity Supply Chain Risk Management", List.of(
                sc("GV.SC-01","Un programme C-SCRM est établi et approuvé par la direction"),
                sc("GV.SC-02","Les rôles cyber des fournisseurs et partenaires sont définis"),
                sc("GV.SC-03","La gestion des risques supply chain est intégrée dans l'ERM"),
                sc("GV.SC-04","Les fournisseurs sont inventoriés et priorisés"),
                sc("GV.SC-05","Les exigences cyber supply chain sont définies et intégrées aux contrats"),
                sc("GV.SC-06","Des diligences raisonnables sont réalisées sur les fournisseurs"),
                sc("GV.SC-07","Les risques fournisseurs sont suivis et surveillés"),
                sc("GV.SC-08","Les fournisseurs critiques sont inclus dans la réponse aux incidents"),
                sc("GV.SC-09","Les pratiques cyber supply chain sont surveillées tout au long du cycle de vie"),
                sc("GV.SC-10","Les plans C-SCRM couvrent les activités post-contrat")
            ))
        )));

        // ── ID — IDENTIFY (21 SC) ─────────────────────────────
        add(fn("ID", "IDENTIFY",
            "Comprendre les actifs, les risques et les dépendances de l'organisation.",
            List.of(
            cat("ID.AM", "Asset Management", List.of(
                sc("ID.AM-01","L'inventaire du matériel informatique est maintenu"),
                sc("ID.AM-02","L'inventaire des logiciels, services et systèmes est maintenu"),
                sc("ID.AM-03","Les flux de données réseau autorisés sont documentés"),
                sc("ID.AM-04","L'inventaire des services des fournisseurs est maintenu"),
                sc("ID.AM-05","Les actifs sont priorisés selon leur classification et criticité"),
                sc("ID.AM-07","L'inventaire des données et métadonnées est maintenu"),
                sc("ID.AM-08","Les actifs sont gérés tout au long de leur cycle de vie")
            )),
            cat("ID.RA", "Risk Assessment", List.of(
                sc("ID.RA-01","Les vulnérabilités dans les actifs sont identifiées et enregistrées"),
                sc("ID.RA-02","Des renseignements sur les menaces cyber sont reçus (CTI)"),
                sc("ID.RA-03","Les menaces internes et externes sont identifiées"),
                sc("ID.RA-04","Les impacts et probabilités des vulnérabilités sont évalués"),
                sc("ID.RA-05","Les risques sont compris et priorisés"),
                sc("ID.RA-06","Les réponses aux risques sont sélectionnées et planifiées"),
                sc("ID.RA-07","Les changements et exceptions sont évalués pour leur impact"),
                sc("ID.RA-08","Des processus de divulgation des vulnérabilités sont établis"),
                sc("ID.RA-09","L'authenticité et l'intégrité du matériel et logiciels sont vérifiées"),
                sc("ID.RA-10","Les fournisseurs critiques sont évalués avant acquisition")
            )),
            cat("ID.IM", "Improvement", List.of(
                sc("ID.IM-01","Des améliorations sont identifiées à partir des évaluations"),
                sc("ID.IM-02","Des améliorations sont identifiées à partir des tests et exercices"),
                sc("ID.IM-03","Des améliorations sont identifiées à partir des processus opérationnels"),
                sc("ID.IM-04","Les plans de réponse aux incidents sont établis et améliorés")
            ))
        )));

        // ── PR — PROTECT (22 SC) ──────────────────────────────
        add(fn("PR", "PROTECT",
            "Déployer les mesures de protection appropriées pour les actifs critiques.",
            List.of(
            cat("PR.AA", "Identity Management, Authentication, and Access Control", List.of(
                sc("PR.AA-01","Les identités et credentials sont gérés pour utilisateurs, services et équipements"),
                sc("PR.AA-02","Les identités sont vérifiées et liées aux credentials"),
                sc("PR.AA-03","Les utilisateurs, services et équipements sont authentifiés (MFA)"),
                sc("PR.AA-04","Les assertions d'identité sont protégées et vérifiées"),
                sc("PR.AA-05","Les permissions sont gérées selon le principe du moindre privilège"),
                sc("PR.AA-06","L'accès physique aux actifs est géré, surveillé et contrôlé")
            )),
            cat("PR.AT", "Awareness and Training", List.of(
                sc("PR.AT-01","Les collaborateurs reçoivent une sensibilisation cybersécurité"),
                sc("PR.AT-02","Les personnes aux rôles spécialisés reçoivent une formation adaptée")
            )),
            cat("PR.DS", "Data Security", List.of(
                sc("PR.DS-01","Les données au repos sont protégées (chiffrement)"),
                sc("PR.DS-02","Les données en transit sont protégées (TLS)"),
                sc("PR.DS-10","Les données en cours d'utilisation sont protégées"),
                sc("PR.DS-11","Des sauvegardes sont créées, protégées et régulièrement testées")
            )),
            cat("PR.PS", "Platform Security", List.of(
                sc("PR.PS-01","Des pratiques de gestion des configurations sont établies"),
                sc("PR.PS-02","Les logiciels sont maintenus, remplacés et supprimés selon le risque"),
                sc("PR.PS-03","Le matériel est maintenu, remplacé et retiré selon le risque"),
                sc("PR.PS-04","Des journaux sont générés et mis à disposition pour le monitoring"),
                sc("PR.PS-05","L'installation de logiciels non autorisés est prévenue"),
                sc("PR.PS-06","Les pratiques de développement sécurisé sont intégrées dans le SDLC")
            )),
            cat("PR.IR", "Technology Infrastructure Resilience", List.of(
                sc("PR.IR-01","Les réseaux et environnements sont protégés contre les accès non autorisés"),
                sc("PR.IR-02","Les actifs sont protégés contre les menaces environnementales"),
                sc("PR.IR-03","Des mécanismes de résilience (HA, redondance) sont implémentés"),
                sc("PR.IR-04","Une capacité de ressources adéquate est maintenue")
            ))
        )));

        // ── DE — DETECT (11 SC) ───────────────────────────────
        add(fn("DE", "DETECT",
            "Détecter les événements de cybersécurité potentiels.",
            List.of(
            cat("DE.CM", "Continuous Monitoring", List.of(
                sc("DE.CM-01","Les réseaux et services sont surveillés en continu (SIEM/SOC)"),
                sc("DE.CM-02","L'environnement physique est surveillé"),
                sc("DE.CM-03","Les activités des utilisateurs et technologies sont surveillées"),
                sc("DE.CM-06","Les activités des prestataires externes sont surveillées"),
                sc("DE.CM-09","Le matériel, logiciels et environnements sont surveillés (EDR)")
            )),
            cat("DE.AE", "Adverse Event Analysis", List.of(
                sc("DE.AE-02","Les événements adverses potentiels sont analysés"),
                sc("DE.AE-03","Des informations sont corrélées depuis de multiples sources"),
                sc("DE.AE-04","L'impact et le périmètre des événements adverses sont compris"),
                sc("DE.AE-06","Les informations sur les événements sont transmises aux équipes autorisées"),
                sc("DE.AE-07","La threat intelligence est intégrée dans l'analyse des événements"),
                sc("DE.AE-08","Des incidents sont déclarés selon les critères définis")
            ))
        )));

        // ── RS — RESPOND (13 SC) ──────────────────────────────
        add(fn("RS", "RESPOND",
            "Agir suite à la détection d'un incident de cybersécurité.",
            List.of(
            cat("RS.MA", "Incident Management", List.of(
                sc("RS.MA-01","Le plan de réponse aux incidents est exécuté dès la déclaration"),
                sc("RS.MA-02","Les rapports d'incidents sont triés et validés"),
                sc("RS.MA-03","Les incidents sont catégorisés et priorisés"),
                sc("RS.MA-04","Les incidents sont escaladés selon les procédures définies"),
                sc("RS.MA-05","Les critères pour initier la récupération sont définis")
            )),
            cat("RS.AN", "Incident Analysis", List.of(
                sc("RS.AN-03","Une analyse est réalisée pour établir ce qui s'est passé"),
                sc("RS.AN-06","Les actions d'investigation sont enregistrées et leur intégrité préservée"),
                sc("RS.AN-07","Une analyse des causes racines (RCA) est conduite"),
                sc("RS.AN-08","Un incident est formellement clôturé selon les critères établis")
            )),
            cat("RS.CO", "Incident Response Reporting and Communication", List.of(
                sc("RS.CO-02","Les parties prenantes sont notifiées selon les obligations légales"),
                sc("RS.CO-03","Les informations sur les incidents sont partagées avec les parties désignées")
            )),
            cat("RS.MI", "Incident Mitigation", List.of(
                sc("RS.MI-01","Les incidents sont contenus pour limiter leur propagation"),
                sc("RS.MI-02","Les incidents sont éradiqués pour éliminer la menace")
            ))
        )));

        // ── RC — RECOVER (8 SC) ───────────────────────────────
        add(fn("RC", "RECOVER",
            "Restaurer les actifs et capacités impactés par un incident.",
            List.of(
            cat("RC.RP", "Incident Recovery Plan Execution", List.of(
                sc("RC.RP-01","La phase de récupération du plan de réponse est exécutée"),
                sc("RC.RP-02","Les actions de récupération sont sélectionnées, priorisées et exécutées"),
                sc("RC.RP-03","L'intégrité des sauvegardes est vérifiée avant utilisation"),
                sc("RC.RP-04","Les fonctions critiques sont maintenues pendant la récupération"),
                sc("RC.RP-05","L'intégrité des actifs restaurés est vérifiée"),
                sc("RC.RP-06","La fin de la récupération est formellement déclarée")
            )),
            cat("RC.CO", "Incident Recovery Communication", List.of(
                sc("RC.CO-03","L'avancement de la récupération est communiqué aux parties prenantes"),
                sc("RC.CO-04","Les communications publiques sur la récupération sont approuvées")
            ))
        )));
    }

    // ══════════════════════════════════════════════════════════
    // HELPERS PRIVÉS
    // ══════════════════════════════════════════════════════════

    private static void add(Function fn) {
        FUNCTIONS.put(fn.id(), fn);
        for (Category cat : fn.categories()) {
            for (Subcategory sc : cat.subcategories()) {
                SC_INDEX.put(sc.id(), sc);
            }
        }
    }

    private static Function fn(String id, String name, String description, List<Category> cats) {
        return new Function(id, name, description, cats);
    }

    private static Category cat(String id, String name, List<Subcategory> scs) {
        return new Category(id, name, scs);
    }

    private static Subcategory sc(String id, String description) {
        String fnId  = id.split("\\.")[0];
        String catId = id.substring(0, id.lastIndexOf('-'));
        return new Subcategory(id, fnId, catId, description);
    }

    // ══════════════════════════════════════════════════════════
    // API PUBLIQUE
    // ══════════════════════════════════════════════════════════

    public static LinkedHashMap<String, Function> getFunctions() { return FUNCTIONS; }

    public static Optional<Subcategory> findSubcategory(String scId) {
        return Optional.ofNullable(SC_INDEX.get(scId));
    }

    public static boolean isValidScId(String scId) { return SC_INDEX.containsKey(scId); }

    public static List<Subcategory> getAllSubcategories() {
        return FUNCTIONS.values().stream().flatMap(f -> f.allSubcategories().stream()).toList();
    }

    public static int getTotalSubcategories() { return SC_INDEX.size(); } // 106
}