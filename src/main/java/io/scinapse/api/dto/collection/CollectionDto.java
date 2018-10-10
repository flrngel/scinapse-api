package io.scinapse.api.dto.collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.scinapse.api.dto.MemberDto;
import io.scinapse.api.model.Collection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class CollectionDto {

    private long id;

    @JsonProperty("created_by")
    private MemberDto createdBy;

    @Size(min = 1, max = 100)
    @NotNull
    private String title;

    @Size(min = 1, max = 500)
    private String description;

    @JsonProperty("paper_count")
    private long paperCount;

    @JsonProperty("is_default")
    private boolean isDefault;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    protected CollectionDto(Collection collection) {
        this.id = collection.getId();
        this.createdBy = new MemberDto(collection.getCreatedBy());
        this.title = collection.getTitle();
        this.description = collection.getDescription();
        this.paperCount = collection.getPaperCount();
        this.isDefault = collection.isDefault();
        this.createdAt = collection.getCreatedAt();
        this.updatedAt = collection.getUpdatedAt();
    }

    public static CollectionDto of(Collection collection) {
        return new CollectionDto(collection);
    }

    public Collection toEntity() {
        Collection collection = new Collection();
        collection.setTitle(this.title);
        collection.setDescription(this.description);
        return collection;
    }

    public void setTitle(String title) {
        this.title = StringUtils.normalizeSpace(title);
    }

    public void setDescription(String description) {
        this.description = StringUtils.stripToNull(description);
    }

}
