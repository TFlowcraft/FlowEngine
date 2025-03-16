package persistence.dao.impl;

import persistence.dao.BaseDAO;
import persistence.dao.DatabaseConnection;
import persistence.entity.ProcessInstanceTask;
import persistence.mapper.ProcessInstanceTaskMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProcessInstanceTaskDAO implements BaseDAO<ProcessInstanceTask> {
    private static final String INSERT_PROCESS_INSTANCE_TASK =
            "INSERT INTO process_instance_task (id, process_instance_id, task_id) VALUES (?, ?, ?)";
    private static final String FIND_PROCESS_INSTANCE_TASK_BY_ID =
            "SELECT * FROM process_instance_task WHERE id = ?";
    private static final String FIND_ALL_PROCESS_INSTANCE_TASKS =
            "SELECT * FROM process_instance_task";
    private static final String UPDATE_PROCESS_INSTANCE_TASK =
            "UPDATE process_instance_task SET process_instance_id = ?, task_id = ? WHERE id = ?";
    private static final String DELETE_PROCESS_INSTANCE_TASK =
            "DELETE FROM process_instance_task WHERE id = ?";

    @Override
    public void save(ProcessInstanceTask task) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_PROCESS_INSTANCE_TASK)) {

            stmt.setObject(1, task.getId());
            stmt.setObject(2, task.getProcessInstanceId());
            stmt.setObject(3, task.getTaskId());
            stmt.executeUpdate();
        }
    }

    @Override
    public ProcessInstanceTask findById(UUID id) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_PROCESS_INSTANCE_TASK_BY_ID)) {

            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return ProcessInstanceTaskMapper.fromResultSet(rs);
            }
        }
        return null;
    }

    @Override
    public List<ProcessInstanceTask> findAll() throws SQLException {
        List<ProcessInstanceTask> tasks = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_PROCESS_INSTANCE_TASKS);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tasks.add(ProcessInstanceTaskMapper.fromResultSet(rs));
            }
        }
        return tasks;
    }

    @Override
    public void update(ProcessInstanceTask task) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_PROCESS_INSTANCE_TASK)) {

            stmt.setObject(1, task.getProcessInstanceId());
            stmt.setObject(2, task.getTaskId());
            stmt.setObject(3, task.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(UUID id) throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_PROCESS_INSTANCE_TASK)) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        }
    }
}

