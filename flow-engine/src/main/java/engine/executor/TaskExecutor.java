package engine.executor;

import com.database.entity.generated.tables.pojos.InstanceTasks;
import engine.common.*;
import engine.model.BpmnElement;
import persistence.TransactionManager;
import persistence.repository.impl.ProcessInstanceRepository;
import persistence.repository.impl.TaskRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
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
                InstanceTasks task = taskQueue.poll(100, TimeUnit.MILLISECONDS);
                if (task != null) {
                    processTask(task);
                } else {
                    if (!running.get()) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (SQLException e) {
                e.printStackTrace();

                if (running.get()) {
                    continue;
                }
            }
        }
    }

    private void processTask(InstanceTasks task) throws SQLException {
        OffsetDateTime startedAt = OffsetDateTime.now();
        String elementType = processNavigator.getElementTypeById(task.getBpmnElementId());
        try {
            if (isGateway(elementType)) {
                handleGateway(task, elementType, startedAt);
            } else if (isEvent(elementType)) {
                handleEvent(task, elementType, startedAt);
            } else {
                handleUserTask(task, startedAt);
            }
        } catch (Exception e) {
            //тут в принципе обрабатываем ошибку
            handleGenericFailure(task, startedAt, e);
        }
    }

    private boolean isGateway(String elementType) {
        return elementType.contains("gateway") || elementType.contains("Gateway");
    }

    private boolean isEvent(String elementType) {
        return elementType.contains("event") || elementType.contains("Event");
    }

    private void handleGateway(InstanceTasks task, String elementType, OffsetDateTime startedAt) throws SQLException {
        List<String> incomingElementsId = processNavigator.getIncomingElementsId(task.getBpmnElementId());
        UUID instanceId = task.getInstanceId();
        if (taskRepository.hasFailedTasks(instanceId, incomingElementsId)) {
            TransactionManager.executeInTransaction(connection ->
                    taskRepository.updateTask(connection,
                    task.getId(),
                    Status.FAILED,
                    startedAt,
                    OffsetDateTime.now(),
                    task.getCurrentRetriesAmount()
            ));
            return;
        }
        if (!taskRepository.areAllTasksCompleted(instanceId, incomingElementsId)) {
            if (!taskQueue.offer(task)) {
        TransactionManager.executeInTransaction(
            connection ->
                taskRepository.updateTask(
                    connection,
                    task.getId(),
                    Status.PENDING,
                    startedAt,
                    task.getEndTime(),
                    task.getCurrentRetriesAmount()));
            }
            return;
        }
        completeTaskAndProceed(task, startedAt);
    }

    private void handleUserTask(InstanceTasks task, OffsetDateTime startedAt) throws Exception {
        TaskDelegate userImpl = userTasks.get(task.getBpmnElementId());
        if (userImpl == null) {
            handleMissingImplementation(task, startedAt);
            return;
        }

        Map<String, Object> businessData = processInstanceRepository.getBusinessData(task.getInstanceId());
        ExecutionContext context = new ExecutionContext(task, businessData);
        try{
            userImpl.execute(context);
            try {
                completeTaskAndProceed(task, startedAt);
            } catch (Exception dbException) {
                userImpl.rollback(context);
                retryTask(task, startedAt, dbException);
            }
        } catch (Exception taskException) {
            retryTask(task, startedAt, taskException);
        }
    }

    private void retryTask(InstanceTasks task, OffsetDateTime startedAt, Exception exception) {
        try {
            TransactionManager.executeInTransaction(connection -> {
               if (shouldRetry(task)) {
                   InstanceTasks retryTask = createRetryTask(task, exception);
                   if (!taskQueue.offer(retryTask)) {
                       taskRepository.updateTask(connection, retryTask);
                   }
               } else {
                   OffsetDateTime endedAt = OffsetDateTime.now();
                   InstanceTasks failedTask = createFailedTask(task, startedAt, endedAt, exception);
                   taskRepository.updateTask(connection, failedTask);
                   processInstanceRepository.updateInstance(
                           connection,
                           task.getInstanceId(),
                           null,
                           null,
                           endedAt
                   );
                   //Откат всех ранее выполненных
               }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Тут переделать просто для обработки Start и End events
    private void handleEvent(InstanceTasks task, String elementType, OffsetDateTime startedAt) throws SQLException {
        if (elementType.equalsIgnoreCase("StartEvent")) {
            completeTaskAndProceed(task, startedAt);
            return;
        }
        if (elementType.equalsIgnoreCase("EndEvent")) {
            TransactionManager.executeInTransaction(connection -> {
                var endTime = OffsetDateTime.now();
                processInstanceRepository.updateInstanceEndTimeIfNull(task.getInstanceId(), endTime, connection);
                taskRepository.updateTask(
                        connection,
                        task.getId(),
                        Status.COMPLETED,
                        startedAt,
                        endTime,
                        task.getCurrentRetriesAmount());
            });
        }
    }

    private void completeTaskAndProceed(InstanceTasks task, OffsetDateTime startedAt) throws SQLException {
        TransactionManager.executeInTransaction(connection -> {
            taskRepository.updateTask(
                    connection,
                    task.getId(),
                    Status.COMPLETED,
                    startedAt,
                    OffsetDateTime.now(),
                    task.getCurrentRetriesAmount()
            );
            createNextTasks(task, connection);
        });
    }


    private void handleGenericFailure(InstanceTasks task, OffsetDateTime startedAt, Exception e) {
        TaskDelegate userImpl = userTasks.get(task.getBpmnElementId());
        if (e instanceof SQLException) {
           if (userImpl != null) {
               try{
                   ExecutionContext context = new ExecutionContext(task,
                           processInstanceRepository.getBusinessData(task.getInstanceId()));
                   userImpl.rollback(context);
               } catch (Exception rollbackException) {
                   rollbackException.printStackTrace();
               }
               retryTask(task, startedAt, e);
           }
        } else {
            retryTask(task, startedAt, e);
        }
    }


    private void createNextTasks(InstanceTasks task, Connection connection) {
        List<BpmnElement> outgoingElements = processNavigator.getOutgoingElements(task.getBpmnElementId());
        for (var element : outgoingElements) {
            taskRepository.createTaskForInstance(task.getInstanceId(), element.getId(), connection);
        }
    }


    private boolean shouldRetry(InstanceTasks task) {
        return task.getCurrentRetriesAmount() < RETRIES_AMOUNT;
    }


    private void handleMissingImplementation(InstanceTasks task, OffsetDateTime startedAt) throws SQLException {
        TransactionManager.executeInTransaction(connection -> {
            OffsetDateTime endAt = OffsetDateTime.now();
            processInstanceRepository.updateInstance(connection, null, null, null, endAt);
            InstanceTasks failedTask = createFailedTask(task, startedAt, endAt, null);
            taskRepository.updateTask(connection, failedTask);
        });
    }

    public void shutdown() {
        /* Тут не делаем откат по сути, а просто всей очереди выставляет PENDING
         */
        running.set(false);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            try {
                for (var task : taskQueue) {
                    taskRepository.updateStatus(task.getId(), Status.PENDING);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}