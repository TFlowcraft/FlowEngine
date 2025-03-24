package persistence.repository.impl;

import com.database.entity.generated.tables.InstanceTasks;

import com.database.entity.generated.tables.records.InstanceTasksRecord;
import org.jooq.DSLContext;
import persistence.DatabaseConfig;
import persistence.repository.BaseRepository;
import static com.database.entity.generated.tables.InstanceTasks.INSTANCE_TASKS;

import java.util.List;
import java.util.UUID;

public class TaskRepository implements BaseRepository<InstanceTasksRecord> {
    private final DSLContext context;

    public TaskRepository() {
        this.context = DatabaseConfig.getContext();
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

    public List<com.database.entity.generated.tables.pojos.InstanceTasks> fetchAndLockTasks(int batchSize) {
        return context
                .update(INSTANCE_TASKS)
                .set(INSTANCE_TASKS.STATUS, "IN_PROGRESS")
                .where(INSTANCE_TASKS.ID.in(
                        context
                                .select(InstanceTasks.INSTANCE_TASKS.ID)
                                .from(INSTANCE_TASKS)
                                .where(INSTANCE_TASKS.STATUS.eq("PENDING"))
                                .orderBy(INSTANCE_TASKS.START_TIME.asc())
                                .limit(batchSize)
                                .forUpdate().skipLocked()
                ))
                .returning()
                .fetchInto(com.database.entity.generated.tables.pojos.InstanceTasks.class);
    }
}
