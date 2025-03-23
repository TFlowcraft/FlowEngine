package persistence.poller;

import persistence.repository.impl.TaskRepository;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProcessPoller {
    private static final int TASK_BATCH_SIZE = 100;
    private static final int POLL_INTERVAL_MS = 5000;

    private final BlockingQueue<Object> taskQueue;
    private final TaskRepository taskRepository;
    private final ScheduledExecutorService scheduler;

    public ProcessPoller(BlockingQueue<Object> taskQueue, TaskRepository taskRepository, ScheduledExecutorService scheduler) {
        this.taskQueue = taskQueue;
        this.taskRepository = taskRepository;
        this.scheduler = scheduler;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::pollTasks, 0, POLL_INTERVAL_MS, TimeUnit.SECONDS);
    }

    public void stopPolling() {
        scheduler.shutdown();
    }

    private void pollTasks() {
        try {
            var tasks = taskRepository.fetchAndLockTasks(TASK_BATCH_SIZE);
            for (var task : tasks) {
                taskQueue.put(task);
            }
        } catch (Exception e) {
            System.err.println("Exception occurred while polling unprocessed instances");
        }
    }
}
