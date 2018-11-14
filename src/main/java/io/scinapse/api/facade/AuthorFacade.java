package io.scinapse.api.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.api.dto.PaperTitleDto;
import io.scinapse.api.dto.mag.AuthorDto;
import io.scinapse.api.dto.mag.AuthorLayerUpdateDto;
import io.scinapse.api.dto.mag.AuthorPaperDto;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.error.BadRequestException;
import io.scinapse.api.model.Member;
import io.scinapse.api.model.author.AuthorLayer;
import io.scinapse.api.model.author.AuthorLayerPaper;
import io.scinapse.api.model.mag.Author;
import io.scinapse.api.model.mag.AuthorTopPaper;
import io.scinapse.api.model.mag.Paper;
import io.scinapse.api.service.SearchService;
import io.scinapse.api.service.author.AuthorLayerService;
import io.scinapse.api.service.mag.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@XRayEnabled
@Transactional(readOnly = true)
@Component
@RequiredArgsConstructor
public class AuthorFacade {

    private final SearchService searchService;
    private final AuthorService authorService;
    private final PaperFacade paperFacade;
    private final AuthorLayerService layerService;

    public Page<AuthorDto> searchAuthor(String keyword, PageRequest pageRequest) {
        // author search from ES
        Page<Long> authorIdPage = searchService.searchAuthor(keyword, pageRequest);

        // get detail from RDB
        // automatically filter authors who have no paper at all
        List<AuthorTopPaper> authorTopPapers = authorService.findAuthorTopPaper(authorIdPage.getContent());

        // transform to author dto
        Map<Long, List<AuthorTopPaper>> map = authorTopPapers.stream().collect(Collectors.groupingBy(ap -> ap.getId().getAuthorId()));

        List<AuthorDto> authorDtos = authorIdPage.getContent()
                .stream()
                .map(id -> {
                    List<AuthorTopPaper> topPapers = map.get(id);
                    if (CollectionUtils.isEmpty(topPapers)) {
                        return null;
                    }

                    AuthorDto dto = new AuthorDto(topPapers.get(0).getAuthor());

                    List<Paper> papers = topPapers.stream().map(AuthorTopPaper::getPaper).collect(Collectors.toList());
                    List<PaperDto> paperDtos = paperFacade.convert(papers, PaperDto.simple());
                    dto.setTopPapers(paperDtos);

                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageImpl<>(authorDtos, pageRequest.toPageable(), authorIdPage.getTotalElements());
    }

    public Page<AuthorPaperDto> findPapers(long authorId, String[] keywords, PageRequest pageRequest) {
        if (layerService.exists(authorId)) {
            return layerService.findPapers(authorId, keywords, pageRequest)
                    .map(lp -> {
                        PaperDto paperDto = PaperDto.detail().convert(lp.getPaper());
                        return new AuthorPaperDto(paperDto, lp.getStatus(), lp.isSelected());
                    });
        } else {
            return authorService.getAuthorPaper(authorId, pageRequest)
                    .map(ap -> {
                        PaperDto paperDto = PaperDto.detail().convert(ap);
                        return new AuthorPaperDto(paperDto, AuthorLayerPaper.PaperStatus.SYNCED, false);
                    });
        }
    }

    @Transactional
    public void connect(Member member, long authorId) {
        Author author = authorService.find(authorId)
                .orElseThrow(() -> new BadRequestException("The author[" + authorId + "] does not exist."));

        layerService.connect(member, author);
    }

    @Transactional
    public void removePapers(Member member, long authorId, List<Long> paperIds) {
        AuthorLayer layer = findLayer(authorId);
        checkOwner(member, layer.getAuthorId());

        layerService.removePapers(layer, paperIds);
    }

    @Transactional
    public void addPapers(Member member, long authorId, List<Long> paperIds) {
        AuthorLayer layer = findLayer(authorId);
        checkOwner(member, layer.getAuthorId());

        layerService.addPapers(layer, paperIds);
    }

    @Transactional
    public List<PaperDto> updateSelected(Member member, long authorId, List<Long> selectedPaperIds) {
        Author author = find(authorId);
        checkOwner(member, author.getId());

        return layerService.updateSelected(author, selectedPaperIds)
                .stream()
                .map(AuthorLayerPaper::getPaper)
                .map(PaperDto.detail()::convert)
                .collect(Collectors.toList());
    }

    public AuthorDto findDetailed(long authorId) {
        Author author = find(authorId);
        AuthorDto dto = new AuthorDto(author);

        // just return original author if it does not have a layer.
        if (author.getLayer() == null) {
            return dto;
        }

        // put detailed information.
        dto.putDetail(author.getLayer());

        // add selected publication information.
        List<PaperDto> selected = layerService.findSelectedPapers(authorId)
                .stream()
                .map(AuthorLayerPaper::getPaper)
                .map(PaperDto.detail()::convert)
                .collect(Collectors.toList());
        dto.setSelectedPapers(selected);

        return dto;
    }

    @Transactional
    public AuthorDto update(Member member, long authorId, AuthorLayerUpdateDto updateDto) {
        AuthorLayer layer = findLayer(authorId);
        checkOwner(member, layer.getAuthorId());

        layerService.update(layer, updateDto);
        return findDetailed(authorId);
    }

    public List<PaperTitleDto> getAllPaperTitles(long authorId) {
        AuthorLayer layer = findLayer(authorId);
        return layerService.getAllPaperTitles(layer);
    }

    private Author find(long authorId) {
        return authorService.find(authorId)
                .orElseThrow(() -> new BadRequestException("Author[" + authorId + "] does not exist."));
    }

    private AuthorLayer findLayer(long authorId) {
        return layerService.find(authorId)
                .orElseThrow(() -> new BadRequestException("The author[" + authorId + "] does not have a layer."));
    }

    private void checkOwner(Member member, long authorId) {
        if (member.getAuthorId() == null || member.getAuthorId() != authorId) {
            throw new BadRequestException("Member[" + member.getId() + "] is not connected with Author[" + authorId + "].");
        }
    }

}
