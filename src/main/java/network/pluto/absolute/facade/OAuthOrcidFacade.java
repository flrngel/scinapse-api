package network.pluto.absolute.facade;

import network.pluto.absolute.dto.OrcidDto;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.absolute.service.OrcidService;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.models.Orcid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
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

    @Value("${pluto.oauth.orcid.endpoint.api}")
    private String apiEndpoint;

    private final RestTemplate restTemplate;
    private final OrcidService orcidService;

    @Autowired
    public OAuthOrcidFacade(RestTemplate restTemplate, OrcidService orcidService) {
        this.restTemplate = restTemplate;
        this.orcidService = orcidService;
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

    public Orcid exchange(String authCode) {
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
        ResponseEntity<OrcidDto> response = restTemplate.postForEntity(tokenEndpoint, entity, OrcidDto.class);
        OrcidDto token = response.getBody();

        Orcid exist = orcidService.findByOrcid(token.getOrcid());
        if (exist != null) {
            return orcidService.update(exist, token.toEntity());
        }

        return orcidService.create(token.toEntity());
    }

    public Orcid verifyOrcid(OrcidDto dto) {
        Orcid orcid = orcidService.findByOrcid(dto.getOrcid());
        if (orcid == null) {
            return orcidService.create(dto.toEntity());
        }

        if (!StringUtils.hasText(dto.getAccessToken()) || !StringUtils.hasText(orcid.getAccessToken())) {
            throw new BadRequestException("Invalid ORCID token : ORCID access token not available");
        }

        if (!dto.getAccessToken().equals(orcid.getAccessToken())) {
            throw new BadRequestException("Invalid ORCID token : ORCID access token not matched");
        }

        return orcid;
    }

    public boolean isValidOrcid(OrcidDto dto) {
        Orcid orcid = orcidService.findByOrcid(dto.getOrcid());
        if (orcid == null) {
            return false;
        }

        if (!StringUtils.hasText(dto.getAccessToken()) || !StringUtils.hasText(orcid.getAccessToken())) {
            return false;
        }

        if (!dto.getAccessToken().equals(orcid.getAccessToken())) {
            return false;
        }

        return true;
    }

    public Orcid getOrcid(String orcid) {
        return orcidService.findByOrcid(orcid);
    }

    @Transactional
    public Member getMember(String orcid) {
        Orcid one = orcidService.findByOrcid(orcid);
        if (one == null) {
            throw new BadCredentialsException("Invalid ORCID");
        }

        return one.getMember();
    }
}
