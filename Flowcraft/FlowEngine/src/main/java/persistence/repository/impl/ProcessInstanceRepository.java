package persistence.repository.impl;


import static com.database.entity.generated.tables.ProcessInstance.PROCESS_INSTANCE;

import com.database.entity.generated.tables.ProcessInfo;
import com.database.entity.generated.tables.pojos.ProcessInstance;
import com.database.entity.generated.tables.records.ProcessInstanceRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.QOM;
import persistence.DatabaseConfig;
import persistence.repository.BaseRepository;

import java.sql.Connection;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class ProcessInstanceRepository implements BaseRepository<ProcessInstance> {
    private final DSLContext context;

    public ProcessInstanceRepository() {
        this.context = DatabaseConfig.getContext();
    }

    @Override
    public void create(ProcessInstance record) {
       // record.store();
    }

    @Override
    public ProcessInstance getById(String name, UUID instanceId) {
    return context
        .select(PROCESS_INSTANCE.fields())
        .from(PROCESS_INSTANCE)
        .join(ProcessInfo.PROCESS_INFO)
        .on(PROCESS_INSTANCE.PROCESS_ID.eq(ProcessInfo.PROCESS_INFO.ID))
        .where(PROCESS_INSTANCE.ID.eq(instanceId))
        .fetchOneInto(ProcessInstance.class);
    }

    @Override
    public List<ProcessInstance> getAll(String name) {
        return context
                .select(PROCESS_INSTANCE.fields())
                .from(PROCESS_INSTANCE)
                .join(ProcessInfo.PROCESS_INFO)
                .on(PROCESS_INSTANCE.PROCESS_ID.eq(ProcessInfo.PROCESS_INFO.ID))
                .where(PROCESS_INSTANCE.ID.eq(instanceId))
                .fetchInto(ProcessInstance.class);
    }

    @Override
    public void delete(UUID id) {
        context
                .deleteFrom(PROCESS_INSTANCE)
                .where(PROCESS_INSTANCE.ID.eq(id))
                .execute();
    }


    @Override
    public void update(ProcessInstance record) {
        //record.update();
    }


    public UUID createNew(JSONB businessData) {
        ProcessInstanceRecord record = context.newRecord(PROCESS_INSTANCE);
        record.setBusinessData(businessData);
        record.setStartedAt(OffsetDateTime.now());
        record.store();
        return record.getId();
    }

    public void updateInstance(UUID id, JSONB businessData, OffsetDateTime startedAt, OffsetDateTime endedAt) {
        updateInstance(context, id, businessData, startedAt, endedAt);
    }

    public void updateInstance(Connection connection, UUID id, JSONB businessData, OffsetDateTime startedAt, OffsetDateTime endedAt) {
        DSLContext dsl = DSL.using(connection, SQLDialect.POSTGRES);
        updateInstance(dsl, id, businessData, startedAt, endedAt);
    }

    private void updateInstance(DSLContext dsl, UUID id, JSONB businessData, OffsetDateTime startedAt, OffsetDateTime endedAt) {
        UpdateQuery<ProcessInstanceRecord> query = dsl.updateQuery(PROCESS_INSTANCE);

        if (businessData != null) {
            query.addValue(PROCESS_INSTANCE.BUSINESS_DATA, businessData);
        }
        if (startedAt != null) {
            query.addValue(PROCESS_INSTANCE.STARTED_AT, startedAt);
        }
        if (endedAt != null) {
            query.addValue(PROCESS_INSTANCE.COMPLETED_AT, endedAt);
        }

        query.addConditions(PROCESS_INSTANCE.ID.eq(id));
        query.execute();
    }

    public JSONB getBusinessData(UUID instanceId) {
        return  context
                .select(PROCESS_INSTANCE.BUSINESS_DATA)
                .from(PROCESS_INSTANCE)
                .where(PROCESS_INSTANCE.ID.eq(instanceId))
                .fetchOne(PROCESS_INSTANCE.BUSINESS_DATA);
    }
}
