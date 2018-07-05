package network.pluto.absolute.dto.collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import network.pluto.absolute.models.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class MyCollectionDto extends CollectionDto {

    @JsonProperty("contains_selected")
    private Boolean containsSelected;

    private MyCollectionDto(Collection collection) {
        super(collection);
    }

    private MyCollectionDto(Collection collection, boolean containsSelected) {
        super(collection);
        this.containsSelected = containsSelected;
    }

    public static MyCollectionDto of(Collection collection) {
        return new MyCollectionDto(collection);
    }

    public static MyCollectionDto of(Collection collection, boolean containsSelected) {
        return new MyCollectionDto(collection, containsSelected);
    }

}
