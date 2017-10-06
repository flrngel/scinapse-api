package network.pluto.absolute.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import network.pluto.bibliotheca.models.Evaluation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Data
public class EvaluationDto {

    @ApiModelProperty(readOnly = true)
    private long id;

    @ApiModelProperty(readOnly = true)
    private MemberDto createdBy;

    @ApiModelProperty(required = true)
    private EvaluationPointDto point;

    @ApiModelProperty(readOnly = true)
    private LocalDateTime createdAt;

    @ApiModelProperty(readOnly = true)
    private List<CommentDto> comments;

    public EvaluationDto(Evaluation evaluation) {
        this.id = evaluation.getEvaluationId();
        this.createdBy = new MemberDto(evaluation.getMember());
        this.point = this.generatePointDto(evaluation);
        this.createdAt = evaluation.getCreatedAt();
        this.comments = evaluation.getComments().stream().map(CommentDto::new).collect(Collectors.toList());
    }

    public Evaluation toEntity() {
        Evaluation evaluation = new Evaluation();
        if (this.createdBy != null) {
            evaluation.setMember(this.createdBy.toEntity());
        }
        this.writePointValuesOn(evaluation);

        if (this.comments != null) {
            evaluation.setComments(this.comments.stream().map(CommentDto::toEntity).collect(Collectors.toList()));
        }

        return evaluation;
    }

    private EvaluationPointDto generatePointDto(Evaluation evaluation) {
        EvaluationPointDto evaluationPointDto = new EvaluationPointDto();

        evaluationPointDto.setTotal(evaluation.getTotal());
        evaluationPointDto.setOriginality(evaluation.getOriginality());
        evaluationPointDto.setContribution(evaluation.getContribution());
        evaluationPointDto.setAnalysis(evaluation.getAnalysis());
        evaluationPointDto.setExpressiveness(evaluation.getExpressiveness());

        evaluationPointDto.setOriginalityComment(evaluation.getOriginalityComment());
        evaluationPointDto.setContributionComment(evaluation.getContributionComment());
        evaluationPointDto.setAnalysisComment(evaluation.getAnalysisComment());
        evaluationPointDto.setExpressivenessComment(evaluation.getExpressivenessComment());

        return evaluationPointDto;
    }

    private void writePointValuesOn(Evaluation evaluation) {
        if (this.point == null) {
            return;
        }

        evaluation.setOriginality(this.point.originality);
        evaluation.setContribution(this.point.contribution);
        evaluation.setAnalysis(this.point.analysis);
        evaluation.setExpressiveness(this.point.expressiveness);

        evaluation.setTotal(this.point.getAverage());

        evaluation.setOriginalityComment(this.point.originalityComment);
        evaluation.setContributionComment(this.point.contributionComment);
        evaluation.setAnalysisComment(this.point.analysisComment);
        evaluation.setExpressivenessComment(this.point.expressivenessComment);
    }

    @Data
    private class EvaluationPointDto {
        private Double total;

        @ApiModelProperty(required = true)
        private Double originality;

        @ApiModelProperty(required = true)
        private Double contribution;

        @ApiModelProperty(required = true)
        private Double analysis;

        @ApiModelProperty(required = true)
        private Double expressiveness;

        private String originalityComment;
        private String contributionComment;
        private String analysisComment;
        private String expressivenessComment;

        double getAverage() {
            double total = originality + contribution + analysis + expressiveness;
            return total / 4;
        }
    }
}
