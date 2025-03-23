package api.service;

import com.database.entity.generated.tables.pojos.ProcessInstance;
import persistence.repository.impl.ProcessInstanceRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProcessInstanceService {
    private final ProcessInstanceRepository repository;

    public ProcessInstanceService(ProcessInstanceRepository repository) {
        this.repository = repository;
    }

    public ProcessInstance getProcessInstance(UUID id) {
        return repository.getById(id).into(ProcessInstance.class);
    }

    public List<ProcessInstance> getProcessInstances() {
        return repository.getAll().stream()
                .map(record -> record.into(ProcessInstance.class))
                .collect(Collectors.toList());
    }
}
