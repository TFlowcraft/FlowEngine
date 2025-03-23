package persistence.repository.impl;

import com.database.entity.generated.tables.InstanceHistory;
import com.database.entity.generated.tables.records.InstanceHistoryRecord;
import org.jooq.DSLContext;
import persistence.DatabaseConfig;
import persistence.repository.BaseRepository;

import java.util.List;
import java.util.UUID;

public class HistoryRepository implements BaseRepository<InstanceHistoryRecord> {
    private final DSLContext context;

    public HistoryRepository() {
        this.context = DatabaseConfig.getContext();
    }

    @Override
    public void create(InstanceHistoryRecord record) {
        record.store();
    }

    @Override
    public InstanceHistoryRecord getById(UUID id) {
        return context.selectFrom(InstanceHistory.INSTANCE_HISTORY)
                .where(InstanceHistory.INSTANCE_HISTORY.ID.eq(id))
                .fetchOne();
    }

    @Override
    public List<InstanceHistoryRecord> getAll() {
        return context.selectFrom(InstanceHistory.INSTANCE_HISTORY).fetch();
    }

    @Override
    public void delete(UUID id) {
        context
                .deleteFrom(InstanceHistory.INSTANCE_HISTORY)
                .where(InstanceHistory.INSTANCE_HISTORY.ID.eq(id))
                .execute();
    }

    @Override
    public void update(InstanceHistoryRecord record) {
        record.update();
    }
}
