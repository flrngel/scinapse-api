package network.pluto.absolute.service;

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

}