package engine.executor;

import com.database.entity.generated.tables.pojos.InstanceTasks;
import engine.ProcessEngine;
import engine.TaskDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.repository.impl.TaskRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskExecutor {
    private static final Logger logger = LoggerFactory.getLogger(TaskExecutor.class);
    private static final int MAX_RETRIES = 10;

    private final BlockingQueue<InstanceTasks> taskQueue;
    private final ExecutorService executor;
    private final TaskRepository taskRepository;
    private ProcessEngine processEngine;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final int THREAD_POOL_SIZE;

    public TaskExecutor(BlockingQueue<InstanceTasks> taskQueue,
                        int threadPoolSize,
                        TaskRepository taskRepository,
                        ProcessEngine processEngine) {
        this.taskQueue = taskQueue;
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
        THREAD_POOL_SIZE = threadPoolSize;
        this.taskRepository = taskRepository;
        this.processEngine = processEngine;
    }

    public void start() {
        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            executor.submit(this::processTasks);
        }
    }

    private void processTasks() {
        while (running.get()) {
            try {
                InstanceTasks task = taskQueue.take();
                processSingleTask(task);
            } catch (InterruptedException e) {
                logger.info("Task processing interrupted", e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error("Unexpected error in task processing", e);
            }
        }
    }

    private void processSingleTask(InstanceTasks task) {
        OffsetDateTime startedAt = OffsetDateTime.now();
        Optional<TaskDelegate> userImplOpt = Optional.ofNullable(processEngine.getUserTaskImplementation(task.getBpmnElementId()));

        if (userImplOpt.isEmpty()) {
            handleMissingImplementation(task);
            return;
        }

        TaskDelegate userImpl = userImplOpt.get();
        InstanceTasks updatedTask = null;

        try {
            userImpl.execute(""); // Используем ID задачи как параметр
            updatedTask = createCompletedTask(task, startedAt);
        } catch (Exception e) {
            updatedTask = handleTaskFailure(task, userImpl, startedAt, e);
        } finally {
            if (updatedTask != null) {
                taskRepository.updateTask(updatedTask);
            }
        }
    }

    private InstanceTasks createCompletedTask(InstanceTasks task, OffsetDateTime startedAt) {
        return new InstanceTasks(
                task.getId(),
                task.getInstanceId(),
                task.getBpmnElementId(),
                "COMPLETED",
                startedAt,
                OffsetDateTime.now(),
                task.getCurrentRetriesAmount()
        );
    }

    private InstanceTasks handleTaskFailure(InstanceTasks task,
                                            TaskDelegate userImpl,
                                            OffsetDateTime startedAt,
                                            Exception exception) {
        logger.error("Task execution failed: {}", task.getId(), exception);

        int retries = task.getCurrentRetriesAmount() + 1;

        if (retries < MAX_RETRIES) {
            performRollback(userImpl);
            return createRetryTask(task, startedAt, retries);
        } else {
            return createFailedTask(task, startedAt, retries);
        }
    }

    private void performRollback(TaskDelegate userImpl) {
        try {
            userImpl.rollback();
        } catch (Exception e) {
            logger.error("Rollback failed", e);
        }
    }

    private InstanceTasks createRetryTask(InstanceTasks task,
                                          OffsetDateTime startedAt,
                                          int retries) {
        taskQueue.add(task); // Возвращаем задачу в очередь
        return new InstanceTasks(
                task.getId(),
                task.getInstanceId(),
                task.getBpmnElementId(),
                "PENDING",
                startedAt,
                task.getEndTime(), // endTime остается null пока задача не завершена
                retries
        );
    }

    private InstanceTasks createFailedTask(InstanceTasks task,
                                           OffsetDateTime startedAt,
                                           int retries) {
        return new InstanceTasks(
                task.getId(),
                task.getInstanceId(),
                task.getBpmnElementId(),
                "FAILED",
                startedAt,
                OffsetDateTime.now(), // Фиксируем время провала
                retries
        );
    }

    private void handleMissingImplementation(InstanceTasks task) {
        logger.error("No implementation found for task: {}", task.getBpmnElementId());
        InstanceTasks failedTask = new InstanceTasks(
                task.getId(),
                task.getInstanceId(),
                task.getBpmnElementId(),
                "FAILED",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                task.getCurrentRetriesAmount()
        );
        taskRepository.updateTask(failedTask);
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

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }
}