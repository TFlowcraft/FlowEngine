package api.service;

import api.dto.ProcessInfoDto;
import org.jooq.XML;
import persistence.repository.impl.ProcessInfoRepository;
import java.util.List;


public class ProcessInfoService {
    private final ProcessInfoRepository repository;

    public ProcessInfoService(ProcessInfoRepository processInfoRepository) {
        this.repository = processInfoRepository;
    }

    public XML getBpmnFile(String processName) {
        return repository.getBpmnFileByProcessName(processName);
    }

    public List<ProcessInfoDto> getAllProcessesInfo() {
        return repository.getAllProcesses();
    }
}
