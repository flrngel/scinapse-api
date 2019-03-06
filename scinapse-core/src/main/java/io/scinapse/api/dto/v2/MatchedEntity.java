package io.scinapse.api.dto.v2;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
@NoArgsConstructor
public class MatchedEntity {

    private MatchedType type;
    private Object entity;

    public MatchedEntity(MatchedType type, Object entity) {
        this.type = type;
        this.entity = entity;
    }

    public enum MatchedType {
        AUTHOR,
        JOURNAL,
        AFFILIATION,
        FOS
    }

}
