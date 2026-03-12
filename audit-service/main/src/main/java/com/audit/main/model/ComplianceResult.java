package com.hsgroup.audit.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Les 5 niveaux de conformité NIST CSF 2.0.
 * Chaque valeur porte son poids pour le calcul du score de maturité.
 */
public enum ComplianceResult {

    NOT_ASSESSED       ("not_assessed",         "Non Évalué",                  0.0,  false),
    COMPLIANT          ("compliant",             "Conforme",                    1.0,  true),
    PARTIALLY_COMPLIANT("partially_compliant",   "Partiellement Conforme",      0.5,  true),
    NON_COMPLIANT      ("non_compliant",         "Non Conforme",                0.0,  true),
    NOT_APPLICABLE     ("not_applicable",        "Non Applicable",              null, true);
    // NOT_APPLICABLE → null = exclu du calcul du score (ni numérateur ni dénominateur)

    private final String  value;
    private final String  labelFr;
    private final Double  scoreWeight;   // null = exclu
    private final boolean assessed;     // a-t-il été évalué par l'auditeur ?

    ComplianceResult(String value, String labelFr, Double scoreWeight, boolean assessed) {
        this.value       = value;
        this.labelFr     = labelFr;
        this.scoreWeight = scoreWeight;
        this.assessed    = assessed;
    }

    @JsonValue
    public String  getValue()       { return value; }
    public String  getLabelFr()     { return labelFr; }
    public Double  getScoreWeight() { return scoreWeight; }
    public boolean isAssessed()     { return assessed; }

    /** Nécessite une action corrective. */
    public boolean needsAction() {
        return this == NON_COMPLIANT || this == PARTIALLY_COMPLIANT;
    }

    public static ComplianceResult fromValue(String value) {
        for (ComplianceResult r : values()) {
            if (r.value.equalsIgnoreCase(value)) return r;
        }
        throw new IllegalArgumentException("ComplianceResult inconnu : " + value);
    }
}