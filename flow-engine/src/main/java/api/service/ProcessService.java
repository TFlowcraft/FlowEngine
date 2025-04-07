package api.service;

import com.database.entity.generated.tables.pojos.ProcessInfo;
import persistence.repository.impl.ProcessInfoRepository;

import java.util.List;

public class ProcessService {
    private final ProcessInfoRepository processInfoRepository;

    public ProcessService(ProcessInfoRepository processInfoRepository) {
        this.processInfoRepository = processInfoRepository;
    }

    public List<ProcessInfo> getAllProcessInfos() {
        return processInfoRepository.getAllProcesses();
    }
}
