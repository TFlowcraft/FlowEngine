package persistence.repository.impl;

import static com.database.entity.generated.tables.InstanceHistory.INSTANCE_HISTORY;

import com.database.entity.generated.tables.ProcessInfo;
import com.database.entity.generated.tables.pojos.InstanceHistory;
import static com.database.entity.generated.tables.InstanceHistory.INSTANCE_HISTORY;
import com.database.entity.generated.tables.records.InstanceHistoryRecord;
import org.jooq.DSLContext;
import persistence.DatabaseConfig;

import java.util.List;
import java.util.UUID;

public class HistoryRepository {
    private final DSLContext context;

    public HistoryRepository() {
        this.context = DatabaseConfig.getContext();
    }

    public void create(InstanceHistory record) {
        //record.store();
    }

    public InstanceHistory getById(String processName, UUID instanceId, UUID taskId) {
        return context
                .selectFrom(INSTANCE_HISTORY)
                .where(INSTANCE_HISTORY.ID.eq(taskId))
                .and(INSTANCE_HISTORY.INSTANCE_ID.eq(instanceId))
                .fetchOneInto(InstanceHistory.class);
    }


    public List<InstanceHistory> getAll(String processName, UUID instanceId) {
        return context
                .selectFrom(INSTANCE_HISTORY)
                .where(INSTANCE_HISTORY.INSTANCE_ID.eq(instanceId))
                .fetchInto(InstanceHistory.class);
    }

    public void delete(UUID id) {
        context
                .deleteFrom(INSTANCE_HISTORY)
                .where(INSTANCE_HISTORY.ID.eq(id))
                .execute();
    }

    public void update(InstanceHistory record) {
        //record.update();
    }
}
