package network.pluto.absolute.service.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import network.pluto.absolute.model.oauth.OauthGoogle;
import network.pluto.absolute.repository.oauth.OauthGoogleRepository;
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
public class OauthGoogleService {

    @Value("${pluto.oauth.google.client.id}")
    private String clientId;

    @Value("${pluto.oauth.google.client.secret}")
    private String clientSecret;

    @Value("${pluto.oauth.google.redirect.uri}")
    private String redirectUri;

    @Value("${pluto.oauth.google.endpoint.authorize}")
    private String authorizeEndpoint;

    @Value("${pluto.oauth.google.endpoint.token}")
    private String tokenEndpoint;

    @Value("${pluto.oauth.google.endpoint.api}")
    private String apiEndpoint;

    private final OauthGoogleRepository googleRepository;
    private final RestTemplate restTemplate;

    public OauthGoogle find(String googleId) {
        return googleRepository.findByGoogleId(googleId);
    }

    public URI getAuthorizeUri(String redirectUri) {
        return UriComponentsBuilder
                .fromHttpUrl(authorizeEndpoint)
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("scope", "https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email")
                .queryParam("redirect_uri", StringUtils.hasText(redirectUri) ? redirectUri : this.redirectUri)
                .build()
                .toUri();
    }


    @Transactional
    public OauthGoogle exchange(String code, String redirectUri) {
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
        AccessTokenResponse accessTokenResponse = restTemplate.postForEntity(tokenEndpoint, entity, AccessTokenResponse.class).getBody();

        UserDataResponse userData = getUserData(accessTokenResponse.accessToken);

        OauthGoogle one = find(userData.googleId);
        if (one != null) {
            one.setAccessToken(accessTokenResponse.accessToken);
        } else {
            OauthGoogle google = new OauthGoogle();
            google.setGoogleId(userData.googleId);
            google.setAccessToken(accessTokenResponse.accessToken);
            one = googleRepository.save(google);
        }

        one.setUserData(userData.toMap());
        return one;
    }

    private UserDataResponse getUserData(String accessToken) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(apiEndpoint)
                .queryParam("access_token", accessToken)
                .build()
                .toUri();

        return restTemplate.getForObject(uri, UserDataResponse.class);
    }

    private static class AccessTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
    }

    private static class UserDataResponse {

        @JsonProperty("id")
        private String googleId;

        @JsonProperty
        private String name;

        @JsonProperty
        private String email;

        Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("googleId", this.googleId);
            map.put("email", this.email);
            map.put("name", this.name);
            return map;
        }
    }
}
