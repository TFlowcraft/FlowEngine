package persistence.repository.impl;

import static com.database.entity.generated.tables.InstanceTasks.INSTANCE_TASKS;
import static org.jooq.impl.DSL.count;

import api.dto.TaskDto;
import com.database.entity.generated.tables.ProcessInfo;
import com.database.entity.generated.tables.ProcessInstance;
import com.database.entity.generated.tables.pojos.InstanceTasks;
import com.database.entity.generated.tables.records.InstanceTasksRecord;
import engine.common.Status;
import org.jooq.*;
import org.jooq.impl.DSL;
import java.sql.Connection;
import java.time.OffsetDateTime;
import java.util.*;


public class TaskRepository  {
    private final DSLContext context;

    public TaskRepository(DSLContext context) {
        this.context = context;
    }

    public InstanceTasks getById(String name, UUID id) {
        return context
                .selectFrom(INSTANCE_TASKS)
                .where(INSTANCE_TASKS.ID.eq(id))
                .fetchOneInto(InstanceTasks.class);
    }

    //TODO Тут надо в history stacktrace добавить, поглядеть как это сделать
    public static InstanceTasks createRetryTask(InstanceTasks task, Exception exception) {
        return new InstanceTasks(
                task.getId(),
                task.getInstanceId(),
                task.getBpmnElementId(),
                Status.PENDING,
                task.getStartTime(),
                task.getEndTime(),
                task.getCurrentRetriesAmount() + 1
        );
    }

    public static InstanceTasks createFailedTask(InstanceTasks task, OffsetDateTime startedAt, OffsetDateTime endedAt, Exception exception) {
        return new InstanceTasks(
                task.getId(),
                task.getInstanceId(),
                task.getBpmnElementId(),
                Status.FAILED,
                startedAt,
                endedAt,
                task.getCurrentRetriesAmount()
        );
    }

    public int getCompletedTasksForInstance(UUID instanceId, UUID taskId, List<String> elementsId) {
        return context
                .select(count())
                .from(INSTANCE_TASKS)
                .where(INSTANCE_TASKS.INSTANCE_ID.eq(instanceId))
                .and(INSTANCE_TASKS.BPMN_ELEMENT_ID.in(elementsId))
                .and(INSTANCE_TASKS.STATUS.eq(Status.COMPLETED))
                .fetchOptionalInto(Integer.class)
                .orElse(0);
    }

    public boolean areAllTasksCompleted(UUID instanceId, List<String> elementsId) {
        if (elementsId.isEmpty()) {
            throw new IllegalArgumentException("Elements list cannot be empty");
        }

        int totalElements = elementsId.size();
        Optional<Integer> completedCount = getTaskAmountForStatus(instanceId, elementsId, Status.COMPLETED);

//        Integer completedCount = context
//                .select(count())
//                .from(INSTANCE_TASKS)
//                .where(INSTANCE_TASKS.INSTANCE_ID.eq(instanceId))
//                .and(INSTANCE_TASKS.BPMN_ELEMENT_ID.in(elementsId))
//                .and(INSTANCE_TASKS.STATUS.eq(Status.COMPLETED))
//                .fetchOneInto(Integer.class);
        return completedCount.isPresent() && completedCount.get() == totalElements;
        //return completedCount != null && completedCount == totalElements;
    }

    public boolean hasFailedTasks(UUID instanceId, List<String> elementsId) {
        if (elementsId.isEmpty()) {
            throw new IllegalArgumentException("Elements list cannot be empty");
        }
        Optional<Integer> failedCount = getTaskAmountForStatus(instanceId, elementsId, Status.FAILED);
        return failedCount.isPresent() && failedCount.get() != 0;
    }

    private Optional<Integer> getTaskAmountForStatus(UUID instanceId, List<String> elementsId, Status status) {
        return context
                .select(count())
                .from(INSTANCE_TASKS)
                .where(INSTANCE_TASKS.INSTANCE_ID.eq(instanceId))
                .and(INSTANCE_TASKS.BPMN_ELEMENT_ID.in(elementsId))
                .and(INSTANCE_TASKS.STATUS.eq(status))
                .fetchOptionalInto(Integer.class);

    }


