package io.scinapse.api.dto.v2;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class MatchedAuthor {

    private long totalElements = 0;
    private List<AuthorItemDto> content = new ArrayList<>();

    public MatchedAuthor(long totalElements, List<AuthorItemDto> authorItemDtos) {
        this.totalElements = totalElements;

        if (!CollectionUtils.isEmpty(authorItemDtos)) {
            this.content = authorItemDtos;
        }
    }

}
