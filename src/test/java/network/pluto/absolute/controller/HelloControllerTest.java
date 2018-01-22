package network.pluto.absolute.controller;

import network.pluto.absolute.WithMockJwtUser;
import network.pluto.bibliotheca.enums.AuthorityName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
@RunWith(SpringRunner.class)
public class HelloControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void web_ignoring_path_should_be_accessible_without_auth() throws Exception {
        mvc
                .perform(get("/hello"))
                .andExpect(status().isOk());
    }

    @Test
    public void general_path_should_not_be_accessible_without_auth() throws Exception {
        mvc
                .perform(get("/world"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockJwtUser(roles = AuthorityName.ROLE_USER)
    @Test
    public void authenticated_user_can_access_to_general_path() throws Exception {
        mvc
                .perform(get("/world"))
                .andExpect(status().isOk());
    }

    @WithMockJwtUser(roles = AuthorityName.ROLE_UNVERIFIED)
    @Test
    public void unverified_user_cannot_access_to_role_base_url() throws Exception {
        mvc
                .perform(get("/user"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockJwtUser(roles = AuthorityName.ROLE_USER)
    @Test
    public void general_user_can_access_to_role_base_url() throws Exception {
        mvc
                .perform(get("/user"))
                .andExpect(status().isOk());
    }

    @WithMockJwtUser(roles = AuthorityName.ROLE_ADMIN)
    @Test
    public void admin_user_can_access_to_role_base_url() throws Exception {
        mvc
                .perform(get("/user"))
                .andExpect(status().isOk());
    }

    @WithMockJwtUser(roles = AuthorityName.ROLE_USER)
    @Test
    public void normal_user_cannot_access_to_admin_role_url() throws Exception {
        mvc
                .perform(get("/admin"))
                .andExpect(status().isUnauthorized());
    }

    @WithMockJwtUser(roles = AuthorityName.ROLE_ADMIN)
    @Test
    public void only_admin_user_can_access_to_admin_role_url() throws Exception {
        mvc
                .perform(get("/admin"))
                .andExpect(status().isOk());
    }

}
