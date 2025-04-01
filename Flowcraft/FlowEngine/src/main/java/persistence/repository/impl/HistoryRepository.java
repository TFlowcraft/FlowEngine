package persistence.repository.impl;

import static com.database.entity.generated.tables.InstanceHistory.INSTANCE_HISTORY;

import com.database.entity.generated.tables.pojos.InstanceHistory;
import com.database.entity.generated.tables.records.InstanceHistoryRecord;
import org.jooq.DSLContext;
import persistence.DatabaseConfig;
import persistence.repository.BaseRepository;

import java.util.List;
import java.util.UUID;

public class HistoryRepository implements BaseRepository<InstanceHistory> {
    private final DSLContext context;

    public HistoryRepository() {
        this.context = DatabaseConfig.getContext();
    }

    @Override
    public void create(InstanceHistory record) {
        //record.store();
    }


    @Override
    public InstanceHistory getById(String processName, UUID id) {
        return context.selectFrom(INSTANCE_HISTORY)
                .where(INSTANCE_HISTORY.ID.eq(id))
                .fetchOneInto(InstanceHistory.class);
    }

    @Override
    public List<InstanceHistory> getAll(String processName) {
        return context.selectFrom(INSTANCE_HISTORY).fetchInto(InstanceHistory.class);
    }

    @Override
    public void delete(UUID id) {
        context
                .deleteFrom(INSTANCE_HISTORY)
                .where(INSTANCE_HISTORY.ID.eq(id))
                .execute();
    }

    @Override
    public void update(InstanceHistory record) {
        //record.update();
    }
}
