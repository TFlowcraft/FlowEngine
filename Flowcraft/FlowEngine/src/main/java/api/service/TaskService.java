package api.service;


import com.database.entity.generated.tables.pojos.InstanceHistory;
import com.database.entity.generated.tables.pojos.InstanceTasks;
import persistence.repository.impl.TaskRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TaskService  {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public InstanceTasks getInstanceHistory(UUID id) {
        return taskRepository.getById(id).into(InstanceTasks.class);
    }

    public List<InstanceTasks> getAllInstanceHistory() {
        return taskRepository.getAll().stream()
                .map(record -> record.into(InstanceTasks.class))
                .collect(Collectors.toList());
    }
}
