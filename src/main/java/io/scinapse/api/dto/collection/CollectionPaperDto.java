package io.scinapse.api.dto.collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.scinapse.api.dto.mag.PaperDto;
import io.scinapse.api.model.CollectionPaper;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class CollectionPaperDto {

    @ApiModelProperty(readOnly = true)
    @JsonProperty("collection_id")
    private long collectionId;

    @JsonProperty("paper_id")
    private long paperId;

    private String note;

    @ApiModelProperty(readOnly = true)
    private PaperDto paper;

    private CollectionPaperDto(CollectionPaper collectionPaper) {
        this.collectionId = collectionPaper.getId().getCollectionId();
        this.paperId = collectionPaper.getId().getPaperId();
        this.note = collectionPaper.getNote();
    }

    public static CollectionPaperDto of(CollectionPaper collectionPaper) {
        return new CollectionPaperDto(collectionPaper);
    }

    public CollectionPaper toEntity() {
        CollectionPaper entity = new CollectionPaper();
        CollectionPaper.CollectionPaperId id = CollectionPaper.CollectionPaperId.of(this.collectionId, this.paperId);
        entity.setId(id);
        entity.setNote(this.note);
        return entity;
    }

}
