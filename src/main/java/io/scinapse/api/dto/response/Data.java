package io.scinapse.api.dto.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class Data<T> {

    private T content;
    private Page page;

    private Map<String, Object> additional = new HashMap<>();

    public Data(T content) {
        this.content = content;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditional() {
        return additional;
    }

    public void putAdditional(String key, Object value) {
        this.additional.put(key, value);
    }

}
