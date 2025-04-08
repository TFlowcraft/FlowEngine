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
        String elementType = processNavigator.getElementTypeById(task.getBpmnElementId()).toLowerCase();

        try {
            if (isGateway(elementType)) {
                handleGateway(task, elementType);
            } else if (isEndEvent(elementType)) {
                handleEndEvent(task);
            } else {
                handleUserTask(task, startedAt);
            }
        } catch (Exception e) {
            handleGenericFailure(task, startedAt, e);
        }
    }

    private boolean isGateway(String elementType) {
        return elementType.contains("gateway");
    }

    private boolean isEndEvent(String elementType) {
        return elementType.equalsIgnoreCase("endEvent");
    }

    private void handleGateway(InstanceTasks task, String elementType) throws SQLException {
        List<String> incomingElementsId = processNavigator.getIncomingElementsId(task.getBpmnElementId());

        if (!taskRepository.areAllTasksCompleted(task.getInstanceId(), incomingElementsId)) {
            if (!requeueTask(task)) {
                TransactionManager.executeInTransaction(connection -> {
                    taskRepository.updateStatus(connection, task.getId(), Status.PENDING);
                });
            }
            return;
        }

        completeTaskAndProceed(task, OffsetDateTime.now());
    }

    private void handleUserTask(InstanceTasks task, OffsetDateTime startedAt) throws Exception {
        TaskDelegate userImpl = userTasks.get(task.getBpmnElementId());
        if (userImpl == null) {
            handleMissingImplementation(task);
            return;
        }

        ExecutionContext context = new ExecutionContext(task,
                processInstanceRepository.getBusinessData(task.getInstanceId()));

        userImpl.execute(context);
        completeTaskAndProceed(task, startedAt);
    }

    private void handleEndEvent(InstanceTasks task) throws SQLException {
        TransactionManager.executeInTransaction(connection -> {
            var endTime = OffsetDateTime.now();
            processInstanceRepository.updateInstanceEndTimeIfNull(task.getInstanceId(), endTime, connection);
            taskRepository.updateTask(
                    connection,
                    task.getId(),
                    Status.COMPLETED,
                    task.getStartTime(),
                    endTime,
                    task.getCurrentRetriesAmount());
        });
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

    private boolean requeueTask(InstanceTasks task) {
        return taskQueue.offer(task);
    }

    private void handleGenericFailure(InstanceTasks task, OffsetDateTime startedAt, Exception e) {
        TaskDelegate userImpl = userTasks.get(task.getBpmnElementId());
        handleTaskFailure(task, userImpl, startedAt, e);
    }



//    private void processGateway(InstanceTasks task, String elementType) throws SQLException {
//        List<String> incomingElementsId = processNavigator.getIncomingElementsId(task.getBpmnElementId());
//        //int completedTaskAmount = taskRepository.getCompletedTasksForInstance(task.getInstanceId(), task.getId(), incomingElementsId);
//        //if (completedTaskAmount != incomingElementsId.size()) {
//        if (!taskRepository.areAllTasksCompleted(task.getInstanceId(), incomingElementsId)) {
//            /*Bad practice, we can lose task if queue is full
//            If we're working with small process, try use offer() method or some construction to guarantee adding element
//            Or change status to "PENDING" and give work to poller
//
//            P.S. "exclusiveGateway" currently unsupported
//            В общем, концепция такая (на понятийном?):
//                1) Суем назад в очередь для повторной обработки TaskExecutor-ом (это плохо, можем потерять задачу)
//                2) Меняем ей статус снова на PENDING, чтобы ProcessPoller снова достал ее и положил в очередь
//
//                Возможно придумать для этого установку стратегии того как мы будет работать с тасками и отдать
//                пользователь возможность выбрать стратегию обработки*/
//            //Тут еще retry сделать
//            if (!requeueTask(task)) {
//                TransactionManager.executeInTransaction(connection -> {
//                    taskRepository.updateStatus(connection, task.getId(), Status.PENDING);
//                });
//            }
//        } else {
//           // handleTaskSuccess(task,)
//            //TransactionManager.executeInTransaction(connection -> createNextTasks(task, connection));
//
//        }
//    }

//    private void handleTaskSuccess(InstanceTasks task, OffsetDateTime startedAt) throws SQLException {
//        TransactionManager.executeInTransaction(connection -> {
//            taskRepository.updateTask(connection, task.getId(),
//                    Status.COMPLETED, startedAt, OffsetDateTime.now(), task.getCurrentRetriesAmount());
//            createNextTasks(task, connection);
//        });
//    }

    //Maybe create method with function interface for multiple creation with or without condition
    private void createNextTasks(InstanceTasks task, Connection connection) {
        List<BpmnElement> outgoingElements = processNavigator.getOutgoingElements(task.getBpmnElementId());
        for (var element : outgoingElements) {
            //Ну и в будущем тут переделать на использование с ENUM ибо это кошмар работать со String
            //Ибо оно по факту может и в виде EndEvent прилететь сюда и в lowercase, и вообще не еби мозги
            //сделай enum (это уже к parser и BpmnElement)
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
                    if (!taskQueue.offer(task)) {
                        taskRepository.updateTask(connection, updatedTask);
                    }
                }
            });
            if (userImpl != null) {
                performRollback(userImpl, task, processInstanceRepository.getBusinessData(task.getInstanceId()));
            }
        } catch (Exception e) {
            //Подумать че тут, наверное еще rollback остальных вызвать
            e.printStackTrace();
        }
    }


    private boolean shouldRetry(InstanceTasks task) {
        return task.getCurrentRetriesAmount() < RETRIES_AMOUNT;
    }


    private void performRollback(TaskDelegate userImpl, InstanceTasks task, Map<String, Object> businessData) {
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
        //Тут всем PENDING выставить, кто в очереди
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
            for (var task : taskQueue) {
                taskRepository.updateStatus(task.getId(), Status.PENDING);
            }
        }
    }

}