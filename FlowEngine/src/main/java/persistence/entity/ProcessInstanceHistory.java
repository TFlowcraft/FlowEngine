package persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProcessInstanceHistory {
    private UUID id;
    private UUID processInstanceId;
    private UUID taskId;
    private LocalDateTime timestamp;

    public ProcessInstanceHistory(UUID id, UUID processInstanceId, UUID taskId, LocalDateTime timestamp) {
        this.id = id;
        this.processInstanceId = processInstanceId;
        this.taskId = taskId;
        this.timestamp = timestamp;
    }

    public ProcessInstanceHistory() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(UUID processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
