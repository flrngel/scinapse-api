package network.pluto.absolute.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import network.pluto.absolute.dto.OAuthRequest;
import network.pluto.absolute.dto.oauth.OauthUserDto;
import network.pluto.absolute.enums.OauthVendor;
import network.pluto.absolute.facade.OauthFacade;
import network.pluto.absolute.security.TokenHelper;
import network.pluto.absolute.service.MemberService;
import network.pluto.bibliotheca.models.Member;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class, secure = false)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class OauthControllerTest {

    @Autowired
    private MockMvc mvc;

    private ObjectMapper mapper = new ObjectMapper();

    @MockBean
    private OauthFacade oauthFacade;

    @MockBean
    private TokenHelper tokenHelper;

    @MockBean
    private MemberService memberService;

    @MockBean
    private BCryptPasswordEncoder encoder;

    @Test
    public void authorize_uri() throws Exception {
        when(oauthFacade.getAuthorizeUri(OauthVendor.ORCID, null)).thenReturn(new URI("test"));

        mvc
                .perform(get("/auth/oauth/authorize-uri")
                        .param("vendor", "ORCID"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vendor", String.class).value("ORCID"))
                .andExpect(jsonPath("$.uri").value("test"));

        verify(oauthFacade, only()).getAuthorizeUri(OauthVendor.ORCID, null);
    }

    @Test
    public void authorize_uri_without_vendor_param() throws Exception {
        mvc
                .perform(get("/auth/oauth/authorize-uri"))

                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Required OauthVendor parameter 'vendor' is not present"));

        verifyZeroInteractions(oauthFacade);
    }

    @Test
    public void authorize_uri_with_redirect_param() throws Exception {
        String redirectUri = "redirect";
        when(oauthFacade.getAuthorizeUri(OauthVendor.ORCID, redirectUri)).thenReturn(new URI(redirectUri));

        mvc
                .perform(get("/auth/oauth/authorize-uri")
                        .param("vendor", "ORCID")
                        .param("redirectUri", redirectUri))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vendor", String.class).value("ORCID"))
                .andExpect(jsonPath("$.uri").value(redirectUri));

        verify(oauthFacade, only()).getAuthorizeUri(OauthVendor.ORCID, redirectUri);
    }

    @Test
    public void exchange() throws Exception {
        OauthUserDto userDto = new OauthUserDto();
        userDto.setVendor(OauthVendor.ORCID);
        userDto.setUuid("uuid");
        userDto.setOauthId("123");
        userDto.setConnected(false);

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", "alice");
        userDto.setUserData(userData);

        when(oauthFacade.exchange(OauthVendor.ORCID, "test", "redirect")).thenReturn(userDto);

        OAuthRequest request = new OAuthRequest();
        request.setVendor(OauthVendor.ORCID);
        request.setCode("test");
        request.setRedirectUri("redirect");

        mvc
                .perform(post("/auth/oauth/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vendor", String.class).value(OauthVendor.ORCID.name()))
                .andExpect(jsonPath("$.uuid", String.class).value("uuid"))
                .andExpect(jsonPath("$.oauthId", String.class).value("123"))
                .andExpect(jsonPath("$.connected", Boolean.class).value(false))
                .andExpect(jsonPath("$.userData.name", String.class).value("alice"));

        verify(oauthFacade, only()).exchange(OauthVendor.ORCID, "test", "redirect");
    }

    @Test
    public void exchange_without_vendor_param() throws Exception {
        OAuthRequest request = new OAuthRequest();
        request.setCode("test");
        request.setRedirectUri("redirect");

        mvc
                .perform(post("/auth/oauth/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest());

        verifyZeroInteractions(oauthFacade);
    }

    @Test
    public void exchange_with_invalid_vendor_param() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("vendor", "INVALID");
        request.put("code", "test");
        request.put("redirectUri", "redirect");

        mvc
                .perform(post("/auth/oauth/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))

                .andExpect(status().isBadRequest());

        verifyZeroInteractions(oauthFacade);
    }

    @Test
    public void oauth_login_request() throws Exception {
        String email = "alice@pluto.network";

        Member member = new Member();
        member.setEmail(email);

        OAuthRequest request = new OAuthRequest();
        request.setVendor(OauthVendor.ORCID);
        request.setCode("test");
        request.setRedirectUri("redirect");

        when(oauthFacade.findMember(request.getVendor(), request.getCode(), request.getRedirectUri())).thenReturn(member);
        when(tokenHelper.generateToken(member, true)).thenReturn("token");

        mvc
                .perform(post("/auth/oauth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loggedIn", Boolean.class).value(true))
                .andExpect(jsonPath("$.oauthLoggedIn", Boolean.class).value(true))
                .andExpect(jsonPath("$.token", String.class).value("token"))
                .andExpect(jsonPath("$.member.email", String.class).value(email));

        verify(oauthFacade, only()).findMember(request.getVendor(), request.getCode(), request.getRedirectUri());
        verify(tokenHelper).generateToken(member, true);
        verify(tokenHelper).addCookie(any(), eq("token"));
    }

    // FIXME need to be fixed with MockMvc
    // It seems that error throwing directly from Controller is not handled well through MockMvc
    // It works well(MockMvc handles error well) if context is loaded, for example, with @SpringBootTest
    @Test(expected = Exception.class)
    public void oauth_login_request_with_invalid_member() throws Exception {
        OAuthRequest request = new OAuthRequest();
        request.setVendor(OauthVendor.ORCID);
        request.setCode("test");
        request.setRedirectUri("redirect");

        when(oauthFacade.findMember(request.getVendor(), request.getCode(), request.getRedirectUri())).thenReturn(null);

        mvc
                .perform(post("/auth/oauth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)));

        fail();
    }
}
