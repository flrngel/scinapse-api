package network.pluto.absolute.controller;

import network.pluto.absolute.dto.EvaluationDto;
import network.pluto.absolute.service.EvaluationService;
import network.pluto.bibliotheca.models.Evaluation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/article/{articleId}/evaluations")
public class EvaluationController {

    private final EvaluationService evaluationService;

    @Autowired
    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public EvaluationDto createEvaluation(@PathVariable long articleId,
                                          @RequestBody EvaluationDto evaluationDto) {

        Evaluation evaluation = this.evaluationService.saveEvaluation(articleId, evaluationDto.toEntity());

        return new EvaluationDto(evaluation);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<EvaluationDto> getEvaluations(@PathVariable long articleId) {
        List<Evaluation> evaluations = this.evaluationService.getEvaluations(articleId);

        return evaluations.stream().map(EvaluationDto::new).collect(Collectors.toList());
    }
}
