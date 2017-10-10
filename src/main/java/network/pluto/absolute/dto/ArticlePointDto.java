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

    public ArticlePointDto(ArticlePoint point) {
        this.total = point.getTotal();
        this.originality = point.getOriginality();
        this.contribution = point.getContribution();
        this.analysis = point.getAnalysis();
        this.expressiveness = point.getExpressiveness();
    }
}
