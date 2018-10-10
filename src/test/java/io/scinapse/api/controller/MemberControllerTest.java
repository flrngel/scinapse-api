package io.scinapse.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.scinapse.api.WithMockJwtUser;
import io.scinapse.api.dto.MemberDto;
import io.scinapse.api.facade.MemberFacade;
import io.scinapse.api.model.Member;
import io.scinapse.api.service.MemberService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
@RunWith(SpringRunner.class)
public class MemberControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private MemberFacade memberFacade;

    @MockBean
    private MemberService memberService;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void create_member_unauthenticated_user_can_access() throws Exception {
        String email = "alice@pluto.network";
        String firstName = "alice";
        String lastName = "bob";
        String affiliation = "Pluto";

        MemberDto requestDto = new MemberDto();
        requestDto.setEmail(email);
        requestDto.setFirstName(firstName);
        requestDto.setLastName(lastName);
        requestDto.setAffiliation(affiliation);

        long createdMemberId = 1;
        Member created = new Member();
        created.setId(createdMemberId);
        created.setEmail(email);
        created.setFirstName(firstName);
        created.setLastName(lastName);
        created.setAffiliation(affiliation);

        when(memberFacade.create(any(MockHttpServletResponse.class), any(MemberDto.class))).thenReturn(created);

        mvc
                .perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Long.class).value(createdMemberId))
                .andExpect(jsonPath("$.email", String.class).value(email))
                .andExpect(jsonPath("$.firstName", String.class).value(firstName))
                .andExpect(jsonPath("$.lastName", String.class).value(lastName))
                .andExpect(jsonPath("$.affiliation", String.class).value(affiliation));

        verify(memberFacade, only()).create(any(MockHttpServletResponse.class), any(MemberDto.class));
    }

    @Test
    public void create_oauth_member_unauthenticated_user_can_access() throws Exception {
        String email = "alice@pluto.network";
        String firstName = "alice";
        String lastName = "bob";
        String affiliation = "Pluto";

        MemberDto requestDto = new MemberDto();
        requestDto.setEmail(email);
        requestDto.setFirstName(firstName);
        requestDto.setLastName(lastName);
        requestDto.setAffiliation(affiliation);

        long createdMemberId = 1;
        Member created = new Member();
        created.setId(createdMemberId);

        when(memberFacade.createOauthMember(any(MockHttpServletResponse.class), any(MemberDto.class))).thenReturn(created);

        mvc
                .perform(post("/members/oauth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Long.class).value(createdMemberId));

        verify(memberFacade, only()).createOauthMember(any(MockHttpServletResponse.class), any(MemberDto.class));
    }

    @Test
    public void getMember_unauthenticated_user_can_access() throws Exception {
        long validMemberId = 1;
        String email = "alice@pluto.network";

        MemberDto response = new MemberDto();
        response.setId(validMemberId);
        response.setEmail(email);

        when(memberFacade.getDetail(validMemberId)).thenReturn(response);

        mvc
                .perform(get("/members/" + validMemberId))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Long.class).value(validMemberId))
                .andExpect(jsonPath("$.email", String.class).value(email));

        verify(memberFacade, only()).getDetail(1);
    }

    @Test
    public void updateMember_unauthenticated_user_cannot_access() throws Exception {
        mvc
                .perform(put("/members/me"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockJwtUser
    @Test
    public void updateMember_authenticated_user_can_access() throws Exception {
        long memberId = 1;
        String affiliation = "Pluto";

        Member old = new Member();
        old.setId(memberId);
        old.setFirstName("alice");
        old.setLastName("bob");
        old.setAffiliation(affiliation);

        String updateName = "charles";
        MemberDto requestDto = new MemberDto();
        requestDto.setFirstName(updateName);
        requestDto.setAffiliation(affiliation);

        when(memberService.findMember(memberId)).thenReturn(old);
        when(memberService.updateMember(eq(old), any())).thenAnswer(invocation -> {
            Member updated = invocation.getArgumentAt(1, Member.class);

            old.setFirstName(updated.getFirstName());
            return old;
        });

        mvc
                .perform(put("/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", String.class).value("charles"))
                .andExpect(jsonPath("$.lastName", String.class).value("bob"))
                .andExpect(jsonPath("$.affiliation", String.class).value(affiliation));

        verify(memberService).findMember(memberId);
        verify(memberService).updateMember(eq(old), any());
        verifyNoMoreInteractions(memberService);
    }

    @WithMockJwtUser(memberId = 0)
    @Test
    public void updateMember_with_invalid_member_id() throws Exception {
        long invalidMemberId = 0;

        MemberDto requestDto = new MemberDto();
        requestDto.setFirstName("alice");
        requestDto.setLastName("bob");
        requestDto.setAffiliation("Pluto");

        when(memberService.findMember(invalidMemberId)).thenReturn(null);

        mvc
                .perform(put("/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))

                .andExpect(status().isNotFound());

        // FIXME should be checked
//                .andExpect(status().reason("Member not found"));

        verify(memberService).findMember(invalidMemberId);
        verify(memberService, never()).updateMember(any(), any());
        verifyNoMoreInteractions(memberService);
    }

    @Test
    public void duplicate_check_unauthenticated_user_can_access() throws Exception {
        String duplicatedEmail = "alice@pluto.network";
        String newEmail = "bob@pluto.network";

        when(memberService.findByEmail(duplicatedEmail)).thenReturn(new Member());
        when(memberService.findByEmail(newEmail)).thenReturn(null);

        mvc
                .perform(get("/members/checkDuplication")
                        .param("email", duplicatedEmail))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", String.class).value(duplicatedEmail))
                .andExpect(jsonPath("$.duplicated", Boolean.class).value(true))
                .andExpect(jsonPath("$.message", String.class).value("duplicated email."));

        verify(memberService).findByEmail(duplicatedEmail);

        mvc
                .perform(get("/members/checkDuplication")
                        .param("email", newEmail))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", String.class).value(newEmail))
                .andExpect(jsonPath("$.duplicated", Boolean.class).value(false));

        verify(memberService).findByEmail(newEmail);

        verifyNoMoreInteractions(memberService);
    }

}