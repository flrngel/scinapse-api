package network.pluto.absolute.controller;

import network.pluto.absolute.dto.ArticleDto;
import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.dto.MemberDuplicationCheckDto;
import network.pluto.absolute.dto.ReviewDto;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.facade.MemberFacade;
import network.pluto.absolute.security.jwt.JwtUser;
import network.pluto.absolute.service.ArticleService;
import network.pluto.absolute.service.MemberService;
import network.pluto.absolute.service.ReviewService;
import network.pluto.absolute.service.TransactionService;
import network.pluto.absolute.validator.Update;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

@RestController
public class MemberController {

    private final MemberService memberService;
    private final ArticleService articleService;
    private final ReviewService reviewService;
    private final TransactionService transactionService;
    private final MemberFacade memberFacade;

    @Autowired
    public MemberController(MemberService memberService,
                            ArticleService articleService,
                            ReviewService reviewService,
                            TransactionService transactionService,
                            MemberFacade memberFacade) {
        this.memberService = memberService;
        this.articleService = articleService;
        this.reviewService = reviewService;
        this.transactionService = transactionService;
        this.memberFacade = memberFacade;
    }

    @RequestMapping(value = "/members", method = RequestMethod.POST)
    public MemberDto create(@RequestBody @Valid MemberDto memberDto) {
        Member exist = memberService.findByEmail(memberDto.getEmail());
        if (exist != null) {
            throw new BadRequestException("Email already exists");
        }

        // extract institution
        memberDto.setInstitution(extractInstitution(memberDto.getEmail()));

        Member saved = memberService.saveMember(memberDto.toEntity());

        // send transaction
        transactionService.createWallet(saved);

        return new MemberDto(saved);
    }

    @RequestMapping(value = "/members/{memberId}", method = RequestMethod.GET)
    public MemberDto getMembers(@ApiIgnore JwtUser user,
                                @PathVariable long memberId) {
        return memberFacade.getDetail(memberId);
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

    @RequestMapping(value = "/members/{memberId}/reviews", method = RequestMethod.GET)
    public Page<ReviewDto> getMyReviews(@ApiIgnore JwtUser user,
                                        @PathVariable long memberId,
                                        @PageableDefault Pageable pageable) {
        Member member = memberService.findMember(memberId);
        if (member == null) {
            throw new ResourceNotFoundException("Member not found");
        }

        return reviewService.findByCreatedBy(member, pageable).map(ReviewDto::new);
    }

    @RequestMapping(value = "/members/{memberId}", method = RequestMethod.PUT)
    public MemberDto updateArticle(@ApiIgnore JwtUser user,
                                   @PathVariable long memberId,
                                   @RequestBody @Validated(Update.class) MemberDto memberDto) {
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
    public Result updatePassword(@ApiIgnore JwtUser user,
                                 @PathVariable long memberId,
                                 @RequestBody @Valid MemberDto.PasswordWrapper password) {
        if (memberId != user.getId()) {
            throw new AuthorizationServiceException("Members can update own password only");
        }

        Member old = memberService.findMember(memberId);
        if (old == null) {
            throw new ResourceNotFoundException("Member not found");
        }

        memberService.updatePassword(old, password.getPassword());

        return Result.success();
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
}
