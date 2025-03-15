package persistence.mapper;

import persistence.entity.ProcessInstanceTask;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ProcessInstanceTaskMapper {
    public static ProcessInstanceTask fromResultSet(ResultSet rs) throws SQLException {
        ProcessInstanceTask task = new ProcessInstanceTask();
        task.setId(UUID.fromString(rs.getString("id")));
        task.setProcessInstanceId(UUID.fromString(rs.getString("process_instance_id")));
        task.setTaskId(UUID.fromString(rs.getString("task_id")));
        task.setStatus(rs.getString("status"));
        task.setStartTime(rs.getTimestamp("start_time") != null ? rs.getTimestamp("start_time").toLocalDateTime() : null);
        task.setEndTime(rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toLocalDateTime() : null);
        task.setParentTaskId(rs.getString("parent_id") != null ? UUID.fromString(rs.getString("gateway_id")) : null);
        return task;
    }
}

