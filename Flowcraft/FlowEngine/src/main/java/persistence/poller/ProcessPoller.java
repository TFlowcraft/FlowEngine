package persistence.poller;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ProcessPoller {
    private final Object taskExecutor;
    private final Object processInstanceDAO;
    private final ScheduledExecutorService scheduler;

    public ProcessPoller(Object taskExecutor, Object processInstanceDAO, ScheduledExecutorService scheduler) {
        this.taskExecutor = taskExecutor;
        this.processInstanceDAO = processInstanceDAO;
        this.scheduler = scheduler;
    }

    public void startPolling() {
        scheduler.scheduleAtFixedRate(this::pollUnprocessedInstances, 0, 5, TimeUnit.SECONDS);
    }

    public void stopPolling() {
        scheduler.shutdown();
    }

    private void pollUnprocessedInstances() {
        try {
//            List<ProcessInstance> unprocessedInstances = processInstanceDAO.findAndLockUnprocessedInstances(conn);
//
//            for (ProcessInstance instance : unprocessedInstances) {
//                System.out.println("Processing instance: " + instance.getId());
//                taskExecutor.executeNextTask(instance);
//            }
//
//            conn.commit(); // Фиксируем изменения
        } catch (Exception e) {
            System.err.println("Exception occured while polling unprocessed instances");
        }
    }
}
