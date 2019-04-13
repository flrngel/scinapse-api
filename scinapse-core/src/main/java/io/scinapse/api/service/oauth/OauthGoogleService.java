package io.scinapse.api.service.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.dto.v2.OAuthConnection;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.domain.data.scinapse.model.Member;
import io.scinapse.domain.data.scinapse.model.oauth.OauthGoogle;
import io.scinapse.domain.data.scinapse.repository.oauth.OauthGoogleRepository;
import io.scinapse.domain.enums.OauthVendor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    private final String authorizeEndpoint = "https://accounts.google.com/o/oauth2/v2/auth";
    private final String tokenEndpoint = "https://www.googleapis.com/oauth2/v4/token";
    private final String apiEndpoint = "https://www.googleapis.com/userinfo/v2/me";
    private final String tokenInfoEndpoint = "https://oauth2.googleapis.com/tokeninfo";

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

    @Transactional
    public void connect(String token, Member member) {
        TokenInfoResponse response = getTokenInfoResponse(token);
        String googleId = response.getSub();

        OauthGoogle google = find(googleId);
        if (google == null) {
            google = new OauthGoogle();
        }

        if (google.isConnected()) {
            throw new BadRequestException("Invalid OAuth Connection: already connected");
        }

        google.setGoogleId(googleId);
        google.setAccessToken(token);
        google.setMember(member);
        google.setConnected(true);
        googleRepository.save(google);
    }

    public OauthGoogle findByToken(String token) {
        TokenInfoResponse body = getTokenInfoResponse(token);
        String sub = body.getSub();
        return Optional.ofNullable(find(sub))
                .orElseThrow(() -> new BadRequestException("Authentication failed. OAuth information not found."));
    }

    public OAuthConnection getConnection(String token) {
        TokenInfoResponse data = getTokenInfoResponse(token);

        OAuthConnection connection = new OAuthConnection();
        connection.setVendor(OauthVendor.GOOGLE);
        connection.setOAuthId(data.getSub());

        connection.setEmail(data.getEmail());
        connection.setFirstName(data.getGivenName());
        connection.setLastName(data.getFamilyName());

        String googleId = data.getSub();
        boolean connected = Optional.ofNullable(find(googleId))
                .map(OauthGoogle::isConnected)
                .orElse(false);
        connection.setConnected(connected);

        return connection;
    }

    private TokenInfoResponse getTokenInfoResponse(String token) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(tokenInfoEndpoint)
                .queryParam("id_token", token)
                .build()
                .toUri();

        try {
            ResponseEntity<TokenInfoResponse> entity = restTemplate.getForEntity(uri, TokenInfoResponse.class);
            if (entity.getStatusCode() != HttpStatus.OK) {
                throw new BadRequestException("Authentication failed. Invalid token provided.");
            }
            return entity.getBody();
        } catch (RestClientException e) {
            throw new BadRequestException("Authentication failed. Invalid token provided.");
        }
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

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @Getter
    @Setter
    private static class TokenInfoResponse {
        private String sub;
        private String name;
        private String givenName;
        private String familyName;
        private String email;
        private boolean emailVerified;
    }

}
