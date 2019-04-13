package io.scinapse.api.service.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.api.dto.v2.OAuthConnection;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.domain.data.scinapse.model.Member;
import io.scinapse.domain.data.scinapse.model.oauth.OauthOrcid;
import io.scinapse.domain.data.scinapse.repository.oauth.OauthOrcidRepository;
import io.scinapse.domain.enums.OauthVendor;
import io.scinapse.domain.util.JsonUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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

import java.io.IOException;
import java.net.URI;
import java.util.*;

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

    private final String authorizeEndpoint = "https://orcid.org/oauth/authorize";
    private final String tokenEndpoint = "https://orcid.org/oauth/token";
    private final String apiEndpoint = "https://pub.orcid.org/v2.0";

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

    @Transactional
    public void connect(String token, Member member) {
        TokenInfoResponse data = getTokenInfoResponse(token);
        String orcid = data.getSub();

        OauthOrcid oauthOrcid = find(orcid);
        if (oauthOrcid == null) {
            oauthOrcid = new OauthOrcid();
        }

        if (oauthOrcid.isConnected()) {
            throw new BadRequestException("Invalid OAuth Connection: already connected");
        }

        oauthOrcid.setOrcid(orcid);
        oauthOrcid.setAccessToken(token);
        oauthOrcid.setMember(member);
        oauthOrcid.setConnected(true);
        oauthOrcidRepository.save(oauthOrcid);
    }

    public OauthOrcid findByToken(String token) {
        TokenInfoResponse data = getTokenInfoResponse(token);
        String orcid = data.getSub();
        return Optional.ofNullable(find(orcid))
                .orElseThrow(() -> new BadRequestException("Authentication failed. OAuth information not found."));
    }

    public OAuthConnection getConnection(String token) {
        TokenInfoResponse data = getTokenInfoResponse(token);

        OAuthConnection connection = new OAuthConnection();
        connection.setVendor(OauthVendor.ORCID);
        connection.setOAuthId(data.getSub());

        connection.setFirstName(data.getGivenName());
        connection.setLastName(data.getFamilyName());

        String orcid = data.getSub();
        boolean connected = Optional.ofNullable(find(orcid))
                .map(OauthOrcid::isConnected)
                .orElse(false);
        connection.setConnected(connected);

        return connection;
    }

    private TokenInfoResponse getTokenInfoResponse(String token) {
        String[] split = org.apache.commons.lang3.StringUtils.split(token, ".");
        if (split.length != 3) {
            throw new BadRequestException("Authentication failed. Invalid token provided.");
        }

        try {
            return JsonUtils.fromBytes(Base64.getDecoder().decode(split[1]), TokenInfoResponse.class);
        } catch (IOException e) {
            throw new BadRequestException("Authentication failed. Invalid token provided.");
        }
    }

    private static class Response {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty
        private String orcid;

        @JsonProperty
        private String name;
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @Getter
    @Setter
    private static class TokenInfoResponse {
        private String sub;
        private String givenName;
        private String familyName;
    }

}
