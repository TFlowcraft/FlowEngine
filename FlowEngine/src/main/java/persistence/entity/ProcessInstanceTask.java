package persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProcessInstanceTask {
    private UUID id;
    private UUID processInstanceId;
    private UUID taskId;
    private String description;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private UUID parentTaskId;

    public ProcessInstanceTask(UUID id, UUID processInstanceId, UUID taskId, String status, LocalDateTime startTime, LocalDateTime endTime, UUID parentTaskId, String description) {
        this.id = id;
        this.processInstanceId = processInstanceId;
        this.taskId = taskId;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.parentTaskId = parentTaskId;
        this.description = description;
    }

    public ProcessInstanceTask() {

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public UUID getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(UUID parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
