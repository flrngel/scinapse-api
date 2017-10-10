package network.pluto.absolute.controller;

import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.dto.MemberDuplicationCheckDto;
import network.pluto.absolute.security.jwt.JwtAuthenticationToken;
import network.pluto.absolute.service.MemberService;
import network.pluto.absolute.validator.MemberDuplicationValidator;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
public class MemberController {

    private final MemberService memberService;
    private final MemberDuplicationValidator memberDuplicationValidator;

    @Autowired
    public MemberController(MemberService memberService,
                            MemberDuplicationValidator memberDuplicationValidator) {
        this.memberService = memberService;
        this.memberDuplicationValidator = memberDuplicationValidator;
    }

    @InitBinder("memberDto")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(memberDuplicationValidator);
    }

    @RequestMapping(value = "/members", method = RequestMethod.POST)
    public MemberDto create(@RequestBody @Valid MemberDto memberDto) {
        Member member = memberDto.toEntity();
        Member saved = memberService.save(member);

        return new MemberDto(saved);
    }

    @RequestMapping(value = "/members/info", method = RequestMethod.GET)
    public MemberDto getMembers(Principal principal) {
        Member member = (Member) ((JwtAuthenticationToken) principal).getPrincipal();
        Member one = memberService.getMember(member.getMemberId());
        return new MemberDto(one);
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
