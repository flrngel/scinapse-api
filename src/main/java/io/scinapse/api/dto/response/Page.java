package io.scinapse.api.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class Page {

    private int size;
    private int page;
    private boolean first;
    private boolean last;
    private int numberOfElements;
    private long totalElements;
    private long totalPages;

    private Page(org.springframework.data.domain.Page page) {
        this.size = page.getSize();
        this.page = page.getNumber();
        this.first = page.isFirst();
        this.last = page.isLast();
        this.numberOfElements = page.getNumberOfElements();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
    }

    public static Page of(org.springframework.data.domain.Page page) {
        return new Page(page);
    }

}
