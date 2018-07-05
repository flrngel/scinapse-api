package network.pluto.absolute.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import network.pluto.absolute.dto.BookmarkDto;
import network.pluto.absolute.dto.PaperDto;
import network.pluto.absolute.error.BadRequestException;
import network.pluto.absolute.error.ResourceNotFoundException;
import network.pluto.absolute.facade.PaperFacade;
import network.pluto.absolute.model.Bookmark;
import network.pluto.absolute.model.Member;
import network.pluto.absolute.model.mag.Paper;
import network.pluto.absolute.security.jwt.JwtUser;
import network.pluto.absolute.service.BookmarkService;
import network.pluto.absolute.service.MemberService;
import network.pluto.absolute.service.mag.PaperService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
public class BookmarkController {

    private final MemberService memberService;
    private final BookmarkService bookmarkService;
    private final PaperService paperService;
    private final PaperFacade paperFacade;

    @RequestMapping(value = "/members/me/bookmarks", method = RequestMethod.GET)
    public Page<BookmarkDto> getBookmarks(@ApiIgnore JwtUser user, @PageableDefault Pageable pageable) {
        Member member = memberService.findMember(user.getId());
        if (member == null) {
            throw new ResourceNotFoundException("Member not found");
        }

        Page<Bookmark> bookmarks = bookmarkService.getBookmarks(member.getId(), pageable);

        List<Long> paperIds = bookmarks.getContent().stream().map(Bookmark::getPaperId).collect(Collectors.toList());
        Map<Long, PaperDto> paperMap = paperFacade.findIn(paperIds, PaperDto.compact())
                .stream()
                .collect(Collectors.toMap(
                        PaperDto::getId,
                        Function.identity()
                ));

        return bookmarks.map(b -> {
            BookmarkDto dto = BookmarkDto.bookmarked(b);
            dto.paper = paperMap.get(b.getPaperId());
            return dto;
        });
    }

    @RequestMapping(value = "/members/me/bookmarks", method = RequestMethod.POST)
    public Result saveBookmark(@ApiIgnore JwtUser user, @RequestBody PaperIdWrapper paperId) {
        Member member = memberService.findMember(user.getId());
        if (member == null) {
            throw new ResourceNotFoundException("Member not found");
        }

        Bookmark bookmark = bookmarkService.find(member.getId(), paperId.paperId);
        if (bookmark != null) {
            throw new BadRequestException("Bookmark already exists");
        }

        Paper paper = paperService.find(paperId.paperId);
        if (paper == null) {
            throw new ResourceNotFoundException("Paper not found");
        }

        bookmarkService.save(member.getId(), paper.getId());
        return Result.success();
    }

    @RequestMapping(value = "/members/me/bookmarks/check", method = RequestMethod.GET)
    public Map<String, Object> checkBookmarks(@ApiIgnore JwtUser user, @RequestParam("paper_ids") List<Long> paperIds) {
        Member member = memberService.findMember(user.getId());
        if (member == null) {
            throw new ResourceNotFoundException("Member not found");
        }

        Map<Long, Bookmark> bookmarkMap = bookmarkService.findIn(member.getId(), paperIds)
                .stream()
                .collect(Collectors.toMap(
                        Bookmark::getPaperId,
                        Function.identity()
                ));

        List<BookmarkDto> dtos = paperIds
                .stream()
                .map(id -> {
                    Bookmark bookmark = bookmarkMap.get(id);
                    if (bookmark != null) {
                        return BookmarkDto.bookmarked(bookmark);
                    } else {
                        return BookmarkDto.available(id);
                    }
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("data", dtos);
        return result;
    }

    @RequestMapping(value = "/members/me/bookmarks", method = RequestMethod.DELETE)
    public Result deleteBookmark(@ApiIgnore JwtUser user, @RequestBody PaperIdWrapper paperId) {
        Member member = memberService.findMember(user.getId());
        if (member == null) {
            throw new ResourceNotFoundException("Member not found");
        }

        Bookmark bookmark = bookmarkService.find(member.getId(), paperId.paperId);
        if (bookmark == null) {
            throw new ResourceNotFoundException("Bookmark not found");
        }

        bookmarkService.delete(bookmark);
        return Result.success();
    }

    public static class PaperIdWrapper {
        @JsonProperty("paper_id")
        public long paperId;
    }

}
