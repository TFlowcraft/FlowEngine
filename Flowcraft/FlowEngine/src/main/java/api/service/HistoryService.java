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

    public ProcessInstance getHistory(UUID processInstanceId) {
        return historyRepository.getById(processInstanceId).into(ProcessInstance.class);
    }

    public List<InstanceHistory> getHistories() {
        return historyRepository.getAll().stream()
                .map(record -> record.into(InstanceHistory.class))
                .collect(Collectors.toList());
    }
}
