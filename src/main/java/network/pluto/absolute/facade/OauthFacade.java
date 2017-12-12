package network.pluto.absolute.facade;

import network.pluto.absolute.dto.oauth.OauthUserDto;
import network.pluto.absolute.enums.OAuthVendor;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.absolute.service.OauthFacebookService;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.models.oauth.OauthFacebook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Transactional(readOnly = true)
@Component
public class OauthFacade {

    private final RestTemplate restTemplate;
    private final OauthFacebookService facebookService;

    @Autowired
    public OauthFacade(RestTemplate restTemplate, OauthFacebookService facebookService) {
        this.restTemplate = restTemplate;
        this.facebookService = facebookService;
    }

    public URI getAuthorizeUri(OAuthVendor vendor) {
        switch (vendor) {
            case ORCID:

            case FACEBOOK:
                return facebookService.getAuthorizeUri();
            default:
                return null;
        }
    }

    @Transactional
    public OauthUserDto exchange(OAuthVendor vendor, String code) {
        switch (vendor) {
            case ORCID:

            case FACEBOOK:
                OauthFacebook facebook = facebookService.exchange(code);

                OauthUserDto dto = new OauthUserDto();
                dto.setVendor(OAuthVendor.FACEBOOK);
                dto.setOauthId(facebook.getFacebookId());
                dto.setUuid(facebook.getUuid());
                dto.setUserData(facebook.getUserData());

                return dto;

            default:
                return null;
        }
    }

    public Member findMember(OAuthVendor vendor, String code) {
        switch (vendor) {
            case ORCID:

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
