package network.pluto.absolute.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import network.pluto.bibliotheca.models.oauth.OauthFacebook;
import network.pluto.bibliotheca.repositories.oauth.OauthFacebookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Transactional(readOnly = true)
@Service
public class OauthFacebookService {

    @Value("${pluto.oauth.facebook.client.id}")
    private String clientId;

    @Value("${pluto.oauth.facebook.client.secret}")
    private String clientSecret;

    @Value("${pluto.oauth.facebook.redirect.uri}")
    private String redirectUrI;

    @Value("${pluto.oauth.facebook.endpoint.authorize}")
    private String authorizeEndpoint;

    @Value("${pluto.oauth.facebook.endpoint.token}")
    private String tokenEndpoint;

    @Value("${pluto.oauth.facebook.endpoint.api}")
    private String apiEndpoint;

    private final OauthFacebookRepository facebookRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public OauthFacebookService(OauthFacebookRepository facebookRepository,
                                RestTemplate restTemplate) {
        this.facebookRepository = facebookRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public OauthFacebook create(OauthFacebook facebook) {
        return facebookRepository.save(facebook);
    }

    public OauthFacebook find(String facebookId) {
        return facebookRepository.findByFacebookId(facebookId);
    }

    public URI getAuthorizeUri() {
        return UriComponentsBuilder
                .fromHttpUrl(authorizeEndpoint)
                .queryParam("client_id", clientId)
                .queryParam("scope", "email")
                .queryParam("redirect_uri", redirectUrI)
                .build()
                .toUri();
    }

    @Transactional
    public OauthFacebook exchange(String code) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(tokenEndpoint)
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("code", code)
                .queryParam("redirect_uri", redirectUrI)
                .build()
                .toUri();

        String accessToken = restTemplate.getForObject(uri, AccessTokenResponse.class).accessToken;

        UserDataResponse userData = getUserData(accessToken);

        OauthFacebook one = find(userData.facebookId);
        if (one != null) {
            one.setAccessToken(accessToken);
        } else {
            OauthFacebook facebook = new OauthFacebook();
            facebook.setAccessToken(accessToken);
            facebook.setFacebookId(userData.facebookId);
            one = facebookRepository.save(facebook);
        }

        one.setUserData(userData.toMap());
        return one;
    }

    public UserDataResponse getUserData(String accessToken) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(apiEndpoint)
                .queryParam("fields", "id,name,email")
                .queryParam("access_token", accessToken)
                .queryParam("redirect_uri", redirectUrI)
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
