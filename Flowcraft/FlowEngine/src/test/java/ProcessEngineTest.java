import api.service.TaskService;
import com.database.entity.generated.tables.pojos.InstanceTasks;
import engine.ProcessEngine;
import engine.common.TaskDelegate;
import engine.model.ExecutionContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xml.sax.SAXException;
import persistence.poller.ProcessPoller;
import persistence.repository.impl.ProcessInstanceRepository;
import persistence.repository.impl.TaskRepository;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ProcessEngineTest {

    @ParameterizedTest
    @ValueSource(strings = {"/diagram.bpmn"})
    public void createProcessEngine(String processSchemePath) {
        TaskRepository service = new TaskRepository();
        List<TaskDelegate> taskDelegates = getTaskDelegates();
        var queue = new ArrayBlockingQueue<InstanceTasks>(100);
        var instanceRepo = new ProcessInstanceRepository();
        try {
            ProcessEngine processEngine = new ProcessEngine.ProcessEngineConfigurator()
                    .setBpmnProcessFile(processSchemePath)
                    .setUserTaskImplementation(taskDelegates)
                    .setEngineQueue(queue)
                    .setPoolSize(10)
                    .setProcessInstanceRepository(instanceRepo)
                    .setTaskRepository(new TaskRepository())
                    .build();
            processEngine.start();
            ProcessPoller poller = new ProcessPoller(queue, new TaskRepository(), new ScheduledThreadPoolExecutor(100));
            poller.start();
           // processEngine.createProcessInstance(null);
            var instance = service.getAll("");
            for (var task : instance) {
                System.out.println(task.toString());
            }
            var res  = instanceRepo.getAll("processNameTest");
            assert res != null;
            Thread.sleep(500_000);
            assert !instance.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    @ParameterizedTest
//    @ValueSource(strings = {"/diagram.bpmn"})
//    public void createAndRunEngine(String processSchemePath) throws ParserConfigurationException, IOException, SAXException {
//        ProcessEngine engine = new ProcessEngine.ProcessEngineConfigurator()
//                .setBpmnProcessFile(processSchemePath)
//                .setEngineQueue(new ArrayBlockingQueue<InstanceTasks>(100))
//                .setPoolSize(100)
//                .setTaskRepository(new TaskRepository())
//                .setRetriesAmount(10)
//                .setProcessInstanceRepository(new ProcessInstanceRepository())
//                .build();
//    }
    @NotNull
    private static List<TaskDelegate> getTaskDelegates() {
        List<TaskDelegate> taskDelegates = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            int finalI = i;
            taskDelegates.add(new TaskDelegate() {
                @Override
                public void execute(ExecutionContext context) {
                    System.out.printf("Executing task %d \n", finalI);
                }

                @Override
                public void rollback(ExecutionContext context) {
                    System.out.printf("Rollback task %d \n", finalI);
                }
            });
        }
        return taskDelegates;
    }
}
