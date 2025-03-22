package engine.executor;

import java.util.concurrent.*;

public class TaskExecutor {
    private final BlockingQueue<Object> taskQueue;
    private final ExecutorService executor;
    private boolean running = true;

    public TaskExecutor(BlockingQueue<Object> taskQueue, int threadPoolSize) {
        this.taskQueue = taskQueue;
        executor = Executors.newFixedThreadPool(threadPoolSize);
    }

    public void start() {
        while (running) {
            try {
                Future<?> future = executor.submit(() -> { });
                //Вынести скорее всего получение результата
                //А именно где-то их хранить и потом вытаскивать
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
                break;
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

}
