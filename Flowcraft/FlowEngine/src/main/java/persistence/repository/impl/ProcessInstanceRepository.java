package persistence.repository.impl;

import com.database.entity.generated.tables.ProcessInstance;
import com.database.entity.generated.tables.records.ProcessInstanceRecord;
import org.jooq.DSLContext;
import persistence.repository.BaseRepository;

import java.util.List;
import java.util.UUID;

public class ProcessInstanceRepository implements BaseRepository<ProcessInstanceRecord> {
    private final DSLContext context;

    public ProcessInstanceRepository(DSLContext context) {
        this.context = context;
    }

    @Override
    public void create(ProcessInstanceRecord record) {
        record.store();
    }

    @Override
    public ProcessInstanceRecord getById(UUID id) {
        return context
                .selectFrom(ProcessInstance.PROCESS_INSTANCE)
                .where(ProcessInstance.PROCESS_INSTANCE.ID.eq(id))
                .fetchOne();
    }

    @Override
    public List<ProcessInstanceRecord> getAll() {
        return context.selectFrom(ProcessInstance.PROCESS_INSTANCE).fetch();
    }

    @Override
    public void delete(UUID id) {
        context
                .deleteFrom(ProcessInstance.PROCESS_INSTANCE)
                .where(ProcessInstance.PROCESS_INSTANCE.ID.eq(id))
                .execute();
    }

    @Override
    public void update(ProcessInstanceRecord record) {
        record.update();
    }
}
