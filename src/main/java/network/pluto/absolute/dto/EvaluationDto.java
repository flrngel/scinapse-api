package network.pluto.absolute.dto;

import lombok.Data;
import network.pluto.bibliotheca.models.Evaluation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class EvaluationDto {
    private long id;
    private MemberDto createdBy;
    private EvaluationPointDto point;
    private LocalDateTime createdAt;
    private List<CommentDto> comments;

    public EvaluationDto() {
    }

    public EvaluationDto(Evaluation evaluation) {
        this.id = evaluation.getEvaluationId();
        this.createdBy = MemberDto.fromEntity(evaluation.getMember());
        this.point = this.generatePointDto(evaluation);
        this.createdAt = evaluation.getCreatedAt();
        this.comments = evaluation.getComments().stream().map(CommentDto::new).collect(Collectors.toList());
    }

    public Evaluation toEntity() {
        Evaluation evaluation = new Evaluation();
        evaluation.setMember(this.createdBy.toEntity());
        this.writePointValuesOn(evaluation);
        evaluation.setComments(this.comments.stream().map(CommentDto::toEntity).collect(Collectors.toList()));

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
        evaluation.setTotal(this.point.total);
        evaluation.setOriginality(this.point.originality);
        evaluation.setContribution(this.point.contribution);
        evaluation.setAnalysis(this.point.analysis);
        evaluation.setExpressiveness(this.point.expressiveness);
        evaluation.setOriginalityComment(this.point.originalityComment);
        evaluation.setContributionComment(this.point.contributionComment);
        evaluation.setAnalysisComment(this.point.analysisComment);
        evaluation.setExpressivenessComment(this.point.expressivenessComment);
    }

    @Data
    private class EvaluationPointDto {
        private Double total;
        private Double originality;
        private Double contribution;
        private Double analysis;
        private Double expressiveness;
        private String originalityComment;
        private String contributionComment;
        private String analysisComment;
        private String expressivenessComment;
    }
}
