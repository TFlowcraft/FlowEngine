package api.service;

import org.jooq.XML;
import persistence.repository.impl.ProcessInfoRepository;

public class ProcessInfoService {
    private final ProcessInfoRepository repository;

    public ProcessInfoService(ProcessInfoRepository processInfoRepository) {
        this.repository = processInfoRepository;
    }

    public XML getBpmnFile(String processName) {
        return repository.getBpmnFileByProcessName(processName);
    }
}
