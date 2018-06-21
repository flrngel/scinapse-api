package network.pluto.absolute.controller;

import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.facade.PaperFacade;
import network.pluto.absolute.util.Query;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
@RunWith(SpringRunner.class)
public class PaperControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PaperFacade paperFacade;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void search_unauthenticated_user_can_access() throws Exception {
        String query = "text=test";

        Paper paper = new Paper();
        paper.setId(1);
        paper.setYear(2018);
        PaperDto dto = PaperDto.of(paper);
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

}