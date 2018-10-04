package io.scinapse.api.dto.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class Data<T> {

    private T content;
    private Page page;

    private Map<String, Object> additional = new HashMap<>();

    private Data(T content) {
        this.content = content;
    }

    private Data(T content, Page page) {
        this(content);
        this.page = page;
    }

    public static <R> Data<R> of(R content) {
        return new Data<>(content);
    }

    public static <S, R extends org.springframework.data.domain.Page<S>> Data<List<S>> of(R content) {
        Page page = Page.of(content);
        return new Data<>(content.getContent(), page);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditional() {
        return additional;
    }

    public void putAdditional(String key, Object value) {
        this.additional.put(key, value);
    }

}
