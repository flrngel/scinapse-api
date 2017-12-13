package network.pluto.absolute.facade;

import network.pluto.absolute.dto.oauth.OauthUserDto;
import network.pluto.absolute.enums.OAuthVendor;
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

    public URI getAuthorizeUri(OAuthVendor vendor, String redirectUri) {
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
    public OauthUserDto exchange(OAuthVendor vendor, String code, String redirectUri) {
        OauthUserDto dto = new OauthUserDto();

        switch (vendor) {
            case ORCID:
                OauthOrcid oauthOrcid = orcidService.exchange(code, redirectUri);

                dto.setVendor(OAuthVendor.ORCID);
                dto.setOauthId(oauthOrcid.getOrcid());
                dto.setUuid(oauthOrcid.getUuid());
                dto.setUserData(oauthOrcid.getUserData());

                break;

            case FACEBOOK:
                OauthFacebook facebook = facebookService.exchange(code, redirectUri);

                dto.setVendor(OAuthVendor.FACEBOOK);
                dto.setOauthId(facebook.getFacebookId());
                dto.setUuid(facebook.getUuid());
                dto.setUserData(facebook.getUserData());

                break;

            case GOOGLE:
                OauthGoogle google = googleService.exchange(code, redirectUri);

                dto.setVendor(OAuthVendor.GOOGLE);
                dto.setOauthId(google.getGoogleId());
                dto.setUuid(google.getUuid());
                dto.setUserData(google.getUserData());

                break;

            default:
        }

        return dto;
    }

    @Transactional
    public Member findMember(OAuthVendor vendor, String code, String redirectUri) {
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
                OauthOrcid oauthOrcid = orcidService.find(oauth.getOauthId());
                if (oauthOrcid == null) {
                    throw new BadRequestException("Invalid Oauth token : token not exist");
                }
                if (!oauthOrcid.getUuid().equals(oauth.getUuid())) {
                    throw new BadRequestException("Invalid Oauth token : token not matched");
                }

                oauthOrcid.setMember(member);
                return;

            case FACEBOOK:
                OauthFacebook facebook = facebookService.find(oauth.getOauthId());
                if (facebook == null) {
                    throw new BadRequestException("Invalid Oauth token : token not exist");
                }
                if (!facebook.getUuid().equals(oauth.getUuid())) {
                    throw new BadRequestException("Invalid Oauth token : token not matched");
                }

                facebook.setMember(member);
                return;

            case GOOGLE:
                OauthGoogle google = googleService.find(oauth.getOauthId());
                if (google == null) {
                    throw new BadRequestException("Invalid Oauth token : token not exist");
                }
                if (!google.getUuid().equals(oauth.getUuid())) {
                    throw new BadRequestException("Invalid Oauth token : token not matched");
                }

                google.setMember(member);
                return;

            default:
        }
    }
}
