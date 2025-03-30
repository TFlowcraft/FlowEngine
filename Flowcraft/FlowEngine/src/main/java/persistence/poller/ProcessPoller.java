package persistence.poller;

import com.database.entity.generated.tables.pojos.InstanceTasks;
import persistence.repository.impl.TaskRepository;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProcessPoller {
    private static final int TASK_BATCH_SIZE = 100;
    private static final int POLL_INTERVAL_SEC = 1;
    private static final int INITIAL_DELAY_MS = 0;

    private final BlockingQueue<InstanceTasks> taskQueue;
    private final TaskRepository taskRepository;
    private final ScheduledExecutorService scheduler;

    public ProcessPoller(BlockingQueue<InstanceTasks> taskQueue, TaskRepository taskRepository, ScheduledExecutorService scheduler) {
        this.taskQueue = taskQueue;
        this.taskRepository = taskRepository;
        this.scheduler = scheduler;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::pollTasks, INITIAL_DELAY_MS, POLL_INTERVAL_SEC, TimeUnit.SECONDS);
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
            e.printStackTrace();
        }
    }
}
