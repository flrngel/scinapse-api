package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import network.pluto.bibliotheca.models.ArticlePoint;

@NoArgsConstructor
@Data
public class ArticlePointDto {

    @ApiModelProperty(readOnly = true)
    private Double total;

    @ApiModelProperty(readOnly = true)
    private Double originality;

    @ApiModelProperty(readOnly = true)
    private Double contribution;

    @ApiModelProperty(readOnly = true)
    private Double analysis;

    @ApiModelProperty(readOnly = true)
    private Double expressiveness;

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
