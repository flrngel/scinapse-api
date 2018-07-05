package network.pluto.absolute.controller;

import network.pluto.absolute.WithMockJwtUser;
import network.pluto.absolute.enums.AuthorityName;
import network.pluto.absolute.models.Authority;
import network.pluto.absolute.models.Member;
import network.pluto.absolute.service.MemberService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.Cookie;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
@RunWith(SpringRunner.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MemberService memberService;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Test
    public void check_login_with_logged_out_user() throws Exception {
        mvc
                .perform(get("/auth/login").cookie(new Cookie("pluto_jwt", "test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loggedIn", Boolean.class).value(false))
                .andExpect(cookie().maxAge("pluto_jwt", 0));
    }

    @WithMockJwtUser
    @Test
    public void check_login_with_invalid_member() throws Exception {

        // member not exists
        when(memberService.findMember(1)).thenReturn(null);

        mvc
                .perform(get("/auth/login").cookie(new Cookie("pluto_jwt", "test")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loggedIn", Boolean.class).value(false))
                .andExpect(cookie().maxAge("pluto_jwt", 0));

        verify(memberService, only()).findMember(1);
    }

    @WithMockJwtUser
    @Test
    public void check_login_with_valid_member() throws Exception {
        Member member = new Member();
        member.setId(1);
        member.setEmail("alice@pluto.network");

        when(memberService.findMember(1)).thenReturn(member);

        mvc
                .perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loggedIn", Boolean.class).value(true))
                .andExpect(jsonPath("$.oauthLoggedIn", Boolean.class).value(false))
                .andExpect(jsonPath("$.member.id", Long.class).value(1))
                .andExpect(jsonPath("$.member.email", String.class).value("alice@pluto.network"));

        verify(memberService, only()).findMember(1);
    }

    @Test
    public void login_request_with_invalid_media_type() throws Exception {
        mvc
                .perform(post("/auth/login").content("{\"email\":\"alice@pluto.network\"}"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    public void login_request_with_invalid_json() throws Exception {
        mvc
                .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"alice@pluto.network\""))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void login_request_with_invalid_email() throws Exception {
        mvc
                .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void login_request_with_invalid_password() throws Exception {
        mvc
                .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void login_request_with_invalid_member() throws Exception {

        // member not exists
        when(memberService.getByEmail(eq("alice@pluto.network"), anyBoolean())).thenReturn(null);

        mvc
                .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"alice@pluto.network\",\"password\":\"password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason(containsString("Member not found:")));

        verify(memberService, only()).getByEmail("alice@pluto.network", true);
    }

    @Test
    public void login_request_with_incorrect_password() throws Exception {
        String email = "alice@pluto.network";
        String password = "password";

        Member member = new Member();
        member.setEmail(email);
        member.setPassword(encoder.encode(password));

        when(memberService.getByEmail(eq(email), anyBoolean())).thenReturn(member);

        mvc
                .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"alice@pluto.network\",\"password\":\"pazzward\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("Authentication Failed. Username or Password not valid."));

        verify(memberService, only()).getByEmail(email, true);
    }

    @Test
    public void login_request_with_oauth_user() throws Exception {
        String email = "alice@pluto.network";

        Member member = new Member();
        member.setEmail(email);

        // password not exists if oauth user
        when(memberService.getByEmail(eq(email), anyBoolean())).thenReturn(member);

        mvc
                .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"alice@pluto.network\",\"password\":\"password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("Authentication Failed. Oauth user did not register password."));

        verify(memberService, only()).getByEmail(email, true);
    }

    @Test
    public void login_request_with_no_authorities() throws Exception {
        String email = "alice@pluto.network";
        String password = "password";

        Member member = new Member();
        member.setEmail(email);
        member.setPassword(encoder.encode(password));

        when(memberService.getByEmail(eq(email), anyBoolean())).thenReturn(member);

        mvc
                .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"alice@pluto.network\",\"password\":\"password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("Member has no roles assigned"));

        verify(memberService, only()).getByEmail(email, true);
    }

    @Test
    public void login_request_with_correct_user() throws Exception {
        String email = "alice@pluto.network";
        String password = "password";

        Member member = new Member();
        member.setEmail(email);
        member.setPassword(encoder.encode(password));

        Authority authority = new Authority();
        authority.setName(AuthorityName.ROLE_USER);
        member.setAuthorities(Collections.singletonList(authority));

        when(memberService.getByEmail(eq(email), anyBoolean())).thenReturn(member);

        mvc
                .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"alice@pluto.network\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("pluto_jwt"))
                .andExpect(cookie().maxAge("pluto_jwt", greaterThan(1)));

        verify(memberService, only()).getByEmail(email, true);
    }

    @Test
    public void logout_request() throws Exception {
        mvc
                .perform(post("/auth/logout"))
                .andExpect(status().isOk());
    }

}