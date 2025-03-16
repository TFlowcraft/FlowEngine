package persistence.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface BaseDAO<T> {
    void save(T entity) throws SQLException;

    T findById(UUID id) throws SQLException;

    List<T> findAll() throws SQLException;

    void update(T entity) throws SQLException;

    void delete(UUID id) throws SQLException;

}
