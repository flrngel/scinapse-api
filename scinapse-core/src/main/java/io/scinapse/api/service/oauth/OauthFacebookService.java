package io.scinapse.api.service.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.dto.v2.OAuthConnection;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.domain.data.scinapse.model.Member;
import io.scinapse.domain.data.scinapse.model.oauth.OauthFacebook;
import io.scinapse.domain.data.scinapse.repository.oauth.OauthFacebookRepository;
import io.scinapse.domain.enums.OauthVendor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    private final String authorizeEndpoint = "https://www.facebook.com/v2.11/dialog/oauth";
    private final String tokenEndpoint = "https://graph.facebook.com/v2.11/oauth/access_token";
    private final String apiEndpoint = "https://graph.facebook.com/v2.11/me";

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

    @Transactional
    public void connect(String token, Member member) {
        UserDataResponse data = getUserData(token);
        String facebookId = data.getFacebookId();

        OauthFacebook facebook = find(facebookId);
        if (facebook == null) {
            facebook = new OauthFacebook();
        }

        if (facebook.isConnected()) {
            throw new BadRequestException("Invalid OAuth Connection: already connected");
        }

        facebook.setFacebookId(facebookId);
        facebook.setAccessToken(token);
        facebook.setMember(member);
        facebook.setConnected(true);
        facebookRepository.save(facebook);
    }

    public OauthFacebook findByToken(String token) {
        UserDataResponse data = getUserData(token);
        String facebookId = data.getFacebookId();
        return Optional.ofNullable(find(facebookId))
                .orElseThrow(() -> new BadRequestException("Authentication failed. OAuth information not found."));
    }

    public OAuthConnection getConnection(String token) {
        UserDataResponse data = getUserData(token);

        OAuthConnection connection = new OAuthConnection();
        connection.setVendor(OauthVendor.FACEBOOK);
        connection.setOAuthId(data.getFacebookId());

        connection.setEmail(data.getEmail());
        connection.setFirstName(data.getFirstName());
        connection.setLastName(data.getLastName());

        String facebookId = data.getFacebookId();
        boolean connected = Optional.ofNullable(find(facebookId))
                .map(OauthFacebook::isConnected)
                .orElse(false);
        connection.setConnected(connected);

        return connection;
    }

    private UserDataResponse getUserData(String accessToken) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(apiEndpoint)
                .queryParam("fields", "id,email,name,first_name,last_name")
                .queryParam("access_token", accessToken)
                .build()
                .toUri();

        try {
            ResponseEntity<UserDataResponse> entity = restTemplate.getForEntity(uri, UserDataResponse.class);
            if (entity.getStatusCode() != HttpStatus.OK) {
                throw new BadRequestException("Authentication failed. Invalid token provided.");
            }
            return entity.getBody();
        } catch (RestClientException e) {
            throw new BadRequestException("Authentication failed. Invalid token provided.");
        }
    }

    private static class AccessTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @Getter
    @Setter
    private static class UserDataResponse {

        @JsonProperty("id")
        private String facebookId;

        private String name;

        private String firstName;

        private String lastName;

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
