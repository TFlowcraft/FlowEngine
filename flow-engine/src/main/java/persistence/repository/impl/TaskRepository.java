package persistence.repository.impl;

import static com.database.entity.generated.tables.InstanceTasks.INSTANCE_TASKS;

import com.database.entity.generated.tables.ProcessInfo;
import com.database.entity.generated.tables.ProcessInstance;
import com.database.entity.generated.tables.pojos.InstanceTasks;
import com.database.entity.generated.tables.records.InstanceTasksRecord;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.impl.DSL;
import persistence.DatabaseConfig;

import java.sql.Connection;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TaskRepository  {
    private final DSLContext context;

    public TaskRepository() {
        this.context = DatabaseConfig.getContext();
    }

    public InstanceTasks getById(String name, UUID id) {
        return context
                .selectFrom(INSTANCE_TASKS)
                .where(INSTANCE_TASKS.ID.eq(id))
                .fetchOneInto(InstanceTasks.class);
    }

    public static InstanceTasks createRetryTask(InstanceTasks task, OffsetDateTime startedAt) {
        return new InstanceTasks(
                task.getId(),
                task.getInstanceId(),
                task.getBpmnElementId(),
                "PENDING",
                startedAt,
                null,
                task.getCurrentRetriesAmount() + 1
        );
    }

    public static InstanceTasks createFailedTask(InstanceTasks task, OffsetDateTime startedAt) {
        return new InstanceTasks(
                task.getId(),
                task.getInstanceId(),
                task.getBpmnElementId(),
                "FAILED",
                startedAt,
                OffsetDateTime.now(),
                task.getCurrentRetriesAmount()
        );
    }

    public List<InstanceTasks> getAll(String name) {
        return context.selectFrom(INSTANCE_TASKS).fetchInto(InstanceTasks.class);
    }

    public List<InstanceTasks> getAll(String processName, UUID instanceId) {
        return context
                .select(INSTANCE_TASKS.fields())
                .from(INSTANCE_TASKS)
                .join(ProcessInstance.PROCESS_INSTANCE).on(ProcessInstance.PROCESS_INSTANCE.ID.eq(INSTANCE_TASKS.INSTANCE_ID))
                .join(ProcessInfo.PROCESS_INFO).on(ProcessInstance.PROCESS_INSTANCE.PROCESS_ID.eq(ProcessInfo.PROCESS_INFO.ID))
                .fetchInto(InstanceTasks.class);
    }

    public List<String> getTasksStatusByInstanceId(UUID instanceId, List<String> elementsId) {
       if (elementsId == null || elementsId.isEmpty()) {
           return Collections.emptyList();
       }
       return context.selectFrom(INSTANCE_TASKS)
               .where(INSTANCE_TASKS.INSTANCE_ID.eq(instanceId))
               .and(INSTANCE_TASKS.BPMN_ELEMENT_ID.in(elementsId))
               .fetchInto(InstanceTasks.class)
               .stream()
               .filter(Objects::nonNull)
               .map(InstanceTasks::getStatus)
               .toList();
    }



    public List<com.database.entity.generated.tables.pojos.InstanceTasks> fetchAndLockTasks(int batchSize) {
        return context
                .update(INSTANCE_TASKS)
                .set(INSTANCE_TASKS.STATUS, "RUNNING")
                .where(INSTANCE_TASKS.ID.in(
                        context
                                .select(INSTANCE_TASKS.ID)
                                .from(INSTANCE_TASKS)
                                .where(INSTANCE_TASKS.STATUS.eq("PENDING"))
                                .orderBy(INSTANCE_TASKS.START_TIME.asc())
                                .limit(batchSize)
                                .forUpdate().skipLocked()
                ))
                .returning()
                .fetchInto(com.database.entity.generated.tables.pojos.InstanceTasks.class);
    }

//    public void createTaskForInstance(UUID id, String elementId) {
//        InstanceTasksRecord record = context.newRecord(INSTANCE_TASKS);
//        record.setInstanceId(id);
//        record.setBpmnElementId(elementId);
//        record.setStatus("PENDING");
//        record.setCurrentRetriesAmount(0);
//        record.store();
//    }

    public void createTaskForInstance(UUID id, String elementId) {
        boolean exists = context.fetchExists(
                context.selectFrom(INSTANCE_TASKS)
                        .where(INSTANCE_TASKS.INSTANCE_ID.eq(id))
                        .and(INSTANCE_TASKS.BPMN_ELEMENT_ID.eq(elementId))
        );

        if (!exists) {
            InstanceTasksRecord record = context.newRecord(INSTANCE_TASKS);
            record.setInstanceId(id);
            record.setBpmnElementId(elementId);
            record.setStatus("PENDING");
            record.setCurrentRetriesAmount(0);
            record.store();
        } else {
            System.out.println("Task already exists for instance " + id + " and element " + elementId);
        }
    }

    public void createTaskForInstance(UUID id, String elementId, Connection connection) {
        DSLContext dsl = DSL.using(connection, SQLDialect.POSTGRES);
        boolean exists = dsl.fetchExists(
                context.selectFrom(INSTANCE_TASKS)
                        .where(INSTANCE_TASKS.INSTANCE_ID.eq(id))
                        .and(INSTANCE_TASKS.BPMN_ELEMENT_ID.eq(elementId))
        );

        if (!exists) {
            InstanceTasksRecord record = dsl.newRecord(INSTANCE_TASKS);
            record.setInstanceId(id);
            record.setBpmnElementId(elementId);
            record.setStatus("PENDING");
            record.setCurrentRetriesAmount(0);
            record.store();
        } else {
            System.out.println("Task already exists for instance " + id + " and element " + elementId);
        }
    }


    public void updateTask(com.database.entity.generated.tables.pojos.InstanceTasks newTask) {
        context.update(INSTANCE_TASKS)
                .set(INSTANCE_TASKS.STATUS, newTask.getStatus())
                .set(INSTANCE_TASKS.START_TIME, newTask.getStartTime())
                .set(INSTANCE_TASKS.END_TIME, newTask.getEndTime())
                .set(INSTANCE_TASKS.CURRENT_RETRIES_AMOUNT, newTask.getCurrentRetriesAmount())
                .where(INSTANCE_TASKS.ID.eq(newTask.getId()))
                .execute();

    }

    public void updateTask(Connection connection, com.database.entity.generated.tables.pojos.InstanceTasks task) {
        DSLContext dsl = DSL.using(connection, SQLDialect.POSTGRES);
        updateTask(dsl, task.getId(), task.getStatus(), task.getStartTime(), task.getEndTime(), task.getCurrentRetriesAmount());
    }

    public void updateTask(UUID id, String status, OffsetDateTime startTime, OffsetDateTime endTime, Integer currentRetriesAmount) {
        updateTask(context, id, status, startTime, endTime, currentRetriesAmount);
    }

    public void updateTask(Connection connection, UUID id, String status, OffsetDateTime startTime, OffsetDateTime endTime, Integer currentRetriesAmount) {
        DSLContext dsl = DSL.using(connection, SQLDialect.POSTGRES);
        updateTask(dsl, id,  status, startTime, endTime, currentRetriesAmount);
    }

    private void updateTask(DSLContext dsl, UUID id, String status, OffsetDateTime startTime, OffsetDateTime endTime, Integer currentRetriesAmount) {
        UpdateSetFirstStep<InstanceTasksRecord> updateStep = dsl.update(INSTANCE_TASKS);
        UpdateSetMoreStep<InstanceTasksRecord> update = null;

        if (status != null) {
            update = updateStep.set(INSTANCE_TASKS.STATUS, status);
        }
        if (startTime != null) {
            update = (update == null ? updateStep.set(INSTANCE_TASKS.START_TIME, startTime)
                    : update.set(INSTANCE_TASKS.START_TIME, startTime));
        }
        if (endTime != null) {
            update = (update == null ? updateStep.set(INSTANCE_TASKS.END_TIME, endTime)
                    : update.set(INSTANCE_TASKS.END_TIME, endTime));
        }
        if (currentRetriesAmount != null) {
            update = (update == null ? updateStep.set(INSTANCE_TASKS.CURRENT_RETRIES_AMOUNT, currentRetriesAmount)
                    : update.set(INSTANCE_TASKS.CURRENT_RETRIES_AMOUNT, currentRetriesAmount));
        }

        if (update != null) {
            update.where(INSTANCE_TASKS.ID.eq(id)).execute();
        }
    }
}
