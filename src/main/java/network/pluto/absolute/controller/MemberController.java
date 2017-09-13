package network.pluto.absolute.controller;

import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.service.MemberService;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    public List<MemberDto> getMembers() {
        return memberService.getAll().stream()
                .map(MemberDto::fromEntity)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/members/{id}", method = RequestMethod.GET)
    public MemberDto getMember(@PathVariable long id) {
        Member member = memberService.getMember(id);
        return MemberDto.fromEntity(member);
    }
}
