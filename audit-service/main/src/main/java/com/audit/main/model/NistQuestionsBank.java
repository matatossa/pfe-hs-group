package com.hsgroup.audit.model;

import lombok.Builder;
import lombok.Getter;

import java.util.*;

/**
 * Banque de 106 questions métier en français — Feature B.
 *
 * Chaque question contient :
 *   scId             → identifiant NIST visible dans l'UI : "GV.RM-01"
 *   label            → libellé affiché : "GV.RM-01 — Les objectifs..."
 *   question         → question métier en français
 *   helpText         → guide pour l'auditeur sur le terrain
 *   evidenceExamples → liste des preuves attendues
 */
public final class NistQuestionsBank {

    @Builder @Getter
    public static class AuditQuestion {
        private final String       scId;
        private final String       functionId;
        private final String       categoryId;
        private final String       question;
        private final String       helpText;
        private final List<String> evidenceExamples;

        /** "GV.RM-01 — Question..." — affiché dans l'UI */
        public String getLabel() { return scId + " — " + question; }
    }

    private static final LinkedHashMap<String, AuditQuestion> BANK = new LinkedHashMap<>();

    static { buildBank(); }

    private static void buildBank() {

        // ══ GV.OC ══════════════════════════════════════════
        q("GV.OC-01","GV","GV.OC",
          "La mission de l'organisation est-elle documentée et intégrée dans la stratégie de gestion des risques cybersécurité ?",
          "Vérifier que les décisions cyber sont alignées avec les objectifs stratégiques (document de mission approuvé COMEX).",
          "Document de mission/vision signé par la direction", "Lien explicite mission → stratégie cyber dans la politique SMSI");

        q("GV.OC-02","GV","GV.OC",
          "Les parties prenantes internes et externes (actionnaires, régulateurs, clients) sont-elles identifiées et leurs attentes formalisées ?",
          "Vérifier : DGSSI, Bank Al-Maghrib (si secteur financier), CNDP, clients contractuels, partenaires.",
          "Registre des parties prenantes", "Exigences documentées par partie prenante", "PV de réunions régulières");

        q("GV.OC-03","GV","GV.OC",
          "Les obligations légales, réglementaires et contractuelles en matière de cybersécurité sont-elles identifiées et respectées ?",
          "Loi 09-08 (données personnelles), Loi 05-20 (cybersécurité Maroc), DGSSI, réglementations sectorielles.",
          "Registre de conformité légale mis à jour", "Veille réglementaire documentée", "Clauses cyber dans les contrats");

        q("GV.OC-04","GV","GV.OC",
          "Les objectifs et services critiques dont dépendent les parties prenantes externes sont-ils identifiés et communiqués ?",
          "Quels services seraient impactés par une cyberattaque ? Les tiers en sont-ils informés (SLA, PCA) ?",
          "Catalogue des services critiques", "SLA avec engagements de disponibilité", "Plan de communication de crise");

        q("GV.OC-05","GV","GV.OC",
          "Les capacités et services dont dépend l'organisation (cloud, SaaS, opérateurs) sont-ils inventoriés et leur risque évalué ?",
          "Dépendances cloud AWS/Azure/GCP, SaaS critiques, prestataires IT, opérateurs télécom.",
          "Cartographie des dépendances externes", "Liste des fournisseurs critiques avec scoring risque");

        // ══ GV.RM ══════════════════════════════════════════
        q("GV.RM-01","GV","GV.RM",
          "Les objectifs de gestion des risques cybersécurité sont-ils formalisés, approuvés par la direction et communiqués ?",
          "Rechercher un document officiel de politique de gestion des risques signé par le COMEX ou équivalent.",
          "Politique de gestion des risques (version actuelle)", "PV d'approbation COMEX", "KRI définis et suivis");

        q("GV.RM-02","GV","GV.RM",
          "Une déclaration d'appétit au risque (Risk Appetite Statement) est-elle formalisée, approuvée et communiquée ?",
          "Ce document définit le niveau de risque acceptable. Doit être signé par la direction et revu annuellement.",
          "Risk Appetite Statement signé", "Seuils de tolérance par domaine (opérationnel, financier, réputationnel)", "Date de dernière révision < 12 mois");

        q("GV.RM-03","GV","GV.RM",
          "La gestion des risques cybersécurité est-elle intégrée dans le processus ERM (Enterprise Risk Management) ?",
          "Les risques cyber apparaissent-ils dans le registre des risques entreprise au même titre que les risques financiers ou opérationnels ?",
          "Registre ERM incluant explicitement les risques cyber", "Comité des risques avec représentation RSSI");

        q("GV.RM-04","GV","GV.RM",
          "Un framework de décision pour le traitement des risques (accepter, transférer, éviter, réduire) est-il établi et appliqué ?",
          "Existe-t-il une matrice de décision ? Les choix de traitement sont-ils documentés et approuvés ?",
          "Framework de traitement des risques documenté", "Exemples de décisions de traitement récentes", "Matrice risque/réponse");

        q("GV.RM-05","GV","GV.RM",
          "Des canaux de communication bidirectionnels sur les risques cybersécurité sont-ils établis (bottom-up et top-down) ?",
          "Comment les risques remontent vers la direction ? Comment les décisions stratégiques descendent vers les équipes ?",
          "Organigramme de remontée des risques", "Rapports risques périodiques à la direction", "Canaux d'alerte fournisseurs");

        q("GV.RM-06","GV","GV.RM",
          "Une méthodologie standardisée et documentée d'évaluation des risques est-elle établie et appliquée uniformément ?",
          "Vérifier l'utilisation d'une méthode reconnue : EBIOS Risk Manager, ISO 27005, FAIR.",
          "Méthodologie d'évaluation des risques documentée", "Registre des risques structuré selon la méthode", "Preuves d'application sur plusieurs cycles");

        q("GV.RM-07","GV","GV.RM",
          "Les opportunités positives générées par le numérique et la cybersécurité sont-elles identifiées et intégrées dans la stratégie ?",
          "Ex : certification ISO 27001 comme avantage concurrentiel, investissements cyber réduisant les primes d'assurance cyber.",
          "Analyse coût-bénéfice des investissements cyber", "Intégration des opportunités dans les rapports de risques");

        // ══ GV.RR ══════════════════════════════════════════
        q("GV.RR-01","GV","GV.RR",
          "La direction est-elle formellement responsable de la gestion des risques cybersécurité et promeut-elle une culture de vigilance ?",
          "Le leadership montre-t-il l'exemple ? La cybersécurité est-elle un sujet régulier en COMEX/Conseil d'administration ?",
          "Rapports cyber réguliers présentés au COMEX", "Déclaration d'engagement de la direction publiée", "Budget cyber approuvé au plus haut niveau");

        q("GV.RR-02","GV","GV.RR",
          "Les rôles, responsabilités et autorités en matière de cybersécurité sont-ils définis, communiqués et appliqués à travers l'organisation ?",
          "Existe-t-il une matrice RACI cybersécurité ? Les responsabilités sont-elles intégrées dans les fiches de poste ?",
          "Matrice RACI cybersécurité documentée", "Fiches de poste incluant des responsabilités cyber", "Organigramme DSI/RSSI à jour");

        q("GV.RR-03","GV","GV.RR",
          "Des ressources adéquates (budget, personnel, outils) sont-elles allouées à la cybersécurité en proportion du niveau de risque ?",
          "Vérifier le % du budget IT dédié à la cyber (benchmark : 8-15% selon secteur), les effectifs sécurité.",
          "Budget cybersécurité annuel (% du budget IT)", "Headcount équipe sécurité vs benchmark", "Plan d'investissements pluriannuels");

        q("GV.RR-04","GV","GV.RR",
          "La cybersécurité est-elle intégrée dans les pratiques RH (recrutement, onboarding, offboarding, évaluation) ?",
          "Clauses de confidentialité à l'embauche, vérification des antécédents, révocation immédiate des accès au départ.",
          "Clauses sécurité dans tous les contrats de travail", "Procédure d'offboarding avec révocation des accès", "Vérifications d'antécédents pour postes sensibles");

        // ══ GV.PO ══════════════════════════════════════════
        q("GV.PO-01","GV","GV.PO",
          "Une politique de gestion des risques cybersécurité est-elle établie, approuvée, diffusée à tous les collaborateurs et appliquée ?",
          "La politique doit être signée par la direction, accessible à tous, et son application vérifiable (formation, accusé de réception).",
          "Politique de sécurité SI (version actuelle, signée)", "Preuve de diffusion et signature par les collaborateurs", "Rapport d'audit d'application");

        q("GV.PO-02","GV","GV.PO",
          "La politique de cybersécurité est-elle révisée et mise à jour régulièrement (au moins annuellement) ?",
          "Vérifier l'historique des versions, la fréquence de révision, et le mécanisme de déclenchement (changement organisationnel, incident).",
          "Historique des versions de la politique (au moins 2 versions)", "Dernière révision < 12 mois", "Processus de revue documenté");

        // ══ GV.OV ══════════════════════════════════════════
        q("GV.OV-01","GV","GV.OV",
          "Les résultats de la gestion des risques cybersécurité sont-ils régulièrement examinés pour informer et ajuster la stratégie ?",
          "Revues de direction périodiques (trimestrielles), tableaux de bord de performance cyber, mécanismes de feedback.",
          "Rapports de performance cyber périodiques", "PV de revues de direction avec sujets cyber", "Dashboard KPI/KRI cybersécurité");

        q("GV.OV-02","GV","GV.OV",
          "La stratégie de gestion des risques est-elle révisée et ajustée pour couvrir les risques en évolution (nouvelles menaces, changements métier) ?",
          "Mécanisme de revue stratégique annuelle incluant les nouvelles menaces (threat intelligence).",
          "Processus de revue stratégique documenté", "Ajustements récents suite à changements", "Rapports threat intelligence intégrés");

        q("GV.OV-03","GV","GV.OV",
          "La performance de la gestion des risques cybersécurité est-elle évaluée et des ajustements apportés si nécessaire ?",
          "Métriques mesurées : MTTD, MTTR, nombre d'incidents, coût moyen, taux de conformité.",
          "Métriques de performance cyber définies et mesurées", "Revues périodiques des indicateurs", "Preuves d'actions correctives");

        // ══ GV.SC ══════════════════════════════════════════
        q("GV.SC-01","GV","GV.SC",
          "Un programme formel de gestion des risques de la chaîne d'approvisionnement cyber (C-SCRM) est-il établi et approuvé ?",
          "Programme couvrant tous les fournisseurs IT, SaaS, prestataires et sous-traitants avec accès à vos systèmes.",
          "Politique TPRM/C-SCRM approuvée par la direction", "Registre des fournisseurs avec scoring risque", "PV d'approbation du programme");

        q("GV.SC-02","GV","GV.SC",
          "Les rôles et responsabilités cybersécurité des fournisseurs, clients et partenaires sont-ils définis et coordonnés ?",
          "Clauses cyber dans les contrats, responsable désigné pour chaque relation fournisseur critique.",
          "Clauses cybersécurité types dans les contrats fournisseurs", "Matrice de responsabilités partagées");

        q("GV.SC-03","GV","GV.SC",
          "La gestion des risques de la chaîne d'approvisionnement est-elle intégrée dans les processus ERM globaux ?",
          "Les risques fournisseurs apparaissent-ils dans le registre ERM ? Sont-ils évalués lors des revues des risques ?",
          "Risques fournisseurs dans le registre ERM", "Processus d'intégration C-SCRM dans ERM documenté");

        q("GV.SC-04","GV","GV.SC",
          "Les fournisseurs sont-ils connus, inventoriés et priorisés selon leur criticité pour l'organisation ?",
          "Classification : Critique (accès aux données sensibles ou systèmes critiques) / Standard / Non-critique.",
          "Inventaire complet des fournisseurs avec classification de criticité", "Critères de classification documentés");

        q("GV.SC-05","GV","GV.SC",
          "Des exigences de cybersécurité pour la chaîne d'approvisionnement sont-elles définies et intégrées dans les contrats fournisseurs ?",
          "Clauses minimales : conformité RGPD/09-08, notification d'incidents sous 48h, droit d'audit, certifications exigées.",
          "Template contractuel avec clauses cyber obligatoires", "Exemples de contrats signés avec clauses vérifiées");

        q("GV.SC-06","GV","GV.SC",
          "Des due diligences cybersécurité sont-elles réalisées avant tout nouveau partenariat ou renouvellement de contrat critique ?",
          "Questionnaires de sécurité, vérification des certifications (ISO 27001, SOC2 Type II), revue des pratiques.",
          "Questionnaire d'évaluation sécurité fournisseur standardisé", "Preuves de due diligences réalisées", "Critères d'acceptation définis");

        q("GV.SC-07","GV","GV.SC",
          "Les risques posés par les fournisseurs sont-ils suivis, évalués et surveillés en continu tout au long de la relation ?",
          "Surveillance continue : alertes sécurité publiques sur les fournisseurs, revues périodiques, réévaluation après incidents.",
          "Processus de suivi continu des fournisseurs documenté", "Revues annuelles de sécurité fournisseurs", "Rapports de monitoring");

        q("GV.SC-08","GV","GV.SC",
          "Les fournisseurs et tiers critiques sont-ils inclus dans la planification, la réponse et la récupération en cas d'incident ?",
          "Sont-ils dans le plan de réponse aux incidents ? Leurs contacts d'urgence sont-ils à jour et testés ?",
          "Plan de réponse aux incidents incluant les fournisseurs critiques", "Contacts d'urgence fournisseurs à jour", "Exercices réalisés avec fournisseurs");

        q("GV.SC-09","GV","GV.SC",
          "Les pratiques de sécurité de la chaîne d'approvisionnement sont-elles surveillées tout au long du cycle de vie des produits et services ?",
          "Surveillance complète : acquisition, déploiement, maintenance, fin de vie / décommissionnement.",
          "Procédure de gestion du cycle de vie des produits tiers", "Indicateurs de performance supply chain cyber");

        q("GV.SC-10","GV","GV.SC",
          "Les plans C-SCRM couvrent-ils les activités post-contrat (fin de relation, transition, destruction certifiée des données) ?",
          "Procédure de sortie complète : révocation des accès, récupération/destruction des données, continuité de service assurée.",
          "Procédure d'offboarding fournisseur documentée", "Clauses de fin de contrat (destruction des données certifiée)", "Exemples de transitions réalisées");

        // ══ ID.AM ══════════════════════════════════════════
        q("ID.AM-01","ID","ID.AM",
          "Un inventaire à jour du matériel informatique (serveurs, postes, réseau, mobile, IoT) est-il maintenu ?",
          "Outil CMDB ou équivalent. L'inventaire doit couvrir 100% du parc. Fréquence de mise à jour : temps réel ou hebdomadaire.",
          "Inventaire matériel (CMDB) avec date de mise à jour", "Procédure de mise à jour automatique de l'inventaire", "Taux de couverture de l'inventaire");

        q("ID.AM-02","ID","ID.AM",
          "Un inventaire à jour des logiciels, services SaaS, APIs et systèmes est-il maintenu ?",
          "Applications métiers, OS, middlewares, services cloud, APIs exposées. Incluant les shadow IT identifiés.",
          "Inventaire logiciels/services avec versions et éditeurs", "Outil de gestion des licences (SAM)", "Inventaire des services cloud approuvés");

        q("ID.AM-03","ID","ID.AM",
          "Des représentations des flux de données réseau autorisés (internes et vers l'extérieur) sont-elles maintenues et à jour ?",
          "Cartographie réseau, diagrammes d'architecture, flux de données documentés entre tous les systèmes.",
          "Cartographie réseau à jour (< 6 mois)", "Matrice des flux réseau autorisés", "Validation par les équipes réseau et sécurité");

        q("ID.AM-04","ID","ID.AM",
          "Un inventaire des services fournis par des tiers (cloud, MSSP, hébergement, SaaS) est-il maintenu ?",
          "Services managés, SOC externalisé, hébergement, CDN. Avec niveau de criticité et dépendance documentés.",
          "Liste des services tiers avec niveau de criticité", "Contrats et SLA associés", "Contacts responsables par service");

        q("ID.AM-05","ID","ID.AM",
          "Les actifs sont-ils classifiés et priorisés selon leur criticité pour la mission et leur sensibilité des données portées ?",
          "Classification : Critique / Sensible / Interne / Public. Les actifs critiques bénéficient de contrôles renforcés.",
          "Politique de classification des actifs approuvée", "Actifs classifiés dans l'inventaire", "Preuve de contrôles différenciés selon la classification");

        q("ID.AM-07","ID","ID.AM",
          "Un inventaire des données (types, localisation, responsable) est-il maintenu et les métadonnées documentées ?",
          "Cartographie des données personnelles (RGPD/09-08), données confidentielles, données critiques métier.",
          "Cartographie des données (data mapping) à jour", "Registre des traitements (CNDP/RGPD)", "Classification des données documentée");

        q("ID.AM-08","ID","ID.AM",
          "Les systèmes, matériels, logiciels, services et données sont-ils gérés tout au long de leur cycle de vie (acquisition → décommissionnement) ?",
          "Processus d'acquisition sécurisée, déploiement contrôlé, maintenance régulière, et décommissionnement sécurisé (destruction données).",
          "Procédure de gestion du cycle de vie IT documentée", "Processus de décommissionnement sécurisé", "Preuves de destruction certifiée des données");

        // ══ ID.RA ══════════════════════════════════════════
        q("ID.RA-01","ID","ID.RA",
          "Les vulnérabilités dans les actifs sont-elles identifiées, validées et enregistrées de manière systématique et continue ?",
          "Scans de vulnérabilités réguliers (Qualys, Nessus, Tenable), gestion des CVE, suivi des correctifs avec SLA.",
          "Rapports de scans de vulnérabilités (fréquence)", "Registre des vulnérabilités ouvertes avec criticité", "SLA de correction par niveau CVSS");

        q("ID.RA-02","ID","ID.RA",
          "Des renseignements sur les menaces cyber (CTI) sont-ils reçus depuis des sources de threat intelligence fiables ?",
          "Abonnements CERT-MA, ANSSI, ISAC sectoriels (FS-ISAC, H-ISAC), flux STIX/TAXII, plateformes commerciales.",
          "Sources CTI actives documentées (CERT-MA, autres)", "Processus d'intégration de la CTI dans les opérations", "Preuves d'utilisation dans les décisions de sécurité");

        q("ID.RA-03","ID","ID.RA",
          "Les menaces internes (erreur humaine, malveillance) et externes (ransomware, APT, phishing) sont-elles identifiées et documentées ?",
          "Analyse des menaces sectorielles (threat landscape), historique des incidents, threat modeling des systèmes critiques.",
          "Registre des menaces identifiées avec probabilité/impact", "Analyse des menaces sectorielles récente");

        q("ID.RA-04","ID","ID.RA",
          "Les impacts potentiels et probabilités d'exploitation des vulnérabilités sont-ils systématiquement évalués et enregistrés ?",
          "Scoring standardisé : CVSS pour les vulnérabilités techniques, impact métier × probabilité pour les risques.",
          "Matrice impact/probabilité appliquée", "Scores de risque documentés dans le registre", "Méthodologie de scoring validée");

        q("ID.RA-05","ID","ID.RA",
          "Les menaces, vulnérabilités, probabilités et impacts sont-ils utilisés pour prioriser les réponses et les investissements sécurité ?",
          "Le registre des risques pilote-t-il les décisions d'investissement et les actions sécurité ?",
          "Registre des risques priorisé (Top 10 au minimum)", "Traçabilité entre risques et décisions d'investissement");

        q("ID.RA-06","ID","ID.RA",
          "Les réponses aux risques identifiés sont-elles choisies, planifiées avec un responsable et un délai, puis suivies et communiquées ?",
          "Plans de traitement des risques formalisés : qui, quoi, quand, budget. Revus trimestriellement.",
          "Plans de traitement des risques (Risk Treatment Plans)", "Suivi d'avancement avec indicateurs", "Reporting périodique à la direction");

        q("ID.RA-07","ID","ID.RA",
          "Les changements organisationnels ou techniques et les exceptions à la politique sont-ils évalués pour leur impact sur les risques ?",
          "Processus de gestion des changements (Change Management) incluant une évaluation sécurité (CAB avec RSSI).",
          "Processus Change Management avec revue sécurité formelle", "Registre des dérogations de sécurité approuvées");

        q("ID.RA-08","ID","ID.RA",
          "Des processus de réception, d'analyse et de réponse aux divulgations de vulnérabilités (internes ou externes) sont-ils établis ?",
          "Vulnerability Disclosure Program (VDP), bug bounty, traitement des signalements des chercheurs en sécurité.",
          "Politique de divulgation des vulnérabilités publiée", "Canal de signalement sécurisé (security@)", "Preuves de traitement des signalements");

        q("ID.RA-09","ID","ID.RA",
          "L'authenticité et l'intégrité du matériel et des logiciels acquis sont-elles vérifiées avant déploiement ?",
          "Vérification des signatures numériques, hashes cryptographiques, certifications constructeurs.",
          "Procédure d'acquisition sécurisée documentée", "Vérifications de signature avant déploiement", "Politique de logiciels autorisés (whitelist)");

        q("ID.RA-10","ID","ID.RA",
          "Les fournisseurs critiques font-ils l'objet d'une évaluation cybersécurité formelle avant toute acquisition de produits ou services ?",
          "Évaluation préalable : certification ISO 27001/SOC2 Type II, questionnaire de sécurité, analyse des risques.",
          "Processus d'évaluation pré-acquisition formalisé", "Questionnaire sécurité fournisseur validé", "Critères d'acceptation documentés et appliqués");

        // ══ ID.IM ══════════════════════════════════════════
        q("ID.IM-01","ID","ID.IM",
          "Des améliorations sont-elles systématiquement identifiées à partir des résultats des évaluations et audits de sécurité ?",
          "Les résultats d'audits internes, certifications, tests de pénétration génèrent-ils des plans d'action formalisés et suivis ?",
          "Plans d'action issus d'audits avec responsables et délais", "Suivi de l'avancement des recommandations", "Preuves de clôture des actions");

        q("ID.IM-02","ID","ID.IM",
          "Des améliorations sont-elles identifiées à partir des tests et exercices de sécurité (pentests, simulations de crise, red team) ?",
          "Tests de pénétration annuels, exercices de simulation de crise (tabletop), campagnes de phishing simulé.",
          "Rapports de tests de pénétration récents", "Rapports d'exercices de crise avec leçons apprises", "Plans d'amélioration post-exercice");

        q("ID.IM-03","ID","ID.IM",
          "Des améliorations sont-elles identifiées et implémentées à partir de l'analyse des processus opérationnels et des incidents ?",
          "Processus de retour d'expérience (RETEX) post-incident, amélioration continue des procédures.",
          "Processus de RETEX documenté et appliqué", "Registre des leçons apprises", "Preuves de mise à jour des procédures suite aux RETEX");

        q("ID.IM-04","ID","ID.IM",
          "Les plans de réponse aux incidents et autres plans de cybersécurité (PCA, PRA) sont-ils établis, maintenus, testés et améliorés ?",
          "CSIRP, PCA/PRA cyber, plan de communication de crise. Tous doivent être à jour et testés.",
          "Plan de réponse aux incidents (CSIRP) actuel et signé", "PCA/PRA cybersécurité", "Date du dernier test ou exercice");

        // ══ PR.AA ══════════════════════════════════════════
        q("PR.AA-01","PR","PR.AA",
          "Les identités et credentials (comptes, mots de passe, clés) de tous les utilisateurs, services et équipements sont-ils gérés centralement ?",
          "IAM centralisé (Active Directory, LDAP, Okta). Processus de création, modification, désactivation des comptes automatisé.",
          "Outil IAM/Active Directory centralisé", "Procédure de gestion du cycle de vie des comptes documentée", "Revue des comptes actifs (fréquence)");

        q("PR.AA-02","PR","PR.AA",
          "Les identités des personnes et systèmes sont-elles vérifiées et liées aux credentials avant attribution d'accès ?",
          "Processus de vérification d'identité à l'onboarding (proofing), niveaux d'assurance (LoA) selon la criticité des accès.",
          "Processus de proofing d'identité documenté par niveau de criticité", "Niveaux d'assurance définis et appliqués");

        q("PR.AA-03","PR","PR.AA",
          "Tous les utilisateurs, services et équipements sont-ils authentifiés par MFA avant d'accéder aux systèmes et données critiques ?",
          "MFA obligatoire : VPN, email pro, applications métier sensibles, portails d'administration, accès cloud.",
          "Politique d'authentification forte (MFA) documentée", "Taux de déploiement du MFA par système", "Logs d'authentification disponibles et analysés");

        q("PR.AA-04","PR","PR.AA",
          "Les assertions d'identité (tokens OAuth, certificats TLS, SAML assertions) sont-elles protégées, validées et à durée de vie limitée ?",
          "Durée de vie des tokens, révocation en temps réel, validation des certificats, mécanismes anti-rejeu.",
          "Architecture SSO/fédération documentée (SAML, OIDC)", "Politique de gestion des certificats", "Tests de validation des assertions");

        q("PR.AA-05","PR","PR.AA",
          "Les permissions, droits et autorisations sont-ils attribués et révisés selon le principe du moindre privilège (least privilege) ?",
          "Revue trimestrielle des droits, suppression des comptes orphelins, pas de comptes partagés, comptes admin nominatifs.",
          "Politique de gestion des droits d'accès documentée", "Fréquence et preuves des revues des droits (access review)", "Matrice des habilitations à jour");

        q("PR.AA-06","PR","PR.AA",
          "L'accès physique aux actifs critiques (datacenter, salles serveurs, zones sensibles) est-il géré, surveillé et contrôlé ?",
          "Badges, biométrie, sas de sécurité, journalisation des accès, escorte des visiteurs.",
          "Système de contrôle d'accès physique (badge, bio)", "Politique d'accès aux zones sensibles avec niveaux", "Journaux d'accès physique conservés et analysés");

        // ══ PR.AT ══════════════════════════════════════════
        q("PR.AT-01","PR","PR.AT",
          "Tous les collaborateurs reçoivent-ils une sensibilisation cybersécurité régulière et adaptée à leur niveau de risque ?",
          "Formation annuelle obligatoire, modules e-learning, campagnes de phishing simulé, affichage en locaux.",
          "Programme de sensibilisation annuel documenté", "Taux de complétion des formations (> 90% cible)", "Résultats des campagnes de phishing simulé");

        q("PR.AT-02","PR","PR.AT",
          "Les personnes occupant des rôles spécialisés (RSSI, SOC, développeurs, admins) reçoivent-elles une formation technique avancée ?",
          "Formations certifiantes (CISSP, CEH, CISM, OSCP), formations techniques spécifiques aux outils utilisés.",
          "Plan de formation technique de l'équipe sécurité", "Certifications obtenues et maintenues", "Budget formation cybersécurité annuel");

        // ══ PR.DS ══════════════════════════════════════════
        q("PR.DS-01","PR","PR.DS",
          "La confidentialité, l'intégrité et la disponibilité des données au repos sont-elles protégées par chiffrement ?",
          "Chiffrement des disques (BitLocker/VeraCrypt/LUKS), bases de données chiffrées, chiffrement stockage cloud.",
          "Politique de chiffrement des données au repos", "Technologies de chiffrement déployées et couverture (%)", "Gestion des clés de chiffrement (KMS)");

        q("PR.DS-02","PR","PR.DS",
          "La confidentialité et l'intégrité des données en transit sont-elles protégées (TLS 1.2/1.3 minimum) ?",
          "TLS pour toutes les communications, VPN pour accès distants, interdiction des protocoles non chiffrés (HTTP, FTP, Telnet).",
          "Politique de chiffrement en transit documentée", "Inventaire des protocoles utilisés (résultats scan TLS)", "Résultats tests de configuration TLS (SSL Labs)");

        q("PR.DS-10","PR","PR.DS",
          "La confidentialité et l'intégrité des données en cours d'utilisation (en mémoire, en traitement) sont-elles protégées ?",
          "DLP (Data Loss Prevention), protection de la mémoire, prévention des captures d'écran non autorisées, contrôle des copier-coller.",
          "Solution DLP déployée et configurée", "Politique de traitement des données sensibles", "Contrôles sur les postes de travail (DLP endpoint)");

        q("PR.DS-11","PR","PR.DS",
          "Des sauvegardes des données et systèmes critiques sont-elles créées, protégées (chiffrées), testées et stockées hors site ?",
          "Règle 3-2-1 (3 copies, 2 supports différents, 1 hors site). Test de restauration mensuel. Sauvegardes hors ligne contre ransomware.",
          "Politique de sauvegarde (fréquence, rétention, localisation)", "Procédure de test de restauration (fréquence)", "Preuves de tests de restauration réussis");

        // ══ PR.PS ══════════════════════════════════════════
        q("PR.PS-01","PR","PR.PS",
          "Des pratiques de gestion des configurations sécurisées (hardening) sont-elles établies et appliquées sur tous les systèmes ?",
          "Baselines de configuration sécurisée (CIS Benchmarks), gestion via Ansible/SCCM/GPO, vérification de conformité automatisée.",
          "Politiques de configuration de base (baselines) documentées", "Outil de gestion des configurations (Ansible, SCCM)", "Rapports de conformité aux configurations de référence");

        q("PR.PS-02","PR","PR.PS",
          "Les logiciels sont-ils maintenus, mis à jour et supprimés en fonction du risque (fin de support, vulnérabilités critiques) ?",
          "Patch management : délai de correction selon CVSS (CVSS 9-10 : 24-72h, CVSS 7-8 : < 7j). EOL planifiée.",
          "Politique de patch management avec SLA par criticité", "Rapport de conformité des correctifs appliqués", "Processus de gestion des logiciels EOL");

        q("PR.PS-03","PR","PR.PS",
          "Le matériel est-il maintenu, remplacé en fin de vie et retiré de façon sécurisée (destruction certifiée des données) ?",
          "Plan de renouvellement matériel, décommissionnement sécurisé avec destruction certifiée des supports de stockage.",
          "Plan de renouvellement matériel pluriannuel", "Procédure de décommissionnement sécurisé documentée", "Certificats de destruction des données des supports");

        q("PR.PS-04","PR","PR.PS",
          "Des journaux d'audit (logs) suffisants sont-ils générés sur tous les systèmes critiques et mis à disposition pour le monitoring ?",
          "Logs systèmes, applications, sécurité, réseau. Centralisés dans un SIEM. Rétention minimale 12 mois.",
          "Politique de journalisation (types d'événements, rétention)", "Solution de collecte et centralisation (SIEM/SOAR)", "Vérification de la disponibilité des logs sur demande");

        q("PR.PS-05","PR","PR.PS",
          "L'installation et l'exécution de logiciels non autorisés sont-elles prévenues sur tous les endpoints ?",
          "Whitelisting applicatif (AppLocker, Windows Defender Application Control), politique de restriction, contrôle des supports USB.",
          "Solution de contrôle des applications (application whitelist)", "Politique d'utilisation des supports amovibles", "Preuves d'application et d'alertes");

        q("PR.PS-06","PR","PR.PS",
          "Les pratiques de développement sécurisé (SSDLC) sont-elles intégrées dans tout le cycle de développement logiciel ?",
          "DevSecOps, SAST/DAST dans le CI/CD, revues de code sécurité, tests de pénétration avant mise en production.",
          "Politique de développement sécurisé documentée", "Outils SAST/DAST intégrés dans le pipeline CI/CD", "Résultats des tests de sécurité applicatifs");

        // ══ PR.IR ══════════════════════════════════════════
        q("PR.IR-01","PR","PR.IR",
          "Les réseaux et environnements sont-ils protégés contre les accès logiques non autorisés par une segmentation appropriée ?",
          "Segmentation réseau (VLAN, DMZ, micro-segmentation), pare-feu, zero-trust network access (ZTNA), NAC.",
          "Schéma de segmentation réseau à jour", "Règles de pare-feu documentées et révisées", "Solution NAC ou ZTNA déployée");

        q("PR.IR-02","PR","PR.IR",
          "Les actifs technologiques critiques sont-ils protégés contre les menaces environnementales (coupures courant, incendie, dégâts des eaux) ?",
          "UPS, groupes électrogènes, climatisation redondante, détection/extinction incendie, site secondaire.",
          "Certifications ou rapport d'inspection datacenter", "Équipements de protection documentés (UPS, climatisation, incendie)", "Tests de fonctionnement des équipements de protection");

        q("PR.IR-03","PR","PR.IR",
          "Des mécanismes de résilience (haute disponibilité, redondance, clustering) sont-ils implémentés pour les systèmes critiques ?",
          "Redondance des systèmes critiques (HA), RTO/RPO définis et testés, load balancing, basculement automatique.",
          "Architecture de haute disponibilité documentée", "Tests de basculement (failover) réalisés", "RTO/RPO définis et mesurés lors des tests");

        q("PR.IR-04","PR","PR.IR",
          "Une capacité de ressources adéquate (CPU, RAM, stockage, bande passante) est-elle maintenue et surveillée en permanence ?",
          "Capacity planning proactif, alertes de dépassement de seuils, prévisions de croissance.",
          "Plan de capacity management documenté", "Alertes configurées pour dépassement de capacité", "Rapports d'utilisation des ressources historiques");

        // ══ DE.CM ══════════════════════════════════════════
        q("DE.CM-01","DE","DE.CM",
          "Les réseaux et services réseau sont-ils surveillés en continu (24/7) pour détecter des événements adverses potentiels ?",
          "SIEM, IDS/IPS, NDR (Network Detection & Response), surveillance 24/7 par SOC interne ou MSSP.",
          "Solution SIEM déployée et configurée", "Périmètre de surveillance exhaustif documenté", "SLA du SOC (temps de détection, temps de réponse)");

        q("DE.CM-02","DE","DE.CM",
          "L'environnement physique (datacenter, locaux sensibles) est-il surveillé pour détecter des intrusions ou événements anormaux ?",
          "Vidéosurveillance, détecteurs d'intrusion physique, capteurs environnementaux (température, humidité), gardiennage.",
          "Système de vidéosurveillance opérationnel", "Détecteurs d'intrusion physique sur les zones sensibles", "Surveillance environnementale datacenter en temps réel");

        q("DE.CM-03","DE","DE.CM",
          "Les activités des utilisateurs et l'utilisation des technologies sont-elles surveillées pour détecter des comportements anormaux (UEBA) ?",
          "Solution UEBA (User Entity Behavior Analytics), surveillance des comptes à privilèges (PAM), DLP comportemental.",
          "Solution UEBA ou module SIEM équivalent", "Surveillance renforcée des comptes administrateurs et privilégiés", "Alertes configurées sur comportements anormaux");

        q("DE.CM-06","DE","DE.CM",
          "Les activités des prestataires de services externes ayant accès à vos systèmes sont-elles surveillées et tracées ?",
          "Solution PAM (Privileged Access Management) pour les accès tiers, journalisation des sessions, enregistrement des sessions admin.",
          "Solution PAM pour les accès tiers documentée", "Journaux d'accès des prestataires conservés (durée de rétention)", "Revues périodiques des accès tiers");

        q("DE.CM-09","DE","DE.CM",
          "Le matériel, les logiciels et les environnements des endpoints sont-ils surveillés en continu pour détecter des menaces (EDR) ?",
          "Solution EDR (Endpoint Detection & Response) sur tous les endpoints. FIM (File Integrity Monitoring) sur les serveurs critiques.",
          "Solution EDR déployée avec couverture documentée (%)", "FIM sur les serveurs et systèmes critiques", "Alertes EDR analysées et traitées par le SOC");

        // ══ DE.AE ══════════════════════════════════════════
        q("DE.AE-02","DE","DE.AE",
          "Les événements adverses potentiels détectés sont-ils systématiquement analysés pour comprendre les activités associées ?",
          "Triage structuré des alertes SIEM, analyse des IOCs (Indicators of Compromise), délais d'analyse définis et mesurés.",
          "Processus de triage des alertes SIEM documenté", "Playbooks d'analyse par type d'incident", "MTTD (Mean Time To Detect) mesuré et en amélioration");

        q("DE.AE-03","DE","DE.AE",
          "Des informations provenant de sources multiples (réseau, endpoint, logs, CTI) sont-elles corrélées pour améliorer la détection ?",
          "Règles de corrélation SIEM multi-sources, détection de patterns d'attaque (MITRE ATT&CK), enrichissement CTI.",
          "Règles de corrélation SIEM documentées et maintenues", "Sources de logs multiples intégrées (réseau, endpoint, appli, CTI)", "Incidents détectés grâce à la corrélation");

        q("DE.AE-04","DE","DE.AE",
          "L'impact et le périmètre estimés des événements adverses sont-ils évalués et documentés lors de chaque incident ?",
          "Processus d'évaluation systématique de l'impact (systèmes affectés, données compromises, utilisateurs impactés).",
          "Processus de qualification de l'impact documenté", "Matrice de criticité des incidents avec critères objectifs", "Exemples d'évaluations d'impact sur incidents passés");

        q("DE.AE-06","DE","DE.AE",
          "Les informations sur les événements adverses sont-elles transmises rapidement au personnel autorisé et aux outils appropriés ?",
          "Procédures d'escalade automatisées, notifications SIEM → tickets ITSM → alertes SMS/email, communication vers le management.",
          "Procédures d'escalade documentées avec délais", "Matrice de notification des alertes par criticité", "Preuves de notification dans les délais définis");

        q("DE.AE-07","DE","DE.AE",
          "La threat intelligence (IOCs, TTPs MITRE ATT&CK) est-elle intégrée en temps quasi-réel dans l'analyse des événements adverses ?",
          "Enrichissement automatique des alertes avec CTI, intégration SOAR/TIP, mise à jour des règles SIEM depuis CTI.",
          "Intégration CTI dans le SIEM/SOAR documentée", "Utilisation de MITRE ATT&CK dans les règles de détection", "Flux CTI consommés et mis à jour régulièrement");

        q("DE.AE-08","DE","DE.AE",
          "Des incidents sont-ils formellement déclarés et enregistrés dès que les événements adverses répondent aux critères d'incident définis ?",
          "Critères de déclaration formalisés, distinction claire alerte → événement → incident, création de ticket systématique.",
          "Critères de qualification d'un incident documentés", "Processus de déclaration d'incident formel", "Statistiques d'incidents déclarés avec tendances");

        // ══ RS.MA ══════════════════════════════════════════
        q("RS.MA-01","RS","RS.MA",
          "Le plan de réponse aux incidents est-il exécuté de manière coordonnée avec les tiers concernés dès qu'un incident est déclaré ?",
          "CSIRP testé et opérationnel. Contacts d'urgence à jour : CERT-MA, assureur cyber, partenaires, forces de l'ordre si nécessaire.",
          "CSIRP actualisé avec contacts d'urgence à jour", "Preuves d'exécution lors d'incidents réels ou d'exercices", "Accords de collaboration signés (CERT-MA, assureur)");

        q("RS.MA-02","RS","RS.MA",
          "Les rapports d'incidents sont-ils triés, analysés et validés pour distinguer les vrais incidents des faux positifs ?",
          "Processus de triage structuré avec critères objectifs, niveaux de confiance, processus de validation par un analyste senior.",
          "Processus de triage documenté avec critères de qualification", "Statistiques faux positifs / vrais positifs", "Formulaire de rapport d'incident standardisé");

        q("RS.MA-03","RS","RS.MA",
          "Les incidents validés sont-ils catégorisés et priorisés selon des critères de criticité objectifs (impact, périmètre, urgence) ?",
          "Matrice de classification des incidents (P1/P2/P3/P4) avec critères objectifs et SLA de réponse associés.",
          "Matrice de classification des incidents documentée", "SLA de réponse par niveau de criticité (P1, P2...)", "Preuves d'application de la classification sur des incidents passés");

        q("RS.MA-04","RS","RS.MA",
          "Les incidents sont-ils escaladés aux bons interlocuteurs (direction, juridique, DPO, médias) selon les procédures définies ?",
          "Arbre d'escalade formalisé : qui contacter selon la gravité, les délais imposés, les seuils de notification à la direction.",
          "Procédure d'escalade avec critères, délais et contacts", "Matrice d'escalade par type et criticité d'incident", "Preuves d'escalades réalisées lors d'incidents passés");

        q("RS.MA-05","RS","RS.MA",
          "Les critères objectifs pour décider de passer de la phase de réponse à la phase de récupération sont-ils définis et connus ?",
          "Conditions de transition clairement définies : menace éradiquée, systèmes assainis, intégrité vérifiée.",
          "Critères de transition réponse → récupération documentés", "Preuves d'application lors d'incidents passés");

        // ══ RS.AN ══════════════════════════════════════════
        q("RS.AN-03","RS","RS.AN",
          "Une analyse forensique ou investigation structurée est-elle réalisée pour établir la chronologie et les causes d'un incident ?",
          "Analyse forensique, timeline de l'incident, identification du patient zéro, reconstruction de l'attaque.",
          "Rapports d'analyse post-incident (forensique)", "Méthodologie d'analyse forensique documentée", "Outils forensiques disponibles (Volatility, Autopsy...)");

        q("RS.AN-06","RS","RS.AN",
          "Les actions menées pendant l'investigation sont-elles enregistrées et l'intégrité des preuves numériques préservée ?",
          "Chaîne de custody pour les preuves numériques, journaux d'investigation horodatés et signés, préservation des logs originaux.",
          "Procédure de préservation des preuves numériques (chain of custody)", "Journaux d'investigation horodatés et intègres", "Formation forensique de l'équipe SOC");

        q("RS.AN-07","RS","RS.AN",
          "Une analyse des causes racines (RCA) est-elle systématiquement conduite pour améliorer la posture de sécurité ?",
          "Post-mortem structuré (blameless), méthode des '5 Pourquoi', identification des défaillances systémiques et des quick wins.",
          "Rapports post-mortem avec causes racines identifiées", "Plans d'amélioration issus des RCA", "Suivi de mise en oeuvre des actions correctives");

        q("RS.AN-08","RS","RS.AN",
          "Un incident est-il formellement clôturé selon des critères prédéfinis, avec un rapport de clôture documenté ?",
          "Processus de clôture : rapport de clôture rédigé, validé par les parties prenantes, archivé et exploité pour améliorer les défenses.",
          "Processus de clôture d'incident formalisé", "Rapports de clôture d'incidents passés archivés", "Critères de clôture objectifs documentés");

        // ══ RS.CO ══════════════════════════════════════════
        q("RS.CO-02","RS","RS.CO",
          "Les parties prenantes internes et externes sont-elles notifiées des incidents dans les délais légaux et contractuels requis ?",
          "Notification CNDP (72h max pour violations de données personnelles), régulateurs sectoriels, clients contractuellement concernés.",
          "Procédure de notification des incidents documentée", "Délais de notification par type d'incident et destinataire", "Preuves de notifications réalisées dans les délais");

        q("RS.CO-03","RS","RS.CO",
          "Les informations pertinentes sur les incidents sont-elles partagées avec les partenaires de confiance, CERT-MA ou ISAC sectoriels ?",
          "Partage d'IOCs, TTPs, informations sur les campagnes d'attaque. Dans le respect des accords de confidentialité (TLP).",
          "Politique de partage d'information sur les incidents", "Accords de partage signés (NDA, protocoles TLP)", "Preuves de partages réalisés avec CERT-MA ou ISAC");

        // ══ RS.MI ══════════════════════════════════════════
        q("RS.MI-01","RS","RS.MI",
          "Les incidents sont-ils confinés et contenus rapidement pour limiter leur propagation à d'autres systèmes ?",
          "Isolation des systèmes compromis, blocage des IOCs en temps réel (firewall, proxy, DNS), coupure des connexions suspectes.",
          "Playbooks de confinement par type d'incident (ransomware, intrusion...)", "Temps de confinement moyen mesuré (MTTC)", "Preuves d'actions de confinement lors d'incidents passés");

        q("RS.MI-02","RS","RS.MI",
          "Les incidents sont-ils éradiqués de façon vérifiable pour éliminer complètement la menace des environnements affectés ?",
          "Suppression des malwares, fermeture des accès non autorisés, correction des vulnérabilités exploitées, réinitialisation des comptes.",
          "Procédures d'éradication par type de menace", "Preuves de vérification post-éradication (scans de validation)", "Checklist d'éradication par type d'incident");

        // ══ RC.RP ══════════════════════════════════════════
        q("RC.RP-01","RC","RC.RP",
          "La phase de récupération du plan est-elle activée de manière ordonnée et coordonnée avec toutes les équipes concernées ?",
          "Plan de reprise activé selon des procédures claires. Responsable de crise désigné. Coordination IT, métier et direction.",
          "Plan de récupération opérationnel (PRA/DRP) actualisé", "Preuves d'exécution lors d'incidents ou d'exercices", "Responsables de récupération désignés et formés");

        q("RC.RP-02","RC","RC.RP",
          "Les actions de récupération sont-elles sélectionnées et exécutées selon un ordre de priorité basé sur la criticité des systèmes ?",
          "Ordre de restauration prédéfini : systèmes prioritaires (cœur de métier) restaurés en premier.",
          "Ordre de priorité de restauration documenté (tier 1, 2, 3)", "Procédures de restauration spécifiques par système", "Preuves d'exécution dans l'ordre défini");

        q("RC.RP-03","RC","RC.RP",
          "L'intégrité des sauvegardes est-elle vérifiée (hash, test de restauration) avant utilisation lors d'une récupération ?",
          "Vérification systématique de l'intégrité avant restauration pour éviter de restaurer des données corrompues ou compromises.",
          "Procédure de vérification d'intégrité avant restauration", "Logs de tests de restauration réguliers", "Preuves de vérification lors de récupérations passées");

        q("RC.RP-04","RC","RC.RP",
          "Les fonctions critiques et la posture de sécurité sont-elles maintenues en mode dégradé pendant la phase de récupération ?",
          "Capacités minimales pour opérer (mode dégradé sécurisé) définies et testées. Les contrôles de sécurité restent actifs.",
          "Mode dégradé documenté pour chaque système critique", "Procédures opérationnelles sécurisées en mode dégradé");

        q("RC.RP-05","RC","RC.RP",
          "L'intégrité et le statut opérationnel normal des actifs restaurés sont-ils vérifiés et confirmés avant retour en production ?",
          "Validation fonctionnelle complète (tests métier), tests de sécurité post-restauration (scan de vulnérabilités), validation des parties prenantes.",
          "Checklist de validation post-restauration documentée", "Tests de sécurité après restauration", "Validation formelle par équipes IT et métier");

        q("RC.RP-06","RC","RC.RP",
          "La fin de la phase de récupération est-elle formellement déclarée avec un rapport documenté et archivé ?",
          "Critères de clôture objectifs, rapport de récupération complet, décision formelle de retour en production, leçons apprises.",
          "Critères de clôture de la phase récupération documentés", "Rapport de récupération post-incident archivé", "Validation direction et équipes avant déclaration de fin");

        // ══ RC.CO ══════════════════════════════════════════
        q("RC.CO-03","RC","RC.CO",
          "L'état et l'avancement de la récupération sont-ils communiqués régulièrement aux parties prenantes internes et externes désignées ?",
          "Mises à jour régulières pendant la récupération : direction, équipes opérationnelles, clients impactés, partenaires.",
          "Plan de communication pendant la récupération documenté", "Templates de messages de statut pré-approuvés", "Preuves de communications régulières lors d'incidents passés");

        q("RC.CO-04","RC","RC.CO",
          "Les communications publiques sur la récupération d'un incident sont-elles approuvées et diffusées via des canaux maîtrisés ?",
          "Porte-parole désigné, messages validés par direction + juridique, canaux officiels (site web, communiqué), pas de déclarations non autorisées.",
          "Politique de communication externe sur les incidents", "Messages types pré-approuvés par la direction et le juridique", "Processus de validation et de diffusion défini");
    }

    // ══════════════════════════════════════════════════════════
    // HELPER
    // ══════════════════════════════════════════════════════════

    private static void q(String scId, String fnId, String catId,
                           String question, String helpText, String... evidence) {
        BANK.put(scId, AuditQuestion.builder()
                .scId(scId).functionId(fnId).categoryId(catId)
                .question(question).helpText(helpText)
                .evidenceExamples(Arrays.asList(evidence))
                .build());
    }

    // ══════════════════════════════════════════════════════════
    // API PUBLIQUE
    // ══════════════════════════════════════════════════════════

    public static Map<String, AuditQuestion> getBank() { return Collections.unmodifiableMap(BANK); }

    public static Optional<AuditQuestion> getQuestion(String scId) {
        return Optional.ofNullable(BANK.get(scId));
    }

    public static int size() { return BANK.size(); }
}