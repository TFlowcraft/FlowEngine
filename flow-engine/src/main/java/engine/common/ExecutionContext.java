package engine.common;

import com.database.entity.generated.tables.pojos.InstanceTasks;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.jooq.JSONB;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutionContext {
    private final InstanceTasks instanceTask;
    private final ConcurrentHashMap<String, Object> businessData;

    public ExecutionContext(InstanceTasks instanceTask, Map<String, Object> businessData) {
        this.instanceTask = instanceTask;
        this.businessData = new ConcurrentHashMap<>();
        if (businessData != null) {
            this.businessData.putAll(businessData);
        }
    }

    public Object getDataField(String key) {
        return businessData.get(key);
    }

    public <T> T getDataField(String key, Class<T> type) {
        Object value = businessData.get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public void putDataField(String key, Object value) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        businessData.put(key, value);
    }


    public void removeDataField(String key) {
        businessData.remove(key);
    }

    public boolean containsDataField(String key) {
        return businessData.containsKey(key);
    }


    public void mergeData(Map<String, Object> additionalData) {
        if (additionalData != null) {
            businessData.putAll(additionalData);
        }
    }


    public void clearData() {
        businessData.clear();
    }


    public Map<String, Object> getBusinessDataAsMap() {
        return Collections.unmodifiableMap(businessData);
    }


    public JSONB getBusinessDataAsJsonb() {
        try {
            return JSONB.valueOf(JsonUtils.OBJECT_MAPPER.writeValueAsString(businessData));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize business data", e);
        }
    }

    public void updateBusinessDataFromJsonb(JSONB json) {
        Map<String, Object> newData = JsonUtils.fromJsonb(json);
        businessData.clear();
        if (newData != null) {
            businessData.putAll(newData);
        }
    }

    public InstanceTasks getInstanceTask() {
        return instanceTask;
    }
}