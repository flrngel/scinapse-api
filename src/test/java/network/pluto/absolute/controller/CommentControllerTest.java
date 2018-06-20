package network.pluto.absolute.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import network.pluto.absolute.WithMockJwtUser;
import network.pluto.absolute.dto.CommentDto;
import network.pluto.absolute.service.CommentService;
import network.pluto.absolute.service.MemberService;
import network.pluto.absolute.service.mag.PaperService;
import network.pluto.bibliotheca.enums.AuthorityName;
import network.pluto.bibliotheca.models.Comment;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.models.mag.Paper;
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
public class CommentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private PaperService paperService;

    @MockBean
    private MemberService memberService;

    @MockBean
    private CommentService commentService;

    @Before
    public void setUp() throws Exception {
    }

    @WithMockJwtUser
    @Test
    public void createComment_only_authenticated_user_can_access() throws Exception {
        long paperId = 1;
        long memberId = 1;

        Paper paper = new Paper();
        paper.setId(paperId);

        Member member = new Member();
        member.setId(memberId);

        String commentMessage = "test comment";
        Comment comment = new Comment();
        comment.setId(1);
        comment.setComment(commentMessage);
        comment.setPaperId(paperId);
        comment.setCreatedBy(member);

        when(paperService.find(paperId, false)).thenReturn(paper);
        when(memberService.getMember(memberId)).thenReturn(member);
        when(commentService.saveComment(eq(paperId), any(Comment.class))).thenReturn(comment);

        CommentDto commentDto = new CommentDto();
        commentDto.setComment(commentMessage);
        commentDto.setPaperId(paperId);

        mvc
                .perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentDto)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment", String.class).value(commentMessage))
                .andExpect(jsonPath("$.paperId", Long.class).value(paperId))
                .andExpect(jsonPath("$.createdBy.id", Long.class).value(memberId));

        verify(paperService, only()).find(paperId, false);
        verify(memberService, only()).getMember(memberId);
        verify(commentService, only()).saveComment(eq(paperId), any(Comment.class));
    }

    @Test
    public void createComment_unauthenticated_user_cannot_access() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setComment("test comment");
        commentDto.setPaperId(1L);

        mvc
                .perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentDto)))

                .andExpect(status().isUnauthorized());

        verifyZeroInteractions(paperService);
        verifyZeroInteractions(memberService);
        verifyZeroInteractions(commentService);
    }

    @WithMockJwtUser(roles = AuthorityName.ROLE_UNVERIFIED)
    @Test
    public void createComment_unverified_user_cannot_access() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setComment("test comment");
        commentDto.setPaperId(1L);

        mvc
                .perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentDto)))

                .andExpect(status().isUnauthorized());

        verifyZeroInteractions(paperService);
        verifyZeroInteractions(memberService);
        verifyZeroInteractions(commentService);
    }

    @Test
    public void findComments() throws Exception {
        long paperId = 1;
        long memberId = 1;

        Paper paper = new Paper();
        paper.setId(paperId);

        Member member = new Member();
        member.setId(memberId);

        String commentMessage = "test comment";
        Comment comment = new Comment();
        comment.setId(1);
        comment.setComment(commentMessage);
        comment.setPaperId(paperId);
        comment.setCreatedBy(member);

        List<Comment> comments = Collections.singletonList(comment);
        PageImpl<Comment> page = new PageImpl<>(comments);


        when(paperService.find(paperId, false)).thenReturn(paper);
        when(commentService.findByPaperId(eq(paperId), any(Pageable.class))).thenReturn(page);

        mvc
                .perform(get("/comments")
                        .param("paperId", "1"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].comment", String.class).value(commentMessage));

        verify(paperService, only()).find(paperId, false);
        verify(commentService, only()).findByPaperId(eq(paperId), any(Pageable.class));
    }

    @WithMockJwtUser
    @Test
    public void deleteComment() throws Exception {
        long memberId = 1;
        Member member = new Member();
        member.setId(memberId);

        Comment comment = new Comment();
        comment.setCreatedBy(member);

        long commentId = 1;
        when(commentService.find(commentId)).thenReturn(comment);

        mvc
                .perform(delete("/comments/" + commentId))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", Boolean.class).value(true));

        verify(commentService).find(commentId);
        verify(commentService).deleteComment(comment);
        verifyNoMoreInteractions(commentService);
    }

}