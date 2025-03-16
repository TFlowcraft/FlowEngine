package persistence.mapper;

import persistence.entity.ProcessInstance;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ProcessInstanceMapper {
    public static ProcessInstance fromResultSet(ResultSet rs) throws SQLException {
        ProcessInstance instance = new ProcessInstance();
        instance.setId(UUID.fromString(rs.getString("id")));
        instance.setBusinessKey(rs.getString("businessKey"));
        instance.setStartTime(rs.getTimestamp("start_time"));
        instance.setEndTime(rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time") : null);
        return instance;
    }
}
