package io.scinapse.domain.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtils {

    private static final ObjectMapper MAPPER;

    static {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        MAPPER = mapper;
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    public static String toJson(Object object) throws JsonProcessingException {
        return MAPPER.writeValueAsString(object);
    }

    public static <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return MAPPER.readValue(json, clazz);
    }

    public static <T> List<T> fromJson(String json, Class<? extends Collection> collection, Class<T> clazz) throws IOException {
        CollectionType collectionType = JsonUtils.getMapper().getTypeFactory().constructCollectionType(collection, clazz);
        return MAPPER.readValue(json, collectionType);
    }

    public static <T> T convert(Object fromValue, Class<T> toValueType) {
        return MAPPER.convertValue(fromValue, toValueType);
    }

    public static <T> T getValue(String json, String key, Class<T> type) throws IOException {
        JsonNode node = MAPPER.readTree(json).get(key);
        if (node != null) {
            return type.cast(node.textValue());
        }
        return null;
    }

}
