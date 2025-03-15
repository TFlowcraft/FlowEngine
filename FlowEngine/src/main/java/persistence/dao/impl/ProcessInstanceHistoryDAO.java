package persistence.dao.impl;

import persistence.dao.BaseDAO;
import persistence.dao.DatabaseConnection;
import persistence.entity.ProcessInstanceHistory;
import persistence.mapper.ProcessInstanceHistoryMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProcessInstanceHistoryDAO implements BaseDAO<ProcessInstanceHistory> {
    private static final String INSERT_PROCESS_INSTANCE_HISTORY =
            "INSERT INTO process_instance_history (id, process_instance_id, task_id, start_time, end_time) VALUES (?, ?, ?, ?, ?)";
    private static final String FIND_PROCESS_INSTANCE_HISTORY_BY_ID =
            "SELECT * FROM process_instance_history WHERE id = ?";
    private static final String FIND_ALL_PROCESS_INSTANCE_HISTORY =
            "SELECT * FROM process_instance_history";
    private static final String UPDATE_PROCESS_INSTANCE_HISTORY =
            "UPDATE process_instance_history SET process_instance_id = ?, task_id = ?, start_time = ?, end_time = ? WHERE id = ?";
    private static final String DELETE_PROCESS_INSTANCE_HISTORY =
            "DELETE FROM process_instance_history WHERE id = ?";

    @Override
    public void save(ProcessInstanceHistory history) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_PROCESS_INSTANCE_HISTORY)) {

            stmt.setObject(1, history.getId());
            stmt.setObject(2, history.getProcessInstanceId());
            stmt.setObject(3, history.getTaskId());
            stmt.setTimestamp(4, Timestamp.valueOf(history.getTimestamp()));
            stmt.executeUpdate();
        }
    }

    @Override
    public ProcessInstanceHistory findById(UUID id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_PROCESS_INSTANCE_HISTORY_BY_ID)) {

            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return ProcessInstanceHistoryMapper.fromResultSet(rs);
            }
        }
        return null;
    }

    @Override
    public List<ProcessInstanceHistory> findAll() throws SQLException {
        List<ProcessInstanceHistory> historyList = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_PROCESS_INSTANCE_HISTORY);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                historyList.add(ProcessInstanceHistoryMapper.fromResultSet(rs));
            }
        }
        return historyList;
    }

    @Override
    public void update(ProcessInstanceHistory history) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_PROCESS_INSTANCE_HISTORY)) {

            stmt.setObject(1, history.getProcessInstanceId());
            stmt.setObject(2, history.getTaskId());
            stmt.setObject(3, history.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(UUID id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_PROCESS_INSTANCE_HISTORY)) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        }
    }
}

