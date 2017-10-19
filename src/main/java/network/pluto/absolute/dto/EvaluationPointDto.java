package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import network.pluto.bibliotheca.models.EvaluationPoint;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Data
public class EvaluationPointDto {

    @ApiModelProperty(readOnly = true)
    private double total;

    @ApiModelProperty(required = true)
    @NotNull
    private int originality;

    @ApiModelProperty(required = true)
    @NotNull
    private int significance;

    @ApiModelProperty(required = true)
    @NotNull
    private int validity;

    @ApiModelProperty(required = true)
    @NotNull
    private int organization;

    private String originalityComment;
    private String significanceComment;
    private String validityComment;
    private String organizationComment;

    public EvaluationPointDto(EvaluationPoint point) {
        this.total = point.getTotal();

        this.originality = point.getOriginality();
        this.significance = point.getSignificance();
        this.validity = point.getValidity();
        this.organization = point.getOrganization();

        this.originalityComment = point.getOriginalityComment();
        this.significanceComment = point.getSignificanceComment();
        this.validityComment = point.getValidityComment();
        this.organizationComment = point.getOrganizationComment();
    }

    public EvaluationPoint toEntity() {
        EvaluationPoint point = new EvaluationPoint();

        point.setOriginality(this.originality);
        point.setSignificance(this.significance);
        point.setValidity(this.validity);
        point.setOrganization(this.organization);

        point.setOriginalityComment(this.originalityComment);
        point.setSignificanceComment(this.significanceComment);
        point.setValidityComment(this.validityComment);
        point.setOrganizationComment(this.organizationComment);

        point.updateTotal();

        return point;
    }
}
