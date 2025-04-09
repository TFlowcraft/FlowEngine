package engine.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jooq.JSONB;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class JsonUtils {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


    public static JSONB toJsonb(Map<String, Object> data) {
        try {
            return JSONB.valueOf(OBJECT_MAPPER.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON serialization error", e);
        }
    }


    public static Map<String, Object> fromJsonb(JSONB json) {
        if (json == null || json.data().equals("null")) return new ConcurrentHashMap<>();
        try {
            return OBJECT_MAPPER.readValue(
                    json.data(),
                    new TypeReference<ConcurrentHashMap<String, Object>>() {}
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON deserialization error", e);
        }
    }
}