import engine.ProcessEngine;
import engine.common.ProcessNavigator;
import engine.common.TaskDelegate;
import engine.common.ExecutionContext;
import engine.model.BpmnElement;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


import java.nio.file.Path;
import java.util.*;

public class ProcessEngineTest {

    @ParameterizedTest
    @ValueSource(strings = {"/diagramTwoParallelGates.bpmn"})
    public void createProcessEngine(String processSchemePath) {
        List<TaskDelegate> taskDelegates = getTaskDelegates();
        try {
            ProcessEngine processEngine = new ProcessEngine.ProcessEngineConfigurator()
                    .useDefaults(processSchemePath, taskDelegates)
                    .build();
            ProcessNavigator processNavigator = processEngine.getProcessNavigator();
            BpmnElement element = processNavigator.findElementByType("process").orElseThrow();
      UUID processId =
          processEngine.createProcess(
              element.getId(),
              element.getName(),
              "C:\\Users\\degl\\Documents\\GitHub\\FlowEngine\\flow-engine\\src\\test\\resources\\diagramTwoParallelGates.bpmn");
            Map<String, Object> data = new HashMap<>();
            data.put("string-name", "john");
            data.put("int-age", 30);
            data.put("map-friends-name", List.of("Bob", "John", "Alice", "Kate"));
            processEngine.createProcessInstance(processId, data);
            processEngine.start();
            while (true) {}
            //assert !instance.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @NotNull
    private static List<TaskDelegate> getTaskDelegates() {
        List<TaskDelegate> taskDelegates = new ArrayList<>();
        taskDelegates.add(new TaskDelegate() {

            @Override
            public void execute(ExecutionContext context) {
                System.out.println("Executing task 1");
                var name = context.getDataField("string-name");
                System.out.printf("Data field value: %s\n", name);
            }

            @Override
            public void rollback(ExecutionContext context) {
                System.out.println("Rollback task 1");
            }
        });
        taskDelegates.add(new TaskDelegate() {

            @Override
            public void execute(ExecutionContext context) {
                System.out.println("Executing task 2");
                var age = context.getDataField("int-age");
                System.out.printf("Data field value: %s\n", age);
            }

            @Override
            public void rollback(ExecutionContext context) {
                System.out.println("Rollback task 2");
            }
        });
        taskDelegates.add(new TaskDelegate() {

            @Override
            public void execute(ExecutionContext context) {
                System.out.println("Executing task 3");
                List<Object> name = Collections.singletonList(context.getDataField("map-friends-name"));
                for (Object object : name) {
                    System.out.printf("Data field value: %s\n", object);
                }
            }

            @Override
            public void rollback(ExecutionContext context) {
                System.out.println("Rollback task 1");
            }
        });
        return taskDelegates;
    }
}
