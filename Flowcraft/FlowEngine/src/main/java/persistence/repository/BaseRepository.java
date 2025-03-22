package persistence.repository;

import java.util.List;
import java.util.UUID;

public interface BaseRepository<T> {
    void create(T record);
    T getById(UUID id);
    List<T> getAll();
    void delete(UUID id);
    void update(T record);
}
