package io.scinapse.api.dto.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class Data<T> {

    private T content;
    private Page page;

    @JsonUnwrapped
    private Object additional;

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

    public static <R> Data<R> of(R content, Object additional) {
        Data<R> data = of(content);
        data.setAdditional(additional);
        return data;
    }

    public static <S, R extends org.springframework.data.domain.Page<S>> Data<List<S>> of(R content, Object additional) {
        Data<List<S>> data = of(content);
        data.setAdditional(additional);
        return data;
    }

}
