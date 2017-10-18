package network.pluto.absolute.controller;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
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
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

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
        // extract institution
        memberDto.setInstitution(extractInstitution(memberDto.getEmail()));

        Member member = memberDto.toEntity();

        Member saved = memberService.saveMember(member);
        return new MemberDto(saved);
    }

    @RequestMapping(value = "/members/{memberId}", method = RequestMethod.GET)
    public MemberDto getMembers(@ApiIgnore JwtUser user,
                                @PathVariable long memberId) {
        Member member = memberService.findMember(memberId);
        if (member == null) {
            throw new ResourceNotFoundException("Member not found");
        }

        return new MemberDto(member);
    }

    @RequestMapping(value = "/members/{memberId}/articles", method = RequestMethod.GET)
    public Page<ArticleDto> getMyArticles(@ApiIgnore JwtUser user,
                                          @PathVariable long memberId,
                                          @PageableDefault Pageable pageable) {
        Member member = memberService.findMember(memberId);
        if (member == null) {
            throw new ResourceNotFoundException("Member not found");
        }

        return articleService.findByCreatedBy(member, pageable).map(ArticleDto::new);
    }

    @RequestMapping(value = "/members/{memberId}/evaluations", method = RequestMethod.GET)
    public Page<EvaluationDto> getMyEvaluations(@ApiIgnore JwtUser user,
                                                @PathVariable long memberId,
                                                @PageableDefault Pageable pageable) {
        Member member = memberService.findMember(memberId);
        if (member == null) {
            throw new ResourceNotFoundException("Member not found");
        }

        return evaluationService.findByCreatedBy(member, pageable).map(EvaluationDto::new);
    }

    @RequestMapping(value = "/members/{memberId}", method = RequestMethod.PUT)
    public MemberDto updateArticle(@ApiIgnore JwtUser user,
                                   @PathVariable long memberId,
                                   @RequestBody MemberDto memberDto) {
        if (memberId != user.getId()) {
            throw new AuthorizationServiceException("Members can update own profile only");
        }

        Member old = memberService.findMember(memberId);
        if (old == null) {
            throw new ResourceNotFoundException("Member not found");
        }

        Member updated = memberDto.toEntity();

        Member saved = memberService.updateMember(old, updated);
        return new MemberDto(saved);
    }

    @RequestMapping(value = "/members/{memberId}/password", method = RequestMethod.PUT)
    public Object updatePassword(@ApiIgnore JwtUser user,
                                 @PathVariable long memberId,
                                 @RequestBody @Valid PasswordWrapper password) {
        if (memberId != user.getId()) {
            throw new AuthorizationServiceException("Members can update own password only");
        }

        Member old = memberService.findMember(memberId);
        if (old == null) {
            throw new ResourceNotFoundException("Member not found");
        }

        memberService.updatePassword(old, password.getPassword());

        Map<String, String> result = new HashMap<>();
        result.put("result", "success");
        return result;
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

    private String extractInstitution(String email) {
        if (StringUtils.isEmpty(email)) {
            return null;
        }

        String[] split = email.split("@");
        if (split.length != 2) {
            return null;
        }

        String host = split[1];
        String institution = host.split("\\.")[0];
        if (StringUtils.isEmpty(institution)) {
            return null;
        }

        // capitalize first letter only
        return StringUtils.capitalize(institution.toLowerCase());
    }

    @Getter
    @Setter
    private static class PasswordWrapper {
        @ApiModelProperty(required = true)
        @Size(min = 8, message = "password must be greater than or equal to 8")
        @NotNull
        private String password;
    }
}
