package io.scinapse.api.service.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.scinapse.domain.data.scinapse.model.oauth.OauthOrcid;
import io.scinapse.domain.data.scinapse.repository.oauth.OauthOrcidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class OauthOrcidService {

    @Value("${pluto.oauth.orcid.client.id}")
    private String clientId;

    @Value("${pluto.oauth.orcid.client.secret}")
    private String clientSecret;

    @Value("${pluto.oauth.orcid.redirect.uri}")
    private String redirectUri;

    @Value("${pluto.oauth.orcid.endpoint.token}")
    private String tokenEndpoint;

    @Value("${pluto.oauth.orcid.endpoint.authorize}")
    private String authorizeEndpoint;

    @Value("${pluto.oauth.orcid.endpoint.api}")
    private String apiEndpoint;

    private final OauthOrcidRepository oauthOrcidRepository;
    private final RestTemplate restTemplate;

    public OauthOrcid find(String orcid) {
        return oauthOrcidRepository.findByOrcid(orcid);
    }

    public URI getAuthorizeUri(String redirectUri) {
        return UriComponentsBuilder
                .fromHttpUrl(authorizeEndpoint)
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("scope", "/authenticate")
                .queryParam("redirect_uri", StringUtils.hasText(redirectUri) ? redirectUri : this.redirectUri)
                .build()
                .toUri();
    }

    @Transactional
    public OauthOrcid exchange(String code, String redirectUri) {
        LinkedMultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("client_id", clientId);
        request.add("client_secret", clientSecret);
        request.add("code", code);
        request.add("grant_type", "authorization_code");
        request.add("redirect_uri", StringUtils.hasText(redirectUri) ? redirectUri : this.redirectUri);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<LinkedMultiValueMap<String, String>> entity = new HttpEntity<>(request, httpHeaders);

        // TODO 400 error handling
        Response response = restTemplate.postForEntity(tokenEndpoint, entity, Response.class).getBody();

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", response.name);

        OauthOrcid one = find(response.orcid);
        if (one != null) {
            one.setAccessToken(response.accessToken);
        } else {
            OauthOrcid oauthOrcid = new OauthOrcid();
            oauthOrcid.setOrcid(response.orcid);
            oauthOrcid.setAccessToken(response.accessToken);
            one = oauthOrcidRepository.save(oauthOrcid);
        }

        one.setUserData(userData);
        return one;
    }

    private static class Response {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty
        private String orcid;

        @JsonProperty
        private String name;
    }

}
