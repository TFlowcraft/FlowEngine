package persistence.dao.impl;

import persistence.dao.BaseDAO;
import persistence.dao.DatabaseConnection;
import persistence.entity.ProcessInstance;
import persistence.mapper.ProcessInstanceMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProcessInstanceDAO implements BaseDAO<ProcessInstance> {
    private static final String INSERT_PROCESS_INSTANCE = "INSERT INTO process_instance (id, start_time, end_time, business_key) VALUES (?, ?, ?, ?)";
    private static final String FIND_PROCESS_INSTANCE_BY_ID = "SELECT * FROM process_instance WHERE id = ?";
    private static final String FIND_ALL_PROCESS_INSTANCE = "SELECT * FROM process_instance";
    private static final String UPDATE_PROCESS_INSTANCE = "UPDATE process_instance SET start_time = ?, end_time = ?, business_key = ? WHERE id = ?";
    private static final String DELETE_PROCESS_INSTANCE = "DELETE FROM process_instance WHERE id = ?";

    @Override
    public void save(ProcessInstance entity) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_PROCESS_INSTANCE)) {
            statement.setObject(1, entity.getId());
            statement.setTimestamp(2, entity.getStartTime());
            statement.setTimestamp(3, entity.getEndTime());
            statement.setString(4, entity.getBusinessKey());
            statement.executeUpdate();
        }
    }

    @Override
    public ProcessInstance findById(UUID id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_PROCESS_INSTANCE_BY_ID)) {
            statement.setObject(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return ProcessInstanceMapper.fromResultSet(resultSet);
            }
        }
        return null;
    }

    @Override
    public List<ProcessInstance> findAll() throws SQLException {
        List<ProcessInstance> instances = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(FIND_ALL_PROCESS_INSTANCE)) {

            while (resultSet.next()) {
                instances.add(ProcessInstanceMapper.fromResultSet(resultSet));
            }
        }
        return instances;
    }

    @Override
    public void update(ProcessInstance entity) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_PROCESS_INSTANCE)) {
            statement.setTimestamp(1, entity.getStartTime());
            statement.setTimestamp(2, entity.getEndTime());
            statement.setString(3, entity.getBusinessKey());
            statement.setObject(4, entity.getId());
            statement.executeUpdate();
        }
    }

    @Override
    public void delete(UUID id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_PROCESS_INSTANCE)) {
            statement.setObject(1, id);
            statement.executeUpdate();
        }
    }
}
