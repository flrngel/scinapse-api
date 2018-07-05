package io.scinapse.api.service.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.scinapse.api.model.oauth.OauthFacebook;
import io.scinapse.api.repository.oauth.OauthFacebookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class OauthFacebookService {

    @Value("${pluto.oauth.facebook.client.id}")
    private String clientId;

    @Value("${pluto.oauth.facebook.client.secret}")
    private String clientSecret;

    @Value("${pluto.oauth.facebook.redirect.uri}")
    private String redirectUri;

    @Value("${pluto.oauth.facebook.endpoint.authorize}")
    private String authorizeEndpoint;

    @Value("${pluto.oauth.facebook.endpoint.token}")
    private String tokenEndpoint;

    @Value("${pluto.oauth.facebook.endpoint.api}")
    private String apiEndpoint;

    private final OauthFacebookRepository facebookRepository;
    private final RestTemplate restTemplate;

    public OauthFacebook find(String facebookId) {
        return facebookRepository.findByFacebookId(facebookId);
    }

    public URI getAuthorizeUri(String redirectUri) {
        return UriComponentsBuilder
                .fromHttpUrl(authorizeEndpoint)
                .queryParam("client_id", clientId)
                .queryParam("scope", "email")
                .queryParam("redirect_uri", StringUtils.hasText(redirectUri) ? redirectUri : this.redirectUri)
                .build()
                .toUri();
    }

    @Transactional
    public OauthFacebook exchange(String code, String redirectUri) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(tokenEndpoint)
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("code", code)
                .queryParam("redirect_uri", StringUtils.hasText(redirectUri) ? redirectUri : this.redirectUri)
                .build()
                .toUri();

        String accessToken = restTemplate.getForObject(uri, AccessTokenResponse.class).accessToken;

        UserDataResponse userData = getUserData(accessToken);

        OauthFacebook one = find(userData.facebookId);
        if (one != null) {
            one.setAccessToken(accessToken);
        } else {
            OauthFacebook facebook = new OauthFacebook();
            facebook.setFacebookId(userData.facebookId);
            facebook.setAccessToken(accessToken);
            one = facebookRepository.save(facebook);
        }

        one.setUserData(userData.toMap());
        return one;
    }

    private UserDataResponse getUserData(String accessToken) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(apiEndpoint)
                .queryParam("fields", "id,name,email")
                .queryParam("access_token", accessToken)
                .build()
                .toUri();

        return restTemplate.getForObject(uri, UserDataResponse.class);
    }

    @Transactional
    public OauthFacebook update(OauthFacebook old, OauthFacebook updated) {
        old.setAccessToken(updated.getAccessToken());
        return old;
    }

    private static class AccessTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
    }

    private static class UserDataResponse {

        @JsonProperty("id")
        private String facebookId;

        @JsonProperty
        private String name;

        @JsonProperty
        private String email;

        Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("facebookId", facebookId);
            map.put("name", name);
            map.put("email", email);
            return map;
        }
    }

}
