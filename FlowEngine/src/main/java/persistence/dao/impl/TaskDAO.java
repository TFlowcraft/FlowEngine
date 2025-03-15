package persistence.dao.impl;

import persistence.dao.BaseDAO;
import persistence.dao.DatabaseConnection;
import persistence.entity.Task;
import persistence.mapper.TaskMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskDAO implements BaseDAO<Task> {
    private static final String INSERT_TASK =
            "INSERT INTO task (id, name, start_time, end_time) VALUES (?, ?, ?, ?)";
    private static final String FIND_TASK_BY_ID =
            "SELECT * FROM task WHERE id = ?";
    private static final String FIND_ALL_TASKS =
            "SELECT * FROM task";
    private static final String UPDATE_TASK =
            "UPDATE task SET name = ?, start_time = ?, end_time = ? WHERE id = ?";
    private static final String DELETE_TASK =
            "DELETE FROM task WHERE id = ?";

    @Override
    public void save(Task task) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_TASK)) {

            stmt.setObject(1, task.getId());
            stmt.setString(2, task.getName());
            stmt.setTimestamp(3, Timestamp.valueOf(task.getStartTime()));
            stmt.setTimestamp(4, task.getEndTime() != null ? Timestamp.valueOf(task.getEndTime()) : null);
            stmt.executeUpdate();
        }
    }

    @Override
    public Task findById(UUID id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_TASK_BY_ID)) {

            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return TaskMapper.fromResultSet(rs);
            }
        }
        return null;
    }

    @Override
    public List<Task> findAll() throws SQLException {
        List<Task> tasks = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_ALL_TASKS);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tasks.add(TaskMapper.fromResultSet(rs));
            }
        }
        return tasks;
    }

    @Override
    public void update(Task task) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_TASK)) {

            stmt.setString(1, task.getName());
            stmt.setTimestamp(2, Timestamp.valueOf(task.getStartTime()));
            stmt.setTimestamp(3, task.getEndTime() != null ? Timestamp.valueOf(task.getEndTime()) : null);
            stmt.setObject(4, task.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(UUID id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_TASK)) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        }
    }
}

