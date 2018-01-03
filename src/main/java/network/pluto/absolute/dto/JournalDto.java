package network.pluto.absolute.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.bibliotheca.models.Journal;

@NoArgsConstructor
@Getter
@Setter
public class JournalDto {

    private long id;
    private String fullTitle;
    private String jcrAbbrTitle;
    private String isoAbbrTitle;
    private String issn;
    private String language;
    private String country;
    private String edition;
    private Boolean isOpenAccess;
    private Double journalImpactFactor;
    private Integer issues;
    private Integer totalCites;
    private Double fiveYearImpactFactor;
    private Double articleInfluenceScore;
    private Double averageJournalImpactFactorPercentile;
    private String citableItems;
    private String citedHalfLife;
    private String citingHalfLife;
    private Double eigenfactorScore;
    private Double immediacyIndex;
    private Double impactFactorWithoutJournalSelfCites;
    private Double normalizedEigenfactor;
    private Double percentArticlesInCitableItems;

    public JournalDto(Journal journal) {
        this.id = journal.getId();
        this.fullTitle = journal.getFullJournalTitle();
        this.jcrAbbrTitle = journal.getJcrAbbreviatedTitle();
        this.isoAbbrTitle = journal.getIsoAbbrTitle();
        this.issn = journal.getIssn();
        this.language = journal.getLanguage();
        this.country = journal.getCountry();
        this.edition = journal.getEdition();
        this.isOpenAccess = journal.getIsOpenAccess();
        this.journalImpactFactor = journal.getJournalImpactFactor();
        this.issues = journal.getIssues();
        this.totalCites = journal.getTotalCites();
        this.fiveYearImpactFactor = journal.getFiveYearImpactFactor();
        this.articleInfluenceScore = journal.getArticleInfluenceScore();
        this.averageJournalImpactFactorPercentile = journal.getAverageJournalImpactFactorPercentile();
        this.citableItems = journal.getCitableItems();
        this.citedHalfLife = journal.getCitedHalfLife();
        this.citingHalfLife = journal.getCitingHalfLife();
        this.eigenfactorScore = journal.getEigenfactorScore();
        this.immediacyIndex = journal.getImmediacyIndex();
        this.impactFactorWithoutJournalSelfCites = journal.getImpactFactorWithoutJournalSelfCites();
        this.normalizedEigenfactor = journal.getNormalizedEigenfactor();
        this.percentArticlesInCitableItems = journal.getPercentArticlesInCitableItems();
    }
}
