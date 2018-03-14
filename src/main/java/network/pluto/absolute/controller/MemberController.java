package network.pluto.absolute.controller;

import lombok.RequiredArgsConstructor;
import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.dto.MemberDuplicationCheckDto;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.facade.MemberFacade;
import network.pluto.absolute.security.jwt.JwtUser;
import network.pluto.absolute.service.MemberService;
import network.pluto.absolute.validator.Update;
import network.pluto.bibliotheca.models.Member;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

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
            throw new ResourceNotFoundException("Member not found");
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

}
