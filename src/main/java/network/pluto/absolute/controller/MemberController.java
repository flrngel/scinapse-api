package network.pluto.absolute.controller;

import network.pluto.absolute.dto.ArticleDto;
import network.pluto.absolute.dto.EvaluationDto;
import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.dto.MemberDuplicationCheckDto;
import network.pluto.absolute.security.jwt.JwtUser;
import network.pluto.absolute.service.ArticleService;
import network.pluto.absolute.service.EvaluationService;
import network.pluto.absolute.service.MemberService;
import network.pluto.absolute.validator.MemberDuplicationValidator;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MemberController {

    private final MemberService memberService;
    private final MemberDuplicationValidator memberDuplicationValidator;
    private final ArticleService articleService;
    private final EvaluationService evaluationService;

    @Autowired
    public MemberController(MemberService memberService,
                            MemberDuplicationValidator memberDuplicationValidator,
                            ArticleService articleService,
                            EvaluationService evaluationService) {
        this.memberService = memberService;
        this.memberDuplicationValidator = memberDuplicationValidator;
        this.articleService = articleService;
        this.evaluationService = evaluationService;
    }

    @InitBinder("memberDto")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(memberDuplicationValidator);
    }

    @RequestMapping(value = "/members", method = RequestMethod.POST)
    public MemberDto create(@RequestBody @Valid MemberDto memberDto) {
        Member member = memberDto.toEntity();
        Member saved = memberService.saveMember(member);

        return new MemberDto(saved);
    }

    @RequestMapping(value = "/members/my", method = RequestMethod.GET)
    public MemberDto getMembers(@ApiIgnore JwtUser user) {
        Member one = memberService.getMember(user.getId());
        return new MemberDto(one);
    }

    @RequestMapping(value = "/members/my/articles", method = RequestMethod.GET)
    public List<ArticleDto> getMyArticles(@ApiIgnore JwtUser user) {
        Member member = memberService.getMember(user.getId());
        return articleService.findByCreatedBy(member)
                .stream()
                .map(ArticleDto::new)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/members/my/evaluations", method = RequestMethod.GET)
    public List<EvaluationDto> getMyEvaluations(@ApiIgnore JwtUser user) {
        Member member = memberService.getMember(user.getId());
        return evaluationService.findByCreatedBy(member)
                .stream()
                .map(EvaluationDto::new)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/members/checkDuplication", method = RequestMethod.GET)
    public MemberDuplicationCheckDto checkDuplication(@RequestParam String email) {
        MemberDuplicationCheckDto dto = new MemberDuplicationCheckDto();
        dto.setEmail(email);

        Member member = memberService.findByEmail(email);
        if (member == null) {
            dto.setDuplicated(false);
        } else {
            dto.setDuplicated(true);
            dto.setMessage("duplicated email.");
        }

        return dto;
    }
}
