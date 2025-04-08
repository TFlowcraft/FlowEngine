package engine.model;

import engine.common.JsonUtils;
import org.jooq.Converter;
import org.jooq.JSONB;

import java.util.Map;

public class JsonbToMapConverter implements Converter<JSONB, Map<String, Object>> {
    @Override
    public Map<String, Object> from(JSONB jsonb) {
        return JsonUtils.fromJsonb(jsonb);
    }

    @Override
    public JSONB to(Map<String, Object> map) {
        return JsonUtils.toJsonb(map);
    }

    @Override
    public Class<JSONB> fromType() {
        return JSONB.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Map<String, Object>> toType() {
        return (Class<Map<String, Object>>) (Class<?>) Map.class;
    }
}
