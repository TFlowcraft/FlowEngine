package persistence.mapper;

import persistence.entity.ProcessInstanceHistory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ProcessInstanceHistoryMapper {
    public static ProcessInstanceHistory fromResultSet(ResultSet rs) throws SQLException {
        ProcessInstanceHistory history = new ProcessInstanceHistory();
        history.setId(UUID.fromString(rs.getString("id")));
        history.setProcessInstanceId(UUID.fromString(rs.getString("process_instance_id")));
        history.setTaskId(UUID.fromString(rs.getString("task_id")));
        history.setTimestamp(rs.getTimestamp("timestamp"));
        return history;
    }
}

