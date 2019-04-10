package io.scinapse.api.service.mag;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import io.scinapse.api.academic.dto.AcConferenceInstanceDto;
import io.scinapse.domain.data.academic.model.Paper;
import io.scinapse.api.dto.mag.*;
import io.scinapse.api.service.author.AuthorLayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@XRayEnabled
@Component
@RequiredArgsConstructor
public class PaperConverter {

    private final AuthorLayerService authorLayerService;

    public PaperDto convertSingle(Paper paper, Converter converter) {
        PaperDto paperDto = converter.convert(paper);
        authorLayerService.decoratePaperAuthors(paperDto.getAuthors());
        return paperDto;
    }

    public static Converter simple() {
        return new Converter()
                .withAuthors()
                .withJournal()
                .withConferenceInstance();
    }
    public static Converter detail() {
        return simple()
                .withAbstract();
    }

    public static Converter full() {
        return detail()
                .withFos()
                .withUrl();
    }

    public List<PaperDto> convert(List<Paper> papers, Converter converter) {
        List<PaperDto> paperDtos = papers.stream()
                .map(converter::convert)
                .collect(Collectors.toList());

        List<PaperAuthorDto> paperAuthorDtos = paperDtos.stream()
                .map(PaperDto::getAuthors)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        authorLayerService.decoratePaperAuthors(paperAuthorDtos);

        return paperDtos;
    }

    public static class Converter {

        private Paper paper;
        private PaperDto dto;
        private Map<String, Runnable> detailLoaders = new HashMap<>();

        public Converter withAuthors() {
            if (detailLoaders.get("authors") != null) return this;
            detailLoaders.put("authors", () -> {
                if (!CollectionUtils.isEmpty(paper.getAuthors())) {
                    List<PaperAuthorDto> authors = paper.getAuthors()
                            .stream()
                            .map(PaperAuthorDto::new)
                            .sorted(Comparator.comparing(PaperAuthorDto::getOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                            .collect(Collectors.toList());
                    dto.setAuthors(authors);
                }
            });
            return this;

        }

        public Converter withJournal() {
            if (detailLoaders.get("journal") != null) return this;
            detailLoaders.put("journal", () -> {
                if (paper.getJournal() != null) {
                    JournalDto journal = new JournalDto(paper.getJournal());
                    dto.setJournal(journal);
                }
            });
            return this;
        }

        public Converter withConferenceInstance() {
            if (detailLoaders.get("conference_instance") != null) return this;
            detailLoaders.put("conference_instance", () -> {
                if (paper.getConferenceInstance() != null) {
                    AcConferenceInstanceDto conferenceInstance = new AcConferenceInstanceDto(paper.getConferenceInstance());
                    dto.setConferenceInstance(conferenceInstance);
                }
            });
            return this;
        }

        public Converter withUrl() {
            if (detailLoaders.get("url") != null) return this;
            detailLoaders.put("url", () -> {
                if (!CollectionUtils.isEmpty(paper.getPaperUrls())) {
                    List<PaperUrlDto> urls = paper.getPaperUrls().stream().map(PaperUrlDto::new).sorted(Comparator.comparing(PaperUrlDto::isPdf).reversed()).collect(Collectors.toList());
                    dto.setUrls(urls);
                    dto.setUrlCount(urls.size());
                }
            });
            return this;
        }

        public Converter withAbstract() {
            if (detailLoaders.get("abstract") != null) return this;
            detailLoaders.put("abstract", () -> {
                if (paper.getPaperAbstract() != null) {
                    String paperAbstract = paper.getPaperAbstract().getAbstract();
                    dto.setPaperAbstract(paperAbstract);
                }
            });
            return this;
        }

        public Converter withFos() {
            if (detailLoaders.get("fos") != null) return this;
            detailLoaders.put("fos", () -> {
                if (!CollectionUtils.isEmpty(paper.getPaperFosList())) {
                    List<PaperFosDto> fosList = paper.getPaperFosList().stream().map(PaperFosDto::new).collect(Collectors.toList());
                    dto.setFosList(fosList);
                    dto.setFosCount(fosList.size());
                }
            });
            return this;
        }

        private PaperDto convert(Paper paper) {
            this.paper = paper;
            this.dto = PaperDto.of(paper);
            this.detailLoaders.values().forEach(Runnable::run);
            return dto;
        }

    }

}
