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
    private int contribution;

    @ApiModelProperty(required = true)
    @NotNull
    private int analysis;

    @ApiModelProperty(required = true)
    @NotNull
    private int expressiveness;

    private String originalityComment;
    private String contributionComment;
    private String analysisComment;
    private String expressivenessComment;

    public EvaluationPointDto(EvaluationPoint point) {
        this.total = point.getTotal();

        this.originality = point.getOriginality();
        this.contribution = point.getContribution();
        this.analysis = point.getAnalysis();
        this.expressiveness = point.getExpressiveness();

        this.originalityComment = point.getOriginalityComment();
        this.contributionComment = point.getContributionComment();
        this.analysisComment = point.getAnalysisComment();
        this.expressivenessComment = point.getExpressivenessComment();
    }

    public EvaluationPoint toEntity() {
        EvaluationPoint point = new EvaluationPoint();

        point.setOriginality(this.originality);
        point.setContribution(this.contribution);
        point.setAnalysis(this.analysis);
        point.setExpressiveness(this.expressiveness);

        point.setOriginalityComment(this.originalityComment);
        point.setContributionComment(this.contributionComment);
        point.setAnalysisComment(this.analysisComment);
        point.setExpressivenessComment(this.expressivenessComment);

        point.updateTotal();

        return point;
    }
}
