package io.scinapse.domain.data.scinapse.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.scinapse.domain.util.JsonUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Nationalized;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
@Table(schema = "scinapse")
@Entity
public class MemberSavedFilter extends BaseEntity {

    @Id
    private long memberId;

    @Nationalized
    @Column(nullable = false)
    private String filterJson;

    public List<SavedFilter> getFilter() {
        try {
            return JsonUtils.fromJson(this.filterJson, List.class, SavedFilter.class);
        } catch (IOException e) {
            log.error("Fail to parse saved filter JSON.", e);
            return new ArrayList<>();
        }
    }

    public void setFilter(List<SavedFilter> savedFilters) {
        try {
            this.filterJson = JsonUtils.toJson(savedFilters);
        } catch (JsonProcessingException e) {
            log.error("Fail to write saved filter JSON.", e);
            this.filterJson = new ArrayList<>().toString();
        }
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @Getter
    @Setter
    public static class SavedFilter {
        @NotNull
        @Size(min = 1)
        private String name;

        @Size(min = 1)
        private String emoji;

        @NotNull
        @Size(min = 1)
        private String filter;
    }

}
