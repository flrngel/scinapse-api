package network.pluto.absolute.security.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import network.pluto.absolute.dto.LoginDto;
import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.security.TokenHelper;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RestAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenHelper tokenHelper;
    private final ObjectMapper objectMapper;

    @Autowired
    public RestAuthenticationSuccessHandler(TokenHelper tokenHelper, ObjectMapper objectMapper) {
        this.tokenHelper = tokenHelper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Member member = (Member) authentication.getPrincipal();

        String jws = tokenHelper.generateToken(member);
        tokenHelper.addCookie(response, jws);

        MemberDto memberDto = new MemberDto(member);
        LoginDto loginDto = new LoginDto(true, jws, memberDto);

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getWriter(), loginDto);

        clearAuthenticationAttributes(request);
    }
}
