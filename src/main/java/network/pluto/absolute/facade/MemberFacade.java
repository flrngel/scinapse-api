package network.pluto.absolute.facade;

import network.pluto.absolute.configuration.CacheName;
import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.security.TokenHelper;
import network.pluto.absolute.service.*;
import network.pluto.bibliotheca.models.Member;
import network.pluto.bibliotheca.models.Orcid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;

@Component
public class MemberFacade {

    private final MemberService memberService;
    private final ArticleService articleService;
    private final ReviewService reviewService;
    private final CommentService commentService;
    private final OAuthOrcidFacade oAuthOrcidFacade;
    private final TransactionService transactionService;
    private final VerificationService verificationService;
    private final TokenHelper tokenHelper;

    @Autowired
    public MemberFacade(MemberService memberService,
                        ArticleService articleService,
                        ReviewService reviewService,
                        CommentService commentService,
                        OAuthOrcidFacade oAuthOrcidFacade,
                        TransactionService transactionService,
                        VerificationService verificationService,
                        TokenHelper tokenHelper) {
        this.memberService = memberService;
        this.articleService = articleService;
        this.reviewService = reviewService;
        this.commentService = commentService;
        this.oAuthOrcidFacade = oAuthOrcidFacade;
        this.transactionService = transactionService;
        this.verificationService = verificationService;
        this.tokenHelper = tokenHelper;
    }

    @Cacheable(CacheName.Member.GET_DETAIL)
    public MemberDto getDetail(long memberId) {
        Member member = memberService.findMember(memberId);
        if (member == null) {
            throw new ResourceNotFoundException("Member not found");
        }

        long articleCount = articleService.getCount(member);
        long reviewCount = reviewService.getCount(member);
        long commentCount = commentService.getCount(member);

        MemberDto memberDto = new MemberDto(member);
        memberDto.setArticleCount(articleCount);
        memberDto.setReviewCount(reviewCount);
        memberDto.setCommentCount(commentCount);

        return memberDto;
    }

    @Transactional
    public Member create(HttpServletResponse response, MemberDto memberDto) {
        Member exist = memberService.findByEmail(memberDto.getEmail());
        if (exist != null) {
            throw new BadRequestException("Email already exists");
        }

        // extract institution
        memberDto.setInstitution(extractInstitution(memberDto.getEmail()));

        Member saved = memberService.saveMember(memberDto.toEntity());

        if (memberDto.getOrcid() != null) {
            Orcid orcid = oAuthOrcidFacade.getVerifiedOrcid(memberDto.getOrcid());
            authenticate(saved, orcid);
        }

        // send verification email
//        verificationService.sendVerification(saved);

        // auto login
        String jwt = tokenHelper.generateToken(saved);
        tokenHelper.addCookie(response, jwt);

        return saved;
    }

    @Transactional
    public void createWallet(Member member) {
        // send transaction
//        transactionService.createWallet(member);
    }

    @Transactional
    public MemberDto authenticate(long memberId, Orcid orcid) {
        Member member = memberService.getMember(memberId);
        return authenticate(member, orcid);
    }

    private MemberDto authenticate(Member member, Orcid orcid) {
        memberService.updateOrcid(member, orcid);
        return new MemberDto(member);
    }

    private String extractInstitution(String email) {
        if (StringUtils.isEmpty(email)) {
            return null;
        }

        String[] split = email.split("@");
        if (split.length != 2) {
            return null;
        }

        String host = split[1];
        String institution = host.split("\\.")[0];
        if (StringUtils.isEmpty(institution)) {
            return null;
        }

        // capitalize first letter only
        return StringUtils.capitalize(institution.toLowerCase());
    }
}
