package api.dto;

import engine.common.Status;

import java.time.OffsetDateTime;
import java.util.UUID;

public class TaskDto {
    private UUID id;
    private UUID instanceId;
    private String bpmnElementId;
    private String taskName;
    private Status status;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;

    public TaskDto(String bpmnElementId, String taskName, Status status, OffsetDateTime startTime, OffsetDateTime endTime, UUID instanceId, UUID id) {
        this.bpmnElementId = bpmnElementId;
        this.taskName = taskName;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.instanceId = instanceId;
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(UUID instanceId) {
        this.instanceId = instanceId;
    }

    public String getBpmnElementId() {
        return bpmnElementId;
    }

    public void setBpmnElementId(String bpmnElementId) {
        this.bpmnElementId = bpmnElementId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
    }

}
