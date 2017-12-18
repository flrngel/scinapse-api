package network.pluto.absolute.facade;

import network.pluto.absolute.dto.oauth.OauthUserDto;
import network.pluto.absolute.enums.OauthVendor;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.absolute.service.oauth.OauthFacebookService;
import network.pluto.absolute.service.oauth.OauthGoogleService;
import network.pluto.absolute.service.oauth.OauthOrcidService;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.models.oauth.OauthFacebook;
import network.pluto.bibliotheca.models.oauth.OauthGoogle;
import network.pluto.bibliotheca.models.oauth.OauthOrcid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

@Transactional(readOnly = true)
@Component
public class OauthFacade {

    private final OauthOrcidService orcidService;
    private final OauthFacebookService facebookService;
    private final OauthGoogleService googleService;

    @Autowired
    public OauthFacade(OauthOrcidService orcidService,
                       OauthFacebookService facebookService,
                       OauthGoogleService googleService) {
        this.orcidService = orcidService;
        this.facebookService = facebookService;
        this.googleService = googleService;
    }

    public URI getAuthorizeUri(OauthVendor vendor, String redirectUri) {
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
    public Member findMember(OauthVendor vendor, String code, String redirectUri) {
        switch (vendor) {
            case ORCID:
                OauthOrcid oauthOrcid = orcidService.exchange(code, redirectUri);
                return oauthOrcid.getMember();

            case FACEBOOK:
                OauthFacebook facebook = facebookService.exchange(code, redirectUri);
                return facebook.getMember();

            case GOOGLE:
                OauthGoogle google = googleService.exchange(code, redirectUri);
                return google.getMember();

            default:
                return null;
        }
    }

    @Transactional
    public void connect(OauthUserDto oauth, Member member) {
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

    public boolean isConnected(OauthUserDto oauth) {
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
}
