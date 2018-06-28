package network.pluto.absolute.dto.collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import network.pluto.bibliotheca.models.CollectionPaper;
import org.springframework.util.StringUtils;

import javax.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class CollectionPaperAddRequest {

    @JsonProperty("collection_ids")
    @Size(min = 1, max = 10)
    private List<Long> collectionIds;

    @JsonProperty("paper_id")
    private long paperId;

    private String note;

    public List<CollectionPaper> toEntities() {
        return collectionIds
                .stream()
                .map(id -> {
                    CollectionPaper entity = new CollectionPaper();
                    CollectionPaper.CollectionPaperId cpId = CollectionPaper.CollectionPaperId.of(id, this.paperId);
                    entity.setId(cpId);
                    entity.setNote(this.note);
                    return entity;
                })
                .collect(Collectors.toList());
    }

    public void setNote(String note) {
        this.note = StringUtils.trimWhitespace(note);
    }

}
