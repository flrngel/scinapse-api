package network.pluto.absolute.facade;

import network.pluto.absolute.configuration.CacheName;
import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.security.TokenHelper;
import network.pluto.absolute.service.*;
import network.pluto.bibliotheca.enums.AuthorityName;
import network.pluto.bibliotheca.models.Member;
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
    private final TransactionService transactionService;
    private final EmailVerificationService emailVerificationService;
    private final TokenHelper tokenHelper;
    private final OauthFacade oauthFacade;

    @Autowired
    public MemberFacade(MemberService memberService,
                        ArticleService articleService,
                        ReviewService reviewService,
                        CommentService commentService,
                        TransactionService transactionService,
                        EmailVerificationService emailVerificationService,
                        TokenHelper tokenHelper, OauthFacade oauthFacade) {
        this.memberService = memberService;
        this.articleService = articleService;
        this.reviewService = reviewService;
        this.commentService = commentService;
        this.transactionService = transactionService;
        this.emailVerificationService = emailVerificationService;
        this.tokenHelper = tokenHelper;
        this.oauthFacade = oauthFacade;
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

        if (!StringUtils.hasText(memberDto.getPassword()) || memberDto.getPassword().length() < 8) {
            throw new BadRequestException("Password length must be greater than or equal to 8");
        }

        Member saved = memberService.saveMember(memberDto.toEntity());

        // send verification email
        emailVerificationService.sendVerification(saved);

        // auto login
        String jwt = tokenHelper.generateToken(saved);
        tokenHelper.addCookie(response, jwt);

        return saved;
    }

    @Transactional
    public Member createOauthMember(HttpServletResponse response, MemberDto memberDto) {
        Member exist = memberService.findByEmail(memberDto.getEmail());
        if (exist != null) {
            throw new BadRequestException("Email already exists");
        }

        if (memberDto.getOauth() == null) {
            throw new BadRequestException("Invalid Oauth Information: not exist");
        }

        if (oauthFacade.isConnected(memberDto.getOauth())) {
            throw new BadRequestException("Invalid Connection: already connected");
        }

        Member saved = memberService.saveMember(memberDto.toEntity());
        memberService.updateAuthority(saved, AuthorityName.ROLE_USER);

        oauthFacade.connect(memberDto.getOauth(), saved);

        // auto login
        String jwt = tokenHelper.generateToken(saved);
        tokenHelper.addCookie(response, jwt);

        return saved;
    }

    @Transactional
    public void createWallet(Member member) {
        // send transaction
        transactionService.createWallet(member);
    }
}
