package network.pluto.absolute.controller;

import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.service.MemberService;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MemberController {

    @Autowired
    private MemberService memberService;

    @RequestMapping(value = "/members", method = RequestMethod.POST)
    public MemberDto create(@RequestBody MemberDto memberDto) {
        Member member = MemberDto.toEntity(memberDto);
        Member saved = memberService.save(member);
        return MemberDto.fromEntity(saved);
    }

    @RequestMapping(value = "/members", method = RequestMethod.GET)
    public List<Member> getMembers() {
        return memberService.getAll();
    }

    @RequestMapping(value = "/members/{id}", method = RequestMethod.GET)
    public Member getMember(@PathVariable long id) {
        return memberService.getMember(id);
    }
}
