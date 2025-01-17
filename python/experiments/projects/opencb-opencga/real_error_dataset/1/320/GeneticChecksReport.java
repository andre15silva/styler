package org.opencb.opencga.core.models.variant;

import java.io.Serializable;
import java.util.List;

public class GeneticChecksReport implements Serializable {

    // Family ID
    private String familyId;

    // Father, mother and children IDs
    private String fatherId;
    private String motherId;
    private List<String> childrenIds;

    // Sex report
    private List<InferredSexReport> inferredSexReport;

    // Relatedness report
    private RelatednessReport relatednessReport;

    // Mendelian errors report
    private MendelianErrorsReport mendelianErrorsReport;

    public GeneticChecksReport() {
    }

    public GeneticChecksReport(String familyId, String fatherId, String motherId, List<String> childrenIds, List<InferredSexReport> inferredSexReport,
                               RelatednessReport relatednessReport, MendelianErrorsReport mendelianErrorsReport) {
        this.familyId = familyId;
        this.fatherId = fatherId;
        this.motherId = motherId;
        this.childrenIds = childrenIds;
        this.inferredSexReport = inferredSexReport;
        this.relatednessReport = relatednessReport;
        this.mendelianErrorsReport = mendelianErrorsReport;
    }

    public String getFamilyId() {
        return familyId;
    }

    public GeneticChecksReport setFamilyId(String familyId) {
        this.familyId = familyId;
        return this;
    }

    public String getFatherId() {
        return fatherId;
    }

    public GeneticChecksReport setFatherId(String fatherId) {
        this.fatherId = fatherId;
        return this;
    }

    public String getMotherId() {
        return motherId;
    }

    public GeneticChecksReport setMotherId(String motherId) {
        this.motherId = motherId;
        return this;
    }

    public List<String> getChildrenIds() {
        return childrenIds;
    }

    public GeneticChecksReport setChildrenIds(List<String> childrenIds) {
        this.childrenIds = childrenIds;
        return this;
    }

    public List<InferredSexReport> getInferredSexReport() {
        return inferredSexReport;
    }

    public GeneticChecksReport setInferredSexReport(List<InferredSexReport> inferredSexReport) {
        this.inferredSexReport = inferredSexReport;
        return this;
    }

    public RelatednessReport getRelatednessReport() {
        return relatednessReport;
    }

    public GeneticChecksReport setRelatednessReport(RelatednessReport relatednessReport) {
        this.relatednessReport = relatednessReport;
        return this;
    }

    public MendelianErrorsReport getMendelianErrorsReport() {
        return mendelianErrorsReport;
    }

    public GeneticChecksReport setMendelianErrorsReport(MendelianErrorsReport mendelianErrorsReport) {
        this.mendelianErrorsReport = mendelianErrorsReport;
        return this;
    }
}