    public List<InstanceTasks> getAll(String name) {
        return context.selectFrom(INSTANCE_TASKS).fetchInto(InstanceTasks.class);
    }

//    public List<InstanceTasks> getAll(String processName, UUID instanceId) {
//        return context
//                .selectDistinct(INSTANCE_TASKS.fields())
//                .from(INSTANCE_TASKS)
//                .join(ProcessInstance.PROCESS_INSTANCE)
//                .on(ProcessInstance.PROCESS_INSTANCE.ID.eq(INSTANCE_TASKS.INSTANCE_ID))
//                .join(ProcessInfo.PROCESS_INFO)
//                .on(ProcessInstance.PROCESS_INSTANCE.PROCESS_ID.eq(ProcessInfo.PROCESS_INFO.ID))
//                .where(ProcessInstance.PROCESS_INSTANCE.ID.eq(instanceId))
//                .and(ProcessInfo.PROCESS_INFO.PROCESS_NAME.eq(processName))
//                .fetchInto(InstanceTasks.class);
//    }

    public List<TaskDto> getAllTasksByInstanceId(String processName, UUID instanceId) {
        return context
                .select(
                        INSTANCE_TASKS.BPMN_ELEMENT_ID,
                        INSTANCE_TASKS.STATUS,
                        INSTANCE_TASKS.START_TIME,
                        INSTANCE_TASKS.END_TIME,
                        INSTANCE_TASKS.INSTANCE_ID,
                        INSTANCE_TASKS.ID,
                        ProcessInfo.PROCESS_INFO.PROCESS_NAME
                )
                .from(INSTANCE_TASKS)
                .join(ProcessInstance.PROCESS_INSTANCE)
                .on(ProcessInstance.PROCESS_INSTANCE.ID.eq(INSTANCE_TASKS.INSTANCE_ID))
                .join(ProcessInfo.PROCESS_INFO)
                .on(ProcessInstance.PROCESS_INSTANCE.PROCESS_ID.eq(ProcessInfo.PROCESS_INFO.ID))
                .where(ProcessInstance.PROCESS_INSTANCE.ID.eq(instanceId))
                .and(ProcessInfo.PROCESS_INFO.PROCESS_NAME.eq(processName))
                .fetchInto(TaskDto.class);
    }

    public List<Status> getTasksStatusByInstanceId(UUID instanceId, List<String> elementsId) {
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
                .set(INSTANCE_TASKS.STATUS, Status.RUNNING)
                .where(INSTANCE_TASKS.ID.in(
                        context
                                .select(INSTANCE_TASKS.ID)
                                .from(INSTANCE_TASKS)
                                .where(INSTANCE_TASKS.STATUS.eq(Status.PENDING))
                                .orderBy(INSTANCE_TASKS.START_TIME.asc())
                                .limit(batchSize)
                                .forUpdate().skipLocked()
                ))
                .returning()
                .fetchInto(com.database.entity.generated.tables.pojos.InstanceTasks.class);
    }

    public void createTaskForInstance(UUID instanceId, String elementId) {
        createTaskForInstance(instanceId, elementId, null);
    }

//    public void createTaskForInstance(UUID instanceId, String elementId, Connection connection) {
//        DSLContext dsl = connection != null
//                ? DSL.using(connection, SQLDialect.POSTGRES)
//                : context;
//
//        boolean exists = checkTaskExists(instanceId, elementId, dsl);
//
//        if (!exists) {
//            createNewTaskRecord(instanceId, elementId, dsl);
//        }
//    }

