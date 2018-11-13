package io.scinapse.api.dto.mag;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.scinapse.api.dto.CommentDto;
import io.scinapse.api.dto.PaperImageDto;
import io.scinapse.api.model.mag.Paper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class PaperDto {

    private long id;

    private String title;

    private int year;

    @JsonProperty("abstract")
    private String paperAbstract;

    private String lang;

    private String doi;

    private String volume;

    private String issue;

    private String pageStart;

    private String pageEnd;

    private long referenceCount = 0;

    private long citedCount = 0;

    private long fosCount = 0;

    private long urlCount = 0;

    private long commentCount = 0;

    private JournalDto journal;

    private List<PaperAuthorDto> authors = new ArrayList<>();

    private List<PaperFosDto> fosList = new ArrayList<>();

    private List<PaperUrlDto> urls = new ArrayList<>();

    private List<PaperImageDto> images = new ArrayList<>();

    private List<CommentDto> comments = new ArrayList<>();


    private PaperDto(Paper paper) {
        this.id = paper.getId();
        this.title = paper.getTitle();
        this.year = paper.getYear();
        this.doi = paper.getDoi();
        this.volume = paper.getVolume();
        this.issue = paper.getIssue();
        this.referenceCount = paper.getPaperCount() != null ? paper.getPaperCount() : 0;
        this.citedCount = paper.getCitationCount() != null ? paper.getCitationCount() : 0;

        if (!CollectionUtils.isEmpty(paper.getAuthors())) {
            this.authors = paper.getAuthors().stream().map(PaperAuthorDto::new).collect(Collectors.toList());
        }
    }

    public static PaperDto of(Paper paper) {
        return new PaperDto(paper);
    }

    public static Converter converter() {
        return new Converter();
    }

    public static Converter simple() {
        return new Converter()
                .withJournal();
    }

    public static Converter compact() {
        return simple()
                .withUrl();
    }

    public static Converter detail() {
        return compact()
                .withAbstract();
    }

    public static Converter full() {
        return detail()
                .withFos();
    }

    public static class Converter {

        private Paper paper;
        private PaperDto dto;
        private Map<String, Runnable> detailLoaders = new HashMap<>();

        private Converter() {
        }

        public Converter withJournal() {
            if (detailLoaders.get("journal") != null) return this;
            detailLoaders.put("journal", () -> {
                if (paper.getJournal() != null) {
                    dto.journal = new JournalDto(paper.getJournal());
                }
            });
            return this;
        }

        public Converter withUrl() {
            if (detailLoaders.get("url") != null) return this;
            detailLoaders.put("url", () -> {
                if (!CollectionUtils.isEmpty(paper.getPaperUrls())) {
                    dto.urls = paper.getPaperUrls().stream().map(PaperUrlDto::new).collect(Collectors.toList());
                    dto.urlCount = dto.urls.size();
                }
            });
            return this;
        }

        public Converter withAbstract() {
            if (detailLoaders.get("abstract") != null) return this;
            detailLoaders.put("abstract", () -> {
                if (paper.getPaperAbstract() != null) {
                    dto.paperAbstract = paper.getPaperAbstract().getAbstract();
                }
            });
            return this;
        }

        public Converter withFos() {
            if (detailLoaders.get("fos") != null) return this;
            detailLoaders.put("fos", () -> {
                if (!CollectionUtils.isEmpty(paper.getPaperFosList())) {
                    dto.fosList = paper.getPaperFosList().stream().map(PaperFosDto::new).collect(Collectors.toList());
                    dto.fosCount = dto.fosList.size();
                }
            });
            return this;
        }

        public PaperDto convert(Paper paper) {
            this.paper = paper;
            this.dto = new PaperDto(paper);
            this.detailLoaders.values().forEach(Runnable::run);
            return dto;
        }

    }

}
