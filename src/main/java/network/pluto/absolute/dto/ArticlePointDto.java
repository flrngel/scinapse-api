package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import network.pluto.bibliotheca.models.ArticlePoint;

@NoArgsConstructor
@ToString
@Getter
@Setter
public class ArticlePointDto {

    @ApiModelProperty(readOnly = true)
    private Double total;

    @ApiModelProperty(readOnly = true)
    private Double originality;

    @ApiModelProperty(readOnly = true)
    private Double significance;

    @ApiModelProperty(readOnly = true)
    private Double validity;

    @ApiModelProperty(readOnly = true)
    private Double organization;

    public ArticlePointDto(ArticlePoint point) {
        this.total = point.getTotal();
        this.originality = point.getOriginality();
        this.significance = point.getSignificance();
        this.validity = point.getValidity();
        this.organization = point.getOrganization();
    }
}
