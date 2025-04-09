package api.service;

import api.dto.TaskDto;
import engine.model.BpmnElement;
import persistence.repository.impl.TaskRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;


public class TaskService {
    private final TaskRepository taskRepository;

    //This shouldn't be here, temporary placeholder
    //the schema must be in the database and given a name in the request
    //but due to an incorrect design, we currently store the ProcessMap field in the engine
    private final Map<String, BpmnElement> processDefinitions;

    public TaskService(TaskRepository taskRepository, Map<String, BpmnElement> processDefinitions) {
        this.taskRepository = taskRepository;
        this.processDefinitions = processDefinitions;
    }

    public List<TaskDto> getAllTasksByInstanceId(String processName, UUID instanceId) {
        List<TaskDto> tasks = taskRepository.getAllTasksByInstanceId(processName, instanceId);
        for (TaskDto task : tasks) {
            task.setTaskName(processDefinitions.get(task.getBpmnElementId()).getName());
        }
        return tasks;
    }
}
