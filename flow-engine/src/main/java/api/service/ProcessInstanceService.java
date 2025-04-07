package api.service;

import com.database.entity.generated.tables.pojos.ProcessInstance;
import persistence.repository.impl.ProcessInstanceRepository;

import java.util.List;
import java.util.UUID;

public class ProcessInstanceService {
    private final ProcessInstanceRepository repository;

    public ProcessInstanceService(ProcessInstanceRepository repository) {
        this.repository = repository;
    }

    public ProcessInstance getProcessInstanceById(String processName, UUID processInstanceId) {
        return repository.getById(processName, processInstanceId);
    }

    public List<ProcessInstance> getAllProcessInstances(String processName) {
        return repository.getAll(processName);
    }


}
