package network.pluto.absolute.facade;

import network.pluto.absolute.dto.OrcidDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;

@Component
public class OAuthOrcidFacade {

    @Value("${pluto.oauth.orcid.client.id}")
    private String clientId;

    @Value("${pluto.oauth.orcid.client.secret}")
    private String clientSecret;

    @Value("${pluto.oauth.orcid.redirect.uri}")
    private String redirectUrI;

    @Value("${pluto.oauth.orcid.endpoint.token}")
    private String tokenEndpoint;

    @Value("${pluto.oauth.orcid.endpoint.authorize}")
    private String authorizeEndpoint;

    private final RestTemplate restTemplate;

    @Autowired
    public OAuthOrcidFacade(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public URI getAuthorizeUri() {
        return UriComponentsBuilder
                .fromHttpUrl(authorizeEndpoint)
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("scope", "/authenticate")
                .queryParam("redirect_uri", redirectUrI)
                .build()
                .toUri();
    }

    public OrcidDto exchange(String authCode) {
        LinkedMultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("client_id", clientId);
        request.add("client_secret", clientSecret);
        request.add("redirect_uri", redirectUrI);
        request.add("grant_type", "authorization_code");
        request.add("code", authCode);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<LinkedMultiValueMap<String, String>> entity = new HttpEntity<>(request, httpHeaders);
        return restTemplate.postForEntity(tokenEndpoint, entity, OrcidDto.class).getBody();
    }
}