    public void createTaskForInstance(UUID instanceId, String elementId, Connection connection) {
        DSLContext dsl = connection != null
                ? DSL.using(connection, SQLDialect.POSTGRES)
                : context;

        dsl.insertInto(INSTANCE_TASKS)
                .set(INSTANCE_TASKS.INSTANCE_ID, instanceId)
                .set(INSTANCE_TASKS.BPMN_ELEMENT_ID, elementId)
                .set(INSTANCE_TASKS.STATUS, Status.PENDING)
                .set(INSTANCE_TASKS.CURRENT_RETRIES_AMOUNT, 0)
                .onConflictDoNothing()
                .execute();
    }

//    private boolean checkTaskExists(UUID instanceId, String elementId, DSLContext dsl) {
//        return dsl.fetchExists(
//                dsl.selectFrom(INSTANCE_TASKS)
//                        .where(INSTANCE_TASKS.INSTANCE_ID.eq(instanceId))
//                        .and(INSTANCE_TASKS.BPMN_ELEMENT_ID.eq(elementId))
//        );
//    }

//    private void createNewTaskRecord(UUID instanceId, String elementId, DSLContext dsl) {
//        InstanceTasksRecord record = dsl.newRecord(INSTANCE_TASKS);
//        record.setInstanceId(instanceId);
//        record.setBpmnElementId(elementId);
//        record.setStatus(Status.PENDING);
//        record.setCurrentRetriesAmount(0);
//        record.store();
//    }

    public void updateTask(com.database.entity.generated.tables.pojos.InstanceTasks newTask) {
        context.update(INSTANCE_TASKS)
                .set(INSTANCE_TASKS.STATUS, newTask.getStatus())
                .set(INSTANCE_TASKS.START_TIME, newTask.getStartTime())
                .set(INSTANCE_TASKS.END_TIME, newTask.getEndTime())
                .set(INSTANCE_TASKS.CURRENT_RETRIES_AMOUNT, newTask.getCurrentRetriesAmount())
                .where(INSTANCE_TASKS.ID.eq(newTask.getId()))
                .execute();

    }

    public void updateStartTimeIfNull(UUID taskId, OffsetDateTime newStartTime) {
        context.update(INSTANCE_TASKS)
                .set(INSTANCE_TASKS.START_TIME,
                        DSL.when(INSTANCE_TASKS.START_TIME.isNull(), newStartTime)
                                .otherwise(INSTANCE_TASKS.START_TIME))
                .where(INSTANCE_TASKS.ID.eq(taskId))
                .execute();
    }

    public void updateTask(Connection connection, com.database.entity.generated.tables.pojos.InstanceTasks task) {
        DSLContext dsl = DSL.using(connection, SQLDialect.POSTGRES);
        updateTask(dsl, task.getId(), task.getStatus(), task.getStartTime(), task.getEndTime(), task.getCurrentRetriesAmount());
    }

    public void updateTask(UUID id, Status status, OffsetDateTime startTime, OffsetDateTime endTime, Integer currentRetriesAmount) {
        updateTask(context, id, status, startTime, endTime, currentRetriesAmount);
    }

    public void updateTask(Connection connection, UUID id, Status status, OffsetDateTime startTime, OffsetDateTime endTime, Integer currentRetriesAmount) {
        DSLContext dsl = DSL.using(connection, SQLDialect.POSTGRES);
        updateTask(dsl, id,  status, startTime, endTime, currentRetriesAmount);
    }

    private void updateTask(DSLContext dsl, UUID id, Status status, OffsetDateTime startTime, OffsetDateTime endTime, Integer currentRetriesAmount) {
        UpdateSetFirstStep<InstanceTasksRecord> updateStep = dsl.update(INSTANCE_TASKS);
        UpdateSetMoreStep<InstanceTasksRecord> update = null;

        if (status != null) {
            update = updateStep.set(INSTANCE_TASKS.STATUS, status);
        }
        if (startTime != null) {
            Field<OffsetDateTime> startTimeField = INSTANCE_TASKS.START_TIME;
            Field<OffsetDateTime> startTimeValue = DSL.when(startTimeField.isNull(), startTime)
                    .otherwise(startTimeField);
            update = (update == null ? updateStep.set(startTimeField, startTimeValue)
                    : update.set(startTimeField, startTimeValue));
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

    public void updateStatus(UUID id, Status status) {
        context.update(INSTANCE_TASKS)
                .set(INSTANCE_TASKS.STATUS, status)
                .where(INSTANCE_TASKS.ID.eq(id))
                .execute();
    }

    public void updateStatus(Connection connection, UUID id, Status status) {
        DSLContext dsl = DSL.using(connection, SQLDialect.POSTGRES);
        dsl.update(INSTANCE_TASKS)
                .set(INSTANCE_TASKS.STATUS, status)
                .where(INSTANCE_TASKS.ID.eq(id))
                .execute();
    }
}
