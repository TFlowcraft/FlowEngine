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

    // Получить значение по ключу
    public Object getDataField(String key) {
        return businessData.get(key);
    }

    // Получить значение с приведением типа
    public <T> T getDataField(String key, Class<T> type) {
        Object value = businessData.get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    // Добавить или обновить поле
    public void putDataField(String key, Object value) {
        if (key == null) throw new IllegalArgumentException("Key cannot be null");
        businessData.put(key, value);
    }

    // Удалить поле
    public void removeDataField(String key) {
        businessData.remove(key);
    }

    // Проверить наличие поля
    public boolean containsDataField(String key) {
        return businessData.containsKey(key);
    }

    // Объединить с другой мапой данных
    public void mergeData(Map<String, Object> additionalData) {
        if (additionalData != null) {
            businessData.putAll(additionalData);
        }
    }

    // Очистить все данные
    public void clearData() {
        businessData.clear();
    }

    // Получить данные как неизменяемую мапу
    public Map<String, Object> getBusinessDataAsMap() {
        return Collections.unmodifiableMap(businessData);
    }

    // Получить данные в формате JSONB
    public JSONB getBusinessDataAsJsonb() {
        try {
            return JSONB.valueOf(JsonUtils.OBJECT_MAPPER.writeValueAsString(businessData));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize business data", e);
        }
    }

    // Обновить данные из JSONB (например, после изменений в БД)
    public void updateBusinessDataFromJsonb(JSONB json) {
        Map<String, Object> newData = JsonUtils.fromJsonb(json);
        businessData.clear();
        if (newData != null) {
            businessData.putAll(newData);
        }
    }

    // Получить задачу, связанную с контекстом
    public InstanceTasks getInstanceTask() {
        return instanceTask;
    }
}