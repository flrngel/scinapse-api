package io.scinapse.api.dto.collection;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Getter
@Setter
public class CollectionPaperUpdateRequest {
    private String note;

    public void setNote(String note) {
        this.note = StringUtils.trimWhitespace(note);
    }
}
