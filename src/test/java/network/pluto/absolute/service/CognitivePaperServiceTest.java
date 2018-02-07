package network.pluto.absolute.service;

import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.util.Query;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// for cognitive api test
@Ignore
@TestPropertySource("classpath:application-test.properties")
@RunWith(SpringRunner.class)
public class CognitivePaperServiceTest {

    private CognitivePaperService cognitivePaperService;

    @Value("${pluto.ms.cognitive.uri}")
    private String cognitiveUri;

    @Value("${pluto.ms.cognitive.subscription.key}")
    private String cognitiveSubscriptionKey;

    @Before
    public void setUp() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
        cognitivePaperService = new CognitivePaperService(restTemplate);

        ReflectionTestUtils.setField(this.cognitivePaperService, "cognitiveSubscriptionKey", cognitiveSubscriptionKey);
        ReflectionTestUtils.setField(this.cognitivePaperService, "cognitiveUri", cognitiveUri);
    }

    @Test
    public void getRecommendedQuery() {
        Query query = Query.parse("side effect");

        String interpret = cognitivePaperService.getRecommendQuery(query);
        System.out.println(interpret);

        assertThat(interpret).isNotEmpty();
    }

    @Test
    public void enhance() {
        PaperDto dto1 = new PaperDto();
        dto1.setTitle("Nanoparticle therapeutics: an emerging treatment modality for cancer.");
        dto1.setYear(2008);

        PaperDto dto2 = new PaperDto();
        dto2.setTitle("Paper title that doesn't existsssssssssssssssss");
        dto2.setYear(1991);

        List<PaperDto> dtos = Arrays.asList(dto1, dto2);

        List<PaperDto> enhancedDtos = cognitivePaperService.enhance(dtos);

        assertThat(enhancedDtos).hasSize(2);

        assertThat(enhancedDtos.get(0).getVenue()).isNotEmpty();
        assertThat(enhancedDtos.get(0).getAuthorCount()).isGreaterThan(0);
        assertThat(enhancedDtos.get(0).getAuthors()).isNotEmpty();
        assertThat(enhancedDtos.get(0).getFosCount()).isGreaterThan(0);
        assertThat(enhancedDtos.get(0).getFosList()).isNotEmpty();
        assertThat(enhancedDtos.get(0).getCitedCount()).isGreaterThan(0);
        assertThat(enhancedDtos.get(0).getReferenceCount()).isGreaterThan(0);

        assertThat(enhancedDtos.get(1).getVenue()).isNull();
        assertThat(enhancedDtos.get(1).getAuthorCount()).isEqualTo(0);
        assertThat(enhancedDtos.get(1).getAuthors()).isEmpty();
        assertThat(enhancedDtos.get(1).getFosCount()).isEqualTo(0);
        assertThat(enhancedDtos.get(1).getFosList()).isEmpty();
        assertThat(enhancedDtos.get(1).getCitedCount()).isEqualTo(0);
        assertThat(enhancedDtos.get(1).getReferenceCount()).isEqualTo(0);
    }

}