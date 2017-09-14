package network.pluto.absolute.controller;

import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.dto.MemberDuplicationCheckDto;
import network.pluto.absolute.security.TokenHelper;
import network.pluto.absolute.service.LoginUserDetails;
import network.pluto.absolute.service.MemberService;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MemberController {

    private final MemberService memberService;
    private final AuthenticationManager authenticationManager;
    private final TokenHelper tokenHelper;

    @Autowired
    public MemberController(MemberService memberService, AuthenticationManager authenticationManager, TokenHelper tokenHelper) {
        this.memberService = memberService;
        this.authenticationManager = authenticationManager;
        this.tokenHelper = tokenHelper;
    }

    @Value("${jwt.cookie}")
    private String cookie;

    @Value("${jwt.expires-in}")
    private int expireIn;

    @RequestMapping(value = "/members", method = RequestMethod.POST)
    public MemberDto create(@RequestBody MemberDto memberDto, HttpServletResponse response) {
        Member member = MemberDto.toEntity(memberDto);
        Member saved = memberService.save(member);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(memberDto.getEmail(), memberDto.getPassword());
        Authentication authentication = authenticationManager.authenticate(token);

        LoginUserDetails user = (LoginUserDetails) authentication.getPrincipal();

        String jws = tokenHelper.generateToken(user.getMember());

        Cookie authCookie = new Cookie(cookie, jws);
        authCookie.setPath("/");
        authCookie.setHttpOnly(true);
        authCookie.setMaxAge(expireIn);
        response.addCookie(authCookie);

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

    @RequestMapping(value = "/members/checkDuplication", method = RequestMethod.GET)
    public MemberDuplicationCheckDto checkDuplication(@RequestParam String email, HttpServletResponse response) {
        MemberDuplicationCheckDto dto = new MemberDuplicationCheckDto();
        dto.setEmail(email);

        Member member = memberService.findByEmail(email);
        if (member == null) {
            dto.setDuplicated(false);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            dto.setDuplicated(true);
            dto.setMessage("duplicated email.");
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        }

        return dto;
    }
}
