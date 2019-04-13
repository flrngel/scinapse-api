package io.scinapse.api.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.dto.MemberDto;
import io.scinapse.api.dto.MemberDuplicationCheckDto;
import io.scinapse.api.dto.response.Response;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.facade.MemberFacade;
import io.scinapse.api.security.jwt.JwtUser;
import io.scinapse.api.service.MemberService;
import io.scinapse.api.validator.Update;
import io.scinapse.domain.data.scinapse.model.Member;
import io.scinapse.domain.data.scinapse.model.MemberSavedFilter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

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
        Member member;
        if (memberDto.getToken() != null) {
            member = memberFacade.createOauthMember2(response, memberDto);
        } else {
            member = memberFacade.createOauthMember(response, memberDto);
        }
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

    @RequestMapping(value = "/members/me/saved-filters", method = RequestMethod.GET)
    public Response<List<MemberSavedFilter.SavedFilter>> getSavedFilters(JwtUser user) {
        Member member = memberFacade.loadMember(user);
        return Response.success(memberService.getSavedFilters(member));
    }

    @RequestMapping(value = "/members/me/saved-filters", method = RequestMethod.PUT)
    public Response<List<MemberSavedFilter.SavedFilter>> updateSavedFilters(JwtUser user, @RequestBody @Valid SavedFilterWrapper wrapper) {
        Member member = memberFacade.loadMember(user);
        return Response.success(memberService.updateSavedFilters(member, wrapper.getSavedFilters()));
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

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/members/{memberId}", method = RequestMethod.DELETE)
    public Response delete(@PathVariable long memberId) {
        memberFacade.delete(memberId);
        return Response.success();
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

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @Getter
    private static class SavedFilterWrapper {
        @Valid
        private List<MemberSavedFilter.SavedFilter> savedFilters;

        public void setSavedFilters(List<MemberSavedFilter.SavedFilter> savedFilters) {
            this.savedFilters = savedFilters == null ? new ArrayList<>() : savedFilters;
        }
    }

}
