package api.service;

import java.util.List;
import java.util.UUID;

public interface BaseService<T> {
    T create(T entity);
    T getById(UUID id);
    List<T> getAll();
    void delete(UUID id);
    void update(T entity);
}
