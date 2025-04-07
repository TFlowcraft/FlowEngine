package engine.common;

import com.database.entity.generated.tables.pojos.InstanceTasks;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.jooq.JSONB;

public class ExecutionContext {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final InstanceTasks instanceTask;
    private JSONB businessData;

    public ExecutionContext(InstanceTasks instanceTask, JSONB businessData) {
        this.instanceTask = instanceTask;
        this.businessData = businessData != null ? businessData : JSONB.valueOf("{}");
    }

    public JSONB getBusinessData() {
        return businessData;
    }

    public void setBusinessData(JSONB businessData) {
        this.businessData = businessData != null ? businessData : JSONB.valueOf("{}");
    }

    public InstanceTasks getInstanceTask() {
        return instanceTask;
    }

    public Object getDataField(String key) {
        try {
            Map<String, Object> dataMap = OBJECT_MAPPER.readValue(
                    businessData.data(),
                    new TypeReference<Map<String, Object>>() {}
            );
            return dataMap.get(key);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error reading business data field: " + key, e);
        }
    }

    public <T> T getDataField(String key, Class<T> type) {
        Object value = getDataField(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public void putDataField(String key, Object value) {
        try {
            Map<String, Object> dataMap = getBusinessDataAsMap();
            dataMap.put(key, value);
            updateBusinessData(dataMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error updating business data field: " + key, e);
        }
    }

    public void removeDataField(String key) {
        try {
            Map<String, Object> dataMap = getBusinessDataAsMap();
            dataMap.remove(key);
            updateBusinessData(dataMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error removing business data field: " + key, e);
        }
    }

    public boolean containsDataField(String key) {
        try {
            return getBusinessDataAsMap().containsKey(key);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error checking business data field: " + key, e);
        }
    }

    public void mergeData(JSONB additionalData) {
        try {
            Map<String, Object> currentData = getBusinessDataAsMap();
            Map<String, Object> newData = OBJECT_MAPPER.readValue(
                    additionalData.data(),
                    new TypeReference<Map<String, Object>>() {}
            );

            currentData.putAll(newData);
            updateBusinessData(currentData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error merging business data", e);
        }
    }

    public void clearData() {
        this.businessData = JSONB.valueOf("{}");
    }

    public Map<String, Object> getBusinessDataAsMap() throws JsonProcessingException {
        return businessData != null
                ? OBJECT_MAPPER.readValue(businessData.data(), new TypeReference<Map<String, Object>>() {})
                : new HashMap<>();
    }

    private void updateBusinessData(Map<String, Object> dataMap) throws JsonProcessingException {
        this.businessData = JSONB.valueOf(
                OBJECT_MAPPER.writeValueAsString(dataMap)
        );
    }
}