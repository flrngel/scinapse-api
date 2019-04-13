package io.scinapse.api.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.dto.oauth.OAuthRequest;
import io.scinapse.api.dto.oauth.OauthUserDto;
import io.scinapse.api.dto.v2.OAuthConnection;
import io.scinapse.api.dto.v2.OAuthToken;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.service.oauth.OauthFacebookService;
import io.scinapse.api.service.oauth.OauthGoogleService;
import io.scinapse.api.service.oauth.OauthOrcidService;
import io.scinapse.domain.data.scinapse.model.Member;
import io.scinapse.domain.data.scinapse.model.oauth.OauthFacebook;
import io.scinapse.domain.data.scinapse.model.oauth.OauthGoogle;
import io.scinapse.domain.data.scinapse.model.oauth.OauthOrcid;
import io.scinapse.domain.enums.OauthVendor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

@XRayEnabled
@Transactional(readOnly = true)
@Component
@RequiredArgsConstructor
public class OauthFacade {

    private final OauthOrcidService orcidService;
    private final OauthFacebookService facebookService;
    private final OauthGoogleService googleService;

    public URI getAuthorizeUri(OauthVendor vendor, String redirectUri) {
        if (vendor == null) {
            throw new BadRequestException("Invalid Oauth: vendor not exist");
        }

        switch (vendor) {
            case ORCID:
                return orcidService.getAuthorizeUri(redirectUri);

            case FACEBOOK:
                return facebookService.getAuthorizeUri(redirectUri);

            case GOOGLE:
                return googleService.getAuthorizeUri(redirectUri);

            default:
                return null;
        }
    }

    @Transactional
    public OauthUserDto exchange(OauthVendor vendor, String code, String redirectUri) {
        if (vendor == null) {
            throw new BadRequestException("Invalid Oauth: vendor not exist");
        }

        OauthUserDto dto = new OauthUserDto();

        switch (vendor) {
            case ORCID:
                OauthOrcid orcid = orcidService.exchange(code, redirectUri);

                dto.setVendor(OauthVendor.ORCID);
                dto.setOauthId(orcid.getOrcid());
                dto.setUuid(orcid.getUuid());
                dto.setUserData(orcid.getUserData());
                dto.setConnected(orcid.isConnected());

                break;

            case FACEBOOK:
                OauthFacebook facebook = facebookService.exchange(code, redirectUri);

                dto.setVendor(OauthVendor.FACEBOOK);
                dto.setOauthId(facebook.getFacebookId());
                dto.setUuid(facebook.getUuid());
                dto.setUserData(facebook.getUserData());
                dto.setConnected(facebook.isConnected());

                break;

            case GOOGLE:
                OauthGoogle google = googleService.exchange(code, redirectUri);

                dto.setVendor(OauthVendor.GOOGLE);
                dto.setOauthId(google.getGoogleId());
                dto.setUuid(google.getUuid());
                dto.setUserData(google.getUserData());
                dto.setConnected(google.isConnected());

                break;

            default:
        }

        return dto;
    }

    @Transactional
    public Member findMember(OAuthRequest request) {
        switch (request.getVendor()) {
            case ORCID:
                return orcidService.exchange(request.getCode(), request.getRedirectUri()).getMember();

            case FACEBOOK:
                return facebookService.exchange(request.getCode(), request.getRedirectUri()).getMember();

            case GOOGLE:
                return googleService.exchange(request.getCode(), request.getRedirectUri()).getMember();

            default:
                return null;
        }
    }

    public Member findMember2(OAuthToken token) {
        switch (token.getVendor()) {
            case ORCID:
                return orcidService.findByToken(token.getToken()).getMember();

            case FACEBOOK:
                return facebookService.findByToken(token.getToken()).getMember();

            case GOOGLE:
                return googleService.findByToken(token.getToken()).getMember();

            default:
                return null;
        }
    }

    @Transactional
    public void connect(OauthUserDto oauth, Member member) {
        if (oauth.getVendor() == null) {
            throw new BadRequestException("Invalid Oauth: vendor not exist");
        }

        switch (oauth.getVendor()) {
            case ORCID:
                OauthOrcid orcid = orcidService.find(oauth.getOauthId());
                if (orcid == null) {
                    throw new BadRequestException("Invalid Oauth token: token not exist");
                }
                if (orcid.isConnected()) {
                    throw new BadRequestException("Invalid Oauth Connection: already connected");
                }
                if (!orcid.getUuid().equals(oauth.getUuid())) {
                    throw new BadRequestException("Invalid Oauth token: token not matched");
                }

                orcid.setMember(member);
                orcid.setConnected(true);
                return;

            case FACEBOOK:
                OauthFacebook facebook = facebookService.find(oauth.getOauthId());
                if (facebook == null) {
                    throw new BadRequestException("Invalid Oauth token: token not exist");
                }
                if (facebook.isConnected()) {
                    throw new BadRequestException("Invalid Oauth Connection: already connected");
                }
                if (!facebook.getUuid().equals(oauth.getUuid())) {
                    throw new BadRequestException("Invalid Oauth token: token not matched");
                }

                facebook.setMember(member);
                facebook.setConnected(true);
                return;

            case GOOGLE:
                OauthGoogle google = googleService.find(oauth.getOauthId());
                if (google == null) {
                    throw new BadRequestException("Invalid Oauth token: token not exist");
                }
                if (google.isConnected()) {
                    throw new BadRequestException("Invalid Oauth Connection: already connected");
                }
                if (!google.getUuid().equals(oauth.getUuid())) {
                    throw new BadRequestException("Invalid Oauth token: token not matched");
                }

                google.setMember(member);
                google.setConnected(true);
                return;

            default:
        }
    }

    @Transactional
    public void connect2(OAuthToken token, Member member) {
        switch (token.getVendor()) {
            case ORCID:
                orcidService.connect(token.getToken(), member);
                return;

            case FACEBOOK:
                facebookService.connect(token.getToken(), member);
                return;

            case GOOGLE:
                googleService.connect(token.getToken(), member);
                return;

            default:
        }
    }

    public boolean getConnection(OauthUserDto oauth) {
        if (oauth.getVendor() == null) {
            throw new BadRequestException("Invalid Oauth: vendor not exist");
        }

        switch (oauth.getVendor()) {
            case ORCID:
                OauthOrcid orcid = orcidService.find(oauth.getOauthId());
                if (orcid == null) {
                    throw new BadRequestException("Invalid Oauth token: token not exist");
                }
                return orcid.isConnected();

            case FACEBOOK:
                OauthFacebook facebook = facebookService.find(oauth.getOauthId());
                if (facebook == null) {
                    throw new BadRequestException("Invalid Oauth token: token not exist");
                }
                return facebook.isConnected();

            case GOOGLE:
                OauthGoogle google = googleService.find(oauth.getOauthId());
                if (google == null) {
                    throw new BadRequestException("Invalid Oauth token: token not exist");
                }
                return google.isConnected();

            default:
                return false;
        }
    }

    public OAuthConnection getConnection(OAuthToken token) {
        switch (token.getVendor()) {
            case ORCID:
                return orcidService.getConnection(token.getToken());

            case FACEBOOK:
                return facebookService.getConnection(token.getToken());

            case GOOGLE:
                return googleService.getConnection(token.getToken());

            default:
                return null;
        }
    }

}
