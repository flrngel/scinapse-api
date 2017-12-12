package network.pluto.absolute.facade;

import network.pluto.absolute.dto.oauth.OauthUserDto;
import network.pluto.absolute.enums.OAuthVendor;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.absolute.service.OauthFacebookService;
import network.pluto.absolute.service.OauthOrcidService;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.models.oauth.OauthFacebook;
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

    @Autowired
    public OauthFacade(OauthOrcidService orcidService, OauthFacebookService facebookService) {
        this.orcidService = orcidService;
        this.facebookService = facebookService;
    }

    public URI getAuthorizeUri(OAuthVendor vendor) {
        switch (vendor) {
            case ORCID:
                return orcidService.getAuthorizeUri();

            case FACEBOOK:
                return facebookService.getAuthorizeUri();

            default:
                return null;
        }
    }

    @Transactional
    public OauthUserDto exchange(OAuthVendor vendor, String code) {
        OauthUserDto dto = new OauthUserDto();

        switch (vendor) {
            case ORCID:
                OauthOrcid oauthOrcid = orcidService.exchange(code);

                dto.setVendor(OAuthVendor.ORCID);
                dto.setOauthId(oauthOrcid.getOrcid());
                dto.setUuid(oauthOrcid.getUuid());
                dto.setUserData(oauthOrcid.getUserData());

                break;

            case FACEBOOK:
                OauthFacebook facebook = facebookService.exchange(code);

                dto.setVendor(OAuthVendor.FACEBOOK);
                dto.setOauthId(facebook.getFacebookId());
                dto.setUuid(facebook.getUuid());
                dto.setUserData(facebook.getUserData());

                break;

            default:
        }

        return dto;
    }

    @Transactional
    public Member findMember(OAuthVendor vendor, String code) {
        switch (vendor) {
            case ORCID:
                OauthOrcid oauthOrcid = orcidService.exchange(code);
                return oauthOrcid.getMember();

            case FACEBOOK:
                OauthFacebook facebook = facebookService.exchange(code);
                return facebook.getMember();

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

            default:
        }
    }
}
