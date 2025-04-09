package api.dto;

import java.util.UUID;

public class ProcessInfoDto {
    private UUID id;
    private String bpmnProcessId;
    private String processName;

    public ProcessInfoDto(UUID id, String bpmnProcessId, String processName) {
        this.id = id;
        this.bpmnProcessId = bpmnProcessId;
        this.processName = processName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBpmnProcessId() {
        return bpmnProcessId;
    }

    public void setBpmnProcessId(String bpmnProcessId) {
        this.bpmnProcessId = bpmnProcessId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }
}
