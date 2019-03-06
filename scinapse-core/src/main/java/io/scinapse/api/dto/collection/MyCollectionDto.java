package io.scinapse.api.dto.collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.scinapse.domain.data.scinapse.model.Collection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class MyCollectionDto extends CollectionDto {

    @JsonProperty("contains_selected")
    private boolean containsSelected = false;

    private String note;

    private MyCollectionDto(Collection collection) {
        super(collection);
    }

    private MyCollectionDto(Collection collection, String note) {
        this(collection);
        this.containsSelected = true;
        this.note = note;
    }

    public static MyCollectionDto of(Collection collection) {
        return new MyCollectionDto(collection);
    }

    public static MyCollectionDto of(Collection collection, String note) {
        return new MyCollectionDto(collection, note);
    }

}
