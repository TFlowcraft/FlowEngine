package api.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import org.jooq.XML;

import java.util.UUID;

public class ProcessInfoDto {
    private UUID id;
    private String bpmnProcessId;
    private String processName;
    @JsonRawValue
    private String bpmnScheme;

    public ProcessInfoDto(UUID id, String bpmnProcessId, String processName, XML bpmnScheme) {
        this.id = id;
        this.bpmnProcessId = bpmnProcessId;
        this.processName = processName;
        this.bpmnScheme = bpmnScheme.data();
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

    public String getBpmnScheme() {
        return bpmnScheme;
    }

    public void setBpmnScheme(String bpmnScheme) {
        this.bpmnScheme = bpmnScheme;
    }
}
