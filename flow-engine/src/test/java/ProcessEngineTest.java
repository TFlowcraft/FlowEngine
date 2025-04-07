import engine.ProcessEngine;
import engine.common.TaskDelegate;
import engine.common.ExecutionContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


import java.util.ArrayList;
import java.util.List;

public class ProcessEngineTest {

    @ParameterizedTest
    @ValueSource(strings = {"/diagram.bpmn"})
    public void createProcessEngine(String processSchemePath) {
        List<TaskDelegate> taskDelegates = getTaskDelegates();
        try {
            ProcessEngine processEngine = new ProcessEngine.ProcessEngineConfigurator()
                    .useDefaults(processSchemePath, taskDelegates)
                    .build();
            processEngine.start();
           // processEngine.createProcessInstance(null);
            while (true) {
                //
            }
            //assert !instance.isEmpty();
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
                    for (int j = 0; j < 1_000_000_00; j++) {

                    }
                }

                @Override
                public void rollback(ExecutionContext context) {

                    System.out.printf("Rollback task %d \n", finalI);
                    for (int j = 0; j < 1_000_000_00; j++) {

                    }
                }
            });
        }
        return taskDelegates;
    }
}
