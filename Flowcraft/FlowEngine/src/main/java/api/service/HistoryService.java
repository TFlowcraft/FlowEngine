package api.service;

import com.database.entity.generated.tables.pojos.InstanceHistory;
import com.database.entity.generated.tables.pojos.ProcessInstance;
import persistence.repository.impl.HistoryRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class HistoryService {
    private final HistoryRepository historyRepository;

    public HistoryService(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public List<InstanceHistory> getHistoryTaskById(String processName, UUID instanceId, UUID taskId) {
        return historyRepository.getById(processName, instanceId, taskId);
    }

    public List<InstanceHistory> getAllHistoryTask(String processName, UUID instanceId) {
        return historyRepository.getAll(processName, instanceId);
    }
}
