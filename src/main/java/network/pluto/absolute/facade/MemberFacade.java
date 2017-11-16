package network.pluto.absolute.facade;

import network.pluto.absolute.configuration.CacheName;
import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.dto.OrcidDto;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.service.ArticleService;
import network.pluto.absolute.service.CommentService;
import network.pluto.absolute.service.MemberService;
import network.pluto.absolute.service.ReviewService;
import network.pluto.bibliotheca.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MemberFacade {

    private final MemberService memberService;
    private final ArticleService articleService;
    private final ReviewService reviewService;
    private final CommentService commentService;

    @Autowired
    public MemberFacade(MemberService memberService,
                        ArticleService articleService,
                        ReviewService reviewService,
                        CommentService commentService) {
        this.memberService = memberService;
        this.articleService = articleService;
        this.reviewService = reviewService;
        this.commentService = commentService;
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
    public MemberDto authenticate(long memberId, OrcidDto token) {
        Member member = memberService.getMember(memberId);
        memberService.updateOrcid(member, token.toEntity());
        return new MemberDto(member);
    }
}
