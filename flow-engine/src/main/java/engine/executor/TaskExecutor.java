package engine.executor;

import com.database.entity.generated.tables.pojos.InstanceTasks;
import engine.common.ProcessNavigator;
import engine.common.Status;
import engine.common.TaskDelegate;
import engine.model.BpmnElement;
import engine.common.ExecutionContext;
import org.jooq.JSONB;
import persistence.TransactionManager;
import persistence.repository.impl.ProcessInstanceRepository;
import persistence.repository.impl.TaskRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static persistence.repository.impl.TaskRepository.createFailedTask;
import static persistence.repository.impl.TaskRepository.createRetryTask;


public class TaskExecutor {
    private final BlockingQueue<InstanceTasks> taskQueue;
    private final ExecutorService executor;
    private final ProcessInstanceRepository processInstanceRepository;
    private final TaskRepository taskRepository;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Map<String, TaskDelegate> userTasks;
    private final int THREAD_POOL_SIZE;
    private final int RETRIES_AMOUNT;
    private final ProcessNavigator processNavigator;

    public TaskExecutor(BlockingQueue<InstanceTasks> taskQueue,
                        int threadPoolSize, ProcessInstanceRepository processInstanceRepository,
                        TaskRepository taskRepository,
                        Map<String, TaskDelegate> userTasks,
                        int retriesAmount, ProcessNavigator processNavigator) {
        this.taskQueue = taskQueue;
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
        THREAD_POOL_SIZE = threadPoolSize;
        this.processInstanceRepository = processInstanceRepository;
        this.taskRepository = taskRepository;
        this.userTasks = userTasks;
        this.RETRIES_AMOUNT = retriesAmount;
        this.processNavigator = processNavigator;
    }

    public void start() {
        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            executor.submit(this::processQueue);
        }
    }

    private void processQueue() {
        while (running.get()) {
            try {
                InstanceTasks task = taskQueue.take();
                processTask(task);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (SQLException e) {
                //Подумать куда ее сунуть
                e.printStackTrace();
            }
        }
    }

    private void processTask(InstanceTasks task) throws SQLException {
        OffsetDateTime startedAt = OffsetDateTime.now();
        String elementType = processNavigator.getElementTypeById(task.getBpmnElementId()).toLowerCase();
        if (elementType.contains("gateway")) {
            processGateway(task, elementType);
        } else {
            TaskDelegate userImpl = userTasks.get(task.getBpmnElementId());
            if (userImpl == null) {
                handleMissingImplementation(task);
                return;
            }
            try {
                userImpl.execute(new ExecutionContext(task,
                        processInstanceRepository.getBusinessData(task.getInstanceId())));
                handleTaskSuccess(task, startedAt);
            } catch (Exception e) {
                handleTaskFailure(task, userImpl, startedAt, e);
            }
        }
    }



    private void processGateway(InstanceTasks task, String elementType) throws SQLException {
        List<String> incomingElementsId = processNavigator.getIncomingElementsId(task.getBpmnElementId());
        int completedTaskAmount = taskRepository.getCompletedTasksForInstance(task.getInstanceId(), task.getId(), incomingElementsId);
        if (completedTaskAmount != incomingElementsId.size()) {
            /*Bad practice, we can lose task if queue is full
            If we're working with small process, try use offer() method or some construction to guarantee adding element
            Or change status to "PENDING" and give work to poller

            P.S. "exclusiveGateway" currently unsupported*/
            taskQueue.add(task);
        } else {
            TransactionManager.executeInTransaction(connection -> {
                createNextTasks(task, connection);
            });

        }
    }

    private void handleTaskSuccess(InstanceTasks task, OffsetDateTime startedAt) throws SQLException {
        TransactionManager.executeInTransaction(connection -> {
            taskRepository.updateTask(connection, task.getId(),
                    Status.COMPLETED,startedAt, OffsetDateTime.now(), task.getCurrentRetriesAmount());
            createNextTasks(task, connection);
        });
    }

    //Maybe create method with function interface for multiple creation with or without condition
    private void createNextTasks(InstanceTasks task, Connection connection) {
        List<BpmnElement> outgoingElements = processNavigator.getOutgoingElements(task.getBpmnElementId());
        for (var element : outgoingElements) {
            if (element.getType().equals("endEvent")) {
                processInstanceRepository.updateInstanceEndTimeIfNull(task.getInstanceId(),OffsetDateTime.now(), connection);
            } else {
                taskRepository.createTaskForInstance(task.getInstanceId(), element.getId(), connection);
            }
        }
    }

    private void handleTaskFailure(InstanceTasks task,
                                   TaskDelegate userImpl,
                                   OffsetDateTime startedAt,
                                   Exception exception) {
        try {
            TransactionManager.executeInTransaction(connection -> {
                InstanceTasks updatedTask = shouldRetry(task)
                        ? createRetryTask(task, startedAt, exception)
                        : createFailedTask(task, startedAt, exception);

                if (!shouldRetry(task)) {
                    taskRepository.updateTask(connection, updatedTask);
                    processInstanceRepository.updateInstance(connection, task.getInstanceId(), null, null, OffsetDateTime.now());
                } else {
                    taskRepository.updateTask(connection, updatedTask);
                    taskQueue.add(updatedTask);
                }
            });
            if (userImpl != null) {
                performRollback(userImpl, task, processInstanceRepository.getBusinessData(task.getInstanceId()));
            }
        } catch (Exception e) {
            //Подумать че тут, наверное еще rollback остальных вызвать
            rollbackCompletedTasks();
            e.printStackTrace();
        }
    }

    private void rollbackCompletedTasks() {

    }

    private boolean shouldRetry(InstanceTasks task) {
        return task.getCurrentRetriesAmount() < RETRIES_AMOUNT;
    }


    private void performRollback(TaskDelegate userImpl, InstanceTasks task, JSONB businessData) {
        userImpl.rollback(new ExecutionContext(task, businessData));
    }

    private void handleMissingImplementation(InstanceTasks task) throws SQLException {
        TransactionManager.executeInTransaction(connection -> {
            OffsetDateTime endAt = OffsetDateTime.now();
            processInstanceRepository.updateInstance(connection, null, null, null, endAt);
            taskRepository.updateTask(connection, task.getId(), Status.FAILED,  null, endAt, null);
        });
    }

    public void shutdown() {
        running.set(false);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}