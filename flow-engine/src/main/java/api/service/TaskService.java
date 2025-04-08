package api.service;

import api.dto.TaskDto;
import engine.model.BpmnElement;
import persistence.repository.impl.TaskRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;


public class TaskService {
    TaskRepository taskRepository;
    Map<String, BpmnElement> bpmnElements;

    public TaskService(TaskRepository taskRepository, Map<String, BpmnElement> bpmnElements) {
        this.taskRepository = taskRepository;
        this.bpmnElements = bpmnElements;
    }

    public List<TaskDto> getAllTasksByInstanceId(String processName, UUID instanceId) {
        return taskRepository.getAll(processName, instanceId)
                .stream()
                .map(task -> new TaskDto(
                        task.getBpmnElementId(),
                        getTaskName(task.getBpmnElementId()),
                        task.getStatus(),
                        task.getStartTime(),
                        task.getEndTime(),
                        task.getInstanceId(),
                        task.getId()
                ))
                .toList();
     }

    private String getTaskName(String bpmnElementId) {
        return bpmnElements.get(bpmnElementId).getName();
    }
}
