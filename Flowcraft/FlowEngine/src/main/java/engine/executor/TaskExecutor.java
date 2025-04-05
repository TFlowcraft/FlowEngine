package engine.executor;

import com.database.entity.generated.tables.pojos.InstanceTasks;
import engine.common.ProcessNavigator;
import engine.common.TaskDelegate;
import engine.model.ExecutionContext;
import org.jooq.JSONB;
import persistence.TransactionManager;
import persistence.repository.impl.ProcessInstanceRepository;
import persistence.repository.impl.TaskRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static persistence.repository.impl.TaskRepository.createFailedTask;
import static persistence.repository.impl.TaskRepository.createRetryTask;

//TODO Добавить нормальный переход на создание следующей таски
// Посмотреть где и как обработать ошибки
// Подумать, что делать с retry и где его запускать
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
                e.printStackTrace();
            }
        }
    }

    private void processTask(InstanceTasks task) throws SQLException {
        OffsetDateTime startedAt = OffsetDateTime.now();
        /*check what we are exec
        * if bpmnElementId.get() == task then we use userImpl and exec
        * if bpmnElementId.get() == gateway then we check IN task status and if they completed, we create OUT tasks
        * but, we need to wait or retry task if IN tasks are not completed
         */
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
            //here we do retry and rollback's
            handleTaskFailure(task, userImpl, startedAt, e);
        }
    }

    private void handleTaskSuccess(InstanceTasks task, OffsetDateTime startedAt) throws SQLException {
        TransactionManager.executeInTransaction(connection -> {
            taskRepository.updateTask(connection, task.getId(),
                    "SUCCESS",startedAt, OffsetDateTime.now(), task.getCurrentRetriesAmount());
            createNextTask(task, connection);
        });
    }

    private void createNextTask(InstanceTasks task, Connection connection) {
        processNavigator.getOutgoingElements(task.getBpmnElementId())
                .forEach(element ->  {
                    if (element.getType().equals("endEvent")) {
                        processInstanceRepository.updateInstance(connection, task.getInstanceId(),null, null, OffsetDateTime.now());
                    } else {
                        taskRepository.createTaskForInstance(task.getInstanceId(),
                                element.getId(), connection);
                    }
                });
    }

    private void handleTaskFailure(InstanceTasks task,
                                   TaskDelegate userImpl,
                                   OffsetDateTime startedAt,
                                   Exception exception) {
        try {
            TransactionManager.executeInTransaction(connection -> {
                InstanceTasks updatedTask = shouldRetry(task)
                        ? createRetryTask(task, startedAt)
                        : createFailedTask(task, startedAt);

                if (!shouldRetry(task)) {
                    taskRepository.updateTask(connection, updatedTask.getId(), "FAIL", null, OffsetDateTime.now(), null);
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
            e.printStackTrace();
        }
    }

    private boolean shouldRetry(InstanceTasks task) {
        return task.getCurrentRetriesAmount() < RETRIES_AMOUNT;
    }


    private void performRollback(TaskDelegate userImpl, InstanceTasks task, JSONB businessData) {
        userImpl.rollback(new ExecutionContext(task, businessData));
    }

    private void handleMissingImplementation(InstanceTasks task) throws SQLException {
        TransactionManager.executeInTransaction(connection -> {
            OffsetDateTime endedAt = OffsetDateTime.now();
            processInstanceRepository.updateInstance(null, null, null, endedAt);
            taskRepository.updateTask(connection, task.getId(), "FAILED",  null, endedAt, null);
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