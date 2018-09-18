package io.scinapse.api.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.scinapse.api.dto.MemberDto;
import io.scinapse.api.dto.MemberDuplicationCheckDto;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.facade.MemberFacade;
import io.scinapse.api.model.Member;
import io.scinapse.api.security.jwt.JwtUser;
import io.scinapse.api.service.MemberService;
import io.scinapse.api.validator.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberFacade memberFacade;

    @RequestMapping(value = "/members", method = RequestMethod.POST)
    public MemberDto create(HttpServletResponse response, @RequestBody @Valid MemberDto memberDto) {
        Member member = memberFacade.create(response, memberDto);
        return new MemberDto(member);
    }

    @RequestMapping(value = "/members/oauth", method = RequestMethod.POST)
    public MemberDto createOauthMember(HttpServletResponse response, @RequestBody @Valid MemberDto memberDto) {
        Member member = memberFacade.createOauthMember(response, memberDto);
        return new MemberDto(member);
    }

    @RequestMapping(value = "/members/{memberId}", method = RequestMethod.GET)
    public MemberDto getMembers(@ApiIgnore JwtUser user,
                                @PathVariable long memberId) {
        return memberFacade.getDetail(memberId);
    }

    @RequestMapping(value = "/members/me", method = RequestMethod.PUT)
    public MemberDto updateMember(@ApiIgnore JwtUser user,
                                  @RequestBody @Validated(Update.class) MemberDto memberDto) {
        Member old = memberService.findMember(user.getId());
        if (old == null) {
            throw new ResourceNotFoundException("Member not found: " + user.getId());
        }

        Member updated = memberDto.toEntity();

        Member saved = memberService.updateMember(old, updated);
        return new MemberDto(saved);
    }

    @RequestMapping(value = "/members/me/password", method = RequestMethod.PUT)
    public Result updatePassword(@ApiIgnore JwtUser user,
                                 @RequestBody @Valid MemberDto.PasswordWrapper password) {
        Member old = memberService.findMember(user.getId());
        if (old == null) {
            throw new ResourceNotFoundException("Member not found: " + user.getId());
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

    @RequestMapping(value = "/members/password-token", method = RequestMethod.POST)
    public Result generateToken(@RequestBody EmailWrapper email) {
        Member member = memberService.findByEmail(email.email);
        if (member == null) {
            throw new ResourceNotFoundException("Member not found: " + email.email);
        }

        memberFacade.generateToken(member);
        return Result.success();
    }

    @RequestMapping(value = "/members/reset-password", method = RequestMethod.POST)
    public Result resetPassword(@RequestBody @Valid TokenWrapper token) {
        memberFacade.resetPassword(token.token, token.password);
        return Result.success();
    }

    private static class EmailWrapper {
        @JsonProperty
        private String email;
    }

    private static class TokenWrapper {

        @JsonProperty
        @NotNull
        private String token;

        @JsonProperty
        @Size(min = 8, message = "password must be greater than or equal to 8")
        @NotNull
        private String password;

    }

}
