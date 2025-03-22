/*
 * This file is generated by jOOQ.
 */
package com.database.entity.generated.tables.pojos;


import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.jooq.JSONB;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class InstanceHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final UUID instanceId;
    private final UUID taskId;
    private final String taskStatus;
    private final JSONB errorStacktrace;
    private final OffsetDateTime timestamp;

    public InstanceHistory(InstanceHistory value) {
        this.id = value.id;
        this.instanceId = value.instanceId;
        this.taskId = value.taskId;
        this.taskStatus = value.taskStatus;
        this.errorStacktrace = value.errorStacktrace;
        this.timestamp = value.timestamp;
    }

    public InstanceHistory(
        UUID id,
        UUID instanceId,
        UUID taskId,
        String taskStatus,
        JSONB errorStacktrace,
        OffsetDateTime timestamp
    ) {
        this.id = id;
        this.instanceId = instanceId;
        this.taskId = taskId;
        this.taskStatus = taskStatus;
        this.errorStacktrace = errorStacktrace;
        this.timestamp = timestamp;
    }

    /**
     * Getter for <code>public.instance_history.id</code>.
     */
    public UUID getId() {
        return this.id;
    }

    /**
     * Getter for <code>public.instance_history.instance_id</code>.
     */
    public UUID getInstanceId() {
        return this.instanceId;
    }

    /**
     * Getter for <code>public.instance_history.task_id</code>.
     */
    public UUID getTaskId() {
        return this.taskId;
    }

    /**
     * Getter for <code>public.instance_history.task_status</code>.
     */
    public String getTaskStatus() {
        return this.taskStatus;
    }

    /**
     * Getter for <code>public.instance_history.error_stacktrace</code>.
     */
    public JSONB getErrorStacktrace() {
        return this.errorStacktrace;
    }

    /**
     * Getter for <code>public.instance_history.timestamp</code>.
     */
    public OffsetDateTime getTimestamp() {
        return this.timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final InstanceHistory other = (InstanceHistory) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.instanceId == null) {
            if (other.instanceId != null)
                return false;
        }
        else if (!this.instanceId.equals(other.instanceId))
            return false;
        if (this.taskId == null) {
            if (other.taskId != null)
                return false;
        }
        else if (!this.taskId.equals(other.taskId))
            return false;
        if (this.taskStatus == null) {
            if (other.taskStatus != null)
                return false;
        }
        else if (!this.taskStatus.equals(other.taskStatus))
            return false;
        if (this.errorStacktrace == null) {
            if (other.errorStacktrace != null)
                return false;
        }
        else if (!this.errorStacktrace.equals(other.errorStacktrace))
            return false;
        if (this.timestamp == null) {
            if (other.timestamp != null)
                return false;
        }
        else if (!this.timestamp.equals(other.timestamp))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.instanceId == null) ? 0 : this.instanceId.hashCode());
        result = prime * result + ((this.taskId == null) ? 0 : this.taskId.hashCode());
        result = prime * result + ((this.taskStatus == null) ? 0 : this.taskStatus.hashCode());
        result = prime * result + ((this.errorStacktrace == null) ? 0 : this.errorStacktrace.hashCode());
        result = prime * result + ((this.timestamp == null) ? 0 : this.timestamp.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("InstanceHistory (");

        sb.append(id);
        sb.append(", ").append(instanceId);
        sb.append(", ").append(taskId);
        sb.append(", ").append(taskStatus);
        sb.append(", ").append(errorStacktrace);
        sb.append(", ").append(timestamp);

        sb.append(")");
        return sb.toString();
    }
}
