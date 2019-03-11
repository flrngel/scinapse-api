package io.scinapse.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
@Setter
public class CollectionWrapper {
    private long id;
    private String title;
    private OffsetDateTime updatedAt;
}
