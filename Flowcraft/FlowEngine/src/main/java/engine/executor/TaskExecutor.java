package engine.executor;

import com.database.entity.generated.tables.pojos.InstanceTasks;
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

    public TaskExecutor(BlockingQueue<InstanceTasks> taskQueue,
                        int threadPoolSize, ProcessInstanceRepository processInstanceRepository,
                        TaskRepository taskRepository,
                        Map<String, TaskDelegate> userTasks,
                        int retriesAmount) {
        this.taskQueue = taskQueue;
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
        THREAD_POOL_SIZE = threadPoolSize;
        this.processInstanceRepository = processInstanceRepository;
        this.taskRepository = taskRepository;
        this.userTasks = userTasks;
        this.RETRIES_AMOUNT = retriesAmount;
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

    private void handleTaskSuccess(InstanceTasks task, OffsetDateTime startedAt) {
        try {
            TransactionManager.executeInTransaction(connection -> {
                taskRepository.updateTask(connection, task.getId(),
                        "SUCCESS",startedAt, OffsetDateTime.now(), task.getCurrentRetriesAmount());
                createNextTasks(task, connection);
            });
        } catch (Exception e) {
            // Здесь обработать ошибки
            handleTaskFailure(task, null, startedAt, e);
        }
    }

    private void createNextTasks(InstanceTasks task, Connection connection) {
        //Tут отдельный компонент который достает следующий элемент и создает в БД
//        processEngine.getBpmnProcess()
//                .get(task.getBpmnElementId())
//                .getOutgoing()
//                .forEach(nextElementId ->
//                        taskRepository.createTaskForInstance(
//                                task.getInstanceId(),
//                                nextElementId,
//                                connection
//                        )
//                );
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
                    int newRetriesAmount = task.getCurrentRetriesAmount() + 1;
                    taskRepository.updateTask(connection, updatedTask.getId(), null, null, null, newRetriesAmount);
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

    private InstanceTasks createRetryTask(InstanceTasks task, OffsetDateTime startedAt) {
        taskQueue.add(task);
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

    private InstanceTasks createFailedTask(InstanceTasks task, OffsetDateTime startedAt) {
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

    private void performRollback(TaskDelegate userImpl, InstanceTasks task, JSONB businessData) {
        try {
            userImpl.rollback(new ExecutionContext(task, businessData));
        } catch (Exception e) {
            e.printStackTrace();
        }
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