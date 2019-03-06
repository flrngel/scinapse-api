package io.scinapse.api.facade;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.domain.configuration.AcademicJpaConfig;
import io.scinapse.api.controller.PageRequest;
import io.scinapse.domain.data.academic.Paper;
import io.scinapse.domain.data.scinapse.model.author.AuthorLayerPaper;
import io.scinapse.api.dto.mag.AuthorDto;
import io.scinapse.api.dto.mag.AuthorPaperDto;
import io.scinapse.api.error.ResourceNotFoundException;
import io.scinapse.api.service.mag.AuthorService;
import io.scinapse.api.service.mag.PaperConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@XRayEnabled
@Transactional(readOnly = true, transactionManager = AcademicJpaConfig.ACADEMIC_TX_MANAGER)
@Component
@RequiredArgsConstructor
public class AuthorFacade {

    private final AuthorService authorService;
    private final PaperConverter paperConverter;

    public Page<AuthorPaperDto> findPapers(long authorId, PageRequest pageRequest) {
        Page<Paper> paperPage = authorService.getAuthorPaper(authorId, pageRequest);

        List<AuthorPaperDto> paperDtos = paperConverter.convert(paperPage.getContent(), PaperConverter.detail())
                .stream()
                .map(dto -> new AuthorPaperDto(dto, AuthorLayerPaper.PaperStatus.SYNCED, false))
                .collect(Collectors.toList());

        return new PageImpl<>(paperDtos, pageRequest.toPageable(), paperPage.getTotalElements());
    }

    public AuthorDto find(long authorId, boolean loadFos) {
        return authorService.find(authorId)
                .map(author -> new AuthorDto(author, loadFos))
                .orElseThrow(() -> new ResourceNotFoundException("Author[" + authorId + "] does not exist."));
    }

}
