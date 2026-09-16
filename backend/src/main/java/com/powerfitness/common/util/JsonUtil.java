package com.powerfitness.common.util;

import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Thin wrapper around the application {@link ObjectMapper} for the jsonb columns
 * (assessment responses, analysis, plan content) — keeps the Jackson 3 API in one place.
 */
@Component
public class JsonUtil {

    private final ObjectMapper mapper;

    public JsonUtil(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String write(Object value) {
        return mapper.writeValueAsString(value);
    }

    public <T> T read(String json, Class<T> type) {
        return mapper.readValue(json == null || json.isBlank() ? "{}" : json, type);
    }

    public JsonNode tree(String json) {
        return mapper.readTree(json == null || json.isBlank() ? "{}" : json);
    }
}
