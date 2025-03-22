package persistence.repository.impl;

import com.database.entity.generated.tables.InstanceTasks;
import com.database.entity.generated.tables.records.InstanceTasksRecord;
import org.jooq.DSLContext;
import persistence.repository.BaseRepository;

import java.util.List;
import java.util.UUID;

public class TaskRepository implements BaseRepository<InstanceTasksRecord> {
    private final DSLContext context;

    public TaskRepository(DSLContext context) {
        this.context = context;
    }

    @Override
    public void create(InstanceTasksRecord record) {
        record.store();
    }

    @Override
    public InstanceTasksRecord getById(UUID id) {
        return context
                .selectFrom(InstanceTasks.INSTANCE_TASKS)
                .where(InstanceTasks.INSTANCE_TASKS.ID.eq(id))
                .fetchOne();
    }

    @Override
    public List<InstanceTasksRecord> getAll() {
        return context.selectFrom(InstanceTasks.INSTANCE_TASKS).fetch();
    }

    @Override
    public void delete(UUID id) {
        context
                .deleteFrom(InstanceTasks.INSTANCE_TASKS)
                .where(InstanceTasks.INSTANCE_TASKS.ID.eq(id))
                .execute();
    }

    @Override
    public void update(InstanceTasksRecord record) {
        record.update();
    }
}
