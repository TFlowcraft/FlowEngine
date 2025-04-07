package api.service;

import api.dto.TaskDto;
import com.database.entity.generated.tables.pojos.InstanceTasks;
import engine.model.BpmnElement;
import persistence.repository.impl.TaskRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
                        task.getEndTime()
                ))
                .toList();
     }

    private String getTaskName(String bpmnElementId) {
        return bpmnElements.get(bpmnElementId).getName();
    }
}
