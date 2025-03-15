package persistence.mapper;

import persistence.entity.Task;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TaskMapper {
    public static Task fromResultSet(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(UUID.fromString(rs.getString("id")));
        task.setName(rs.getString("name"));
        task.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        task.setEndTime(rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toLocalDateTime() : null);
        return task;
    }
}
