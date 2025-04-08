import engine.common.ProcessNavigator;
import org.junit.jupiter.api.Test;
import persistence.DatabaseConfig;
import persistence.repository.impl.TaskRepository;

import java.util.List;
import java.util.UUID;

public class TaskRepoTest {
    @Test
    public void testRepo() {
        DatabaseConfig.setupFromEnv();
        TaskRepository taskRepository = new TaskRepository(DatabaseConfig.getContext());

        //boolean condition = taskRepository.areAllTasksCompleted(UUID.fromString("454c7b3b-e9d3-4268-b025-e9eb42c45630"), List.of("Task_1hcentk"));
//        int res = taskRepository.getCompletedTasksForInstance(
//                UUID.fromString("454c7b3b-e9d3-4268-b025-e9eb42c45630"),
//                        UUID.fromString("1e98f8f9-7b82-4aef-9361-315ce433ca3a"),
//                        List.of("Task_1hcentk")
//                );
//        System.out.println(res);
        //assert condition;

    }
}
