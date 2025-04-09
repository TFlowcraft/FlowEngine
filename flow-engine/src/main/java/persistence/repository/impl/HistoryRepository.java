package persistence.repository.impl;

import static com.database.entity.generated.tables.InstanceHistory.INSTANCE_HISTORY;
import com.database.entity.generated.tables.pojos.InstanceHistory;
import static com.database.entity.generated.tables.ProcessInfo.PROCESS_INFO;
import static com.database.entity.generated.tables.ProcessInstance.PROCESS_INSTANCE;
import org.jooq.DSLContext;
import java.util.List;
import java.util.UUID;

public class HistoryRepository {
    private final DSLContext context;

    public HistoryRepository(DSLContext context) {
        this.context = context;
    }

    public List<InstanceHistory> getById(String processName, UUID instanceId, UUID taskId) {
        return context
                .select(INSTANCE_HISTORY.fields())
                .from(INSTANCE_HISTORY)
                .join(PROCESS_INSTANCE).on(INSTANCE_HISTORY.INSTANCE_ID.eq(PROCESS_INSTANCE.ID))
                .join(PROCESS_INFO).on(PROCESS_INSTANCE.PROCESS_ID.eq(PROCESS_INFO.ID))
                .where(PROCESS_INFO.PROCESS_NAME.eq(processName))
                .and(PROCESS_INSTANCE.ID.eq(instanceId))
                .and(INSTANCE_HISTORY.TASK_ID.eq(taskId))
                .fetchInto(InstanceHistory.class);
    }

    public List<InstanceHistory> getAll(String processName, UUID instanceId) {
        return context
                .select(INSTANCE_HISTORY.fields())
                .from(INSTANCE_HISTORY)
                .join(PROCESS_INSTANCE).on(INSTANCE_HISTORY.INSTANCE_ID.eq(PROCESS_INSTANCE.ID))
                .join(PROCESS_INFO).on(PROCESS_INSTANCE.PROCESS_ID.eq(PROCESS_INFO.ID))
                .where(PROCESS_INFO.PROCESS_NAME.eq(processName))
                .and(PROCESS_INSTANCE.ID.eq(instanceId))
                .fetchInto(InstanceHistory.class);
    }

}
