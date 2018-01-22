package network.pluto.absolute.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import network.pluto.absolute.WithMockJwtUser;
import network.pluto.absolute.dto.CommentDto;
import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.facade.PaperFacade;
import network.pluto.absolute.service.CommentService;
import network.pluto.absolute.service.MemberService;
import network.pluto.absolute.service.PaperService;
import network.pluto.absolute.util.Query;
import network.pluto.bibliotheca.enums.AuthorityName;
import network.pluto.bibliotheca.models.Comment;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.models.Paper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

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
public class PaperControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private PaperFacade paperFacade;

    @MockBean
    private PaperService paperService;

    @MockBean
    private CommentService commentService;

    @MockBean
    private MemberService memberService;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void search_unauthenticated_user_can_access() throws Exception {
        String query = "text=test";

        PaperDto dto = new PaperDto();
        dto.setId(1);
        PageImpl<PaperDto> page = new PageImpl<>(Collections.singletonList(dto));

        when(paperFacade.search(any(Query.class), any(Pageable.class))).thenReturn(page);

        mvc
                .perform(get("/papers")
                        .param("query", query))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", List.class).isArray())
                .andExpect(jsonPath("$.content[0].id", Long.class).value(1));

        verify(paperFacade, only()).search(any(Query.class), any(Pageable.class));
    }

    @Test
    public void search_with_null_query_string() throws Exception {
        mvc
                .perform(get("/papers"))

                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Required String parameter 'query' is not present"));

        verifyZeroInteractions(paperFacade);
    }

    @Test
    public void search_with_empty_query_string() throws Exception {
        mvc
                .perform(get("/papers")
                        .param("query", ""))

                .andExpect(status().isBadRequest());

        // TODO should add controller error handler
//                .andExpect(status().reason("Invalid query: query not exists"));

        verifyZeroInteractions(paperFacade);
    }

    @Test
    public void createComment_unauthenticated_user_cannot_access() throws Exception {
        CommentDto dto = new CommentDto();
        dto.setComment("test comment");

        mvc
                .perform(post("/papers/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))

                .andExpect(status().isUnauthorized());

        verifyZeroInteractions(paperService);
        verifyZeroInteractions(memberService);
        verifyZeroInteractions(commentService);
    }

    @WithMockJwtUser(roles = AuthorityName.ROLE_UNVERIFIED)
    @Test
    public void createComment_unverified_user_cannot_access() throws Exception {
        CommentDto dto = new CommentDto();
        dto.setComment("test comment");

        mvc
                .perform(post("/papers/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))

                .andExpect(status().isUnauthorized());

        verifyZeroInteractions(paperService);
        verifyZeroInteractions(memberService);
        verifyZeroInteractions(commentService);
    }

    @WithMockJwtUser
    @Test
    public void createComment_only_verified_user_can_access() throws Exception {
        Paper paper = new Paper();
        paper.setId(1);

        Member member = new Member();
        member.setId(1);

        Comment created = new Comment();
        created.setId(1);
        created.setComment("test comment");
        created.setPaper(paper);
        created.setCreatedBy(member);

        when(paperService.find(1)).thenReturn(paper);
        when(memberService.getMember(1)).thenReturn(member);
        when(commentService.saveComment(eq(paper), any(Comment.class))).thenReturn(created);

        CommentDto dto = new CommentDto();
        dto.setComment("test comment");

        mvc
                .perform(post("/papers/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment", String.class).value("test comment"))
                .andExpect(jsonPath("$.paperId", Long.class).value(1))
                .andExpect(jsonPath("$.createdBy.id", Long.class).value(1));

        verify(paperService, only()).find(1);
        verify(memberService, only()).getMember(1);
        verify(commentService, only()).saveComment(eq(paper), any(Comment.class));
    }

    @WithMockJwtUser
    @Test
    public void deleteComment_only_verified_user_can_access() throws Exception {
        Member member = new Member();
        member.setId(1);
        Comment comment = new Comment();
        comment.setCreatedBy(member);

        when(commentService.find(1)).thenReturn(comment);

        mvc
                .perform(delete("/papers/1/comments/1"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", Boolean.class).value(true));

        verify(commentService).find(1);
        verify(commentService).deleteComment(comment);
        verifyNoMoreInteractions(commentService);
    }
}