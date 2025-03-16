package persistence.entity;

import java.sql.Timestamp;
import java.util.UUID;

public class ProcessInstance {
    private UUID id;
    private Timestamp startTime;
    private Timestamp endTime;
    private String businessKey;

    public ProcessInstance(UUID id, Timestamp startTime, Timestamp endTime, String businessKey) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.businessKey = businessKey;
    }

    public ProcessInstance() {

    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }
}
