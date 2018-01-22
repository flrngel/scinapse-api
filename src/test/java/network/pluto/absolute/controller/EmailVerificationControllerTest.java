package network.pluto.absolute.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import network.pluto.absolute.WithMockJwtUser;
import network.pluto.absolute.security.TokenHelper;
import network.pluto.absolute.service.EmailVerificationService;
import network.pluto.absolute.service.MemberService;
import network.pluto.bibliotheca.enums.AuthorityName;
import network.pluto.bibliotheca.models.Authority;
import network.pluto.bibliotheca.models.Member;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
@RunWith(SpringRunner.class)
public class EmailVerificationControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private EmailVerificationService emailVerificationService;

    @MockBean
    private MemberService memberService;

    @MockBean
    private TokenHelper tokenHelper;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void verify_unauthenticated_user_can_access() throws Exception {
        mvc
                .perform(post("/email-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"token\"}"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", Boolean.class).value(true));

        verify(emailVerificationService, only()).verify("token");
        verifyZeroInteractions(memberService);
        verify(tokenHelper).getToken(any(HttpServletRequest.class));
        verifyNoMoreInteractions(tokenHelper);
    }

    @WithMockJwtUser
    @Test
    public void verify_authenticated_user_can_access() throws Exception {
        Member member = new Member();
        Authority authority = new Authority();
        authority.setName(AuthorityName.ROLE_USER);
        member.setAuthorities(Collections.singletonList(authority));

        when(memberService.getMember(1)).thenReturn(member);
        when(tokenHelper.generateToken(member, false)).thenReturn("jwt");

        mvc
                .perform(post("/email-verification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"token\"}"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", Boolean.class).value(true));

        verify(emailVerificationService, only()).verify("token");
        verify(memberService, only()).getMember(1);
        verify(tokenHelper).generateToken(member, false);
        verify(tokenHelper).addCookie(any(HttpServletResponse.class), eq("jwt"));
    }

    @Test
    public void resend() throws Exception {
        Member member = new Member();
        member.setEmailVerified(false);

        when(memberService.findByEmail("alice@pluto.network")).thenReturn(member);

        mvc
                .perform(post("/email-verification/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"alice@pluto.network\"}"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", Boolean.class).value(true));

        verify(memberService, only()).findByEmail("alice@pluto.network");
        verify(emailVerificationService, only()).sendVerification(member);
    }

    @Test
    public void resend_with_verified_member() throws Exception {
        Member member = new Member();
        member.setEmailVerified(true);

        when(memberService.findByEmail("alice@pluto.network")).thenReturn(member);

        mvc
                .perform(post("/email-verification/resend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"alice@pluto.network\"}"))

                .andExpect(status().isBadRequest());

        verify(memberService, only()).findByEmail("alice@pluto.network");
        verifyZeroInteractions(emailVerificationService);
    }

}