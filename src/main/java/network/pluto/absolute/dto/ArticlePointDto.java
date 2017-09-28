package network.pluto.absolute.dto;

import lombok.Data;
import network.pluto.bibliotheca.models.Article;
import network.pluto.bibliotheca.models.ArticlePoint;

@Data
public class ArticlePointDto {
    private Double total;
    private Double originality;
    private Double contribution;
    private Double analysis;
    private Double expressiveness;

    public ArticlePointDto() {

    }

    public ArticlePointDto(ArticlePoint articlePoint) {
        this.total = articlePoint.getTotal();
        this.originality = articlePoint.getOriginality();
        this.contribution = articlePoint.getContribution();
        this.analysis = articlePoint.getAnalysis();
        this.expressiveness = articlePoint.getExpressiveness();
    }

    public ArticlePoint toEntity() {
        ArticlePoint articlePoint = new ArticlePoint();
        articlePoint.setTotal(this.total);
        articlePoint.setOriginality(this.originality);
        articlePoint.setContribution(this.contribution);
        articlePoint.setAnalysis(this.analysis);
        articlePoint.setExpressiveness(this.expressiveness);

        return articlePoint;
    }
}
