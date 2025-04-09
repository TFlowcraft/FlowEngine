package api.service;

import api.dto.TaskDto;
import engine.model.BpmnElement;
import persistence.repository.impl.TaskRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;


public class TaskService {
    TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskDto> getAllTasksByInstanceId(String processName, UUID instanceId) {
        return taskRepository.getAllTasksByInstanceId(processName, instanceId);
     }
}
