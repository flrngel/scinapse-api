package network.pluto.absolute.dto.collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.absolute.dto.MemberDto;
import network.pluto.absolute.model.Collection;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class CollectionDto {

    private long id;

    @JsonProperty("created_by")
    private MemberDto createdBy;

    @Size(min = 1, max = 60)
    @NotNull
    private String title;

    @Size(max = 500)
    private String description;

    @JsonProperty("paper_count")
    private long paperCount;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    protected CollectionDto(Collection collection) {
        this.id = collection.getId();
        this.createdBy = new MemberDto(collection.getCreatedBy());
        this.title = collection.getTitle();
        this.description = collection.getDescription();
        this.paperCount = collection.getPaperCount();
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
        this.title = StringUtils.trimWhitespace(title);
    }

    public void setDescription(String description) {
        this.description = StringUtils.trimWhitespace(description);
    }

}
