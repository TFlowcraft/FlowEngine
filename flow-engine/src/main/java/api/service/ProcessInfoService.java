package api.service;

import api.dto.ProcessInfoDto;
import com.database.entity.generated.tables.pojos.ProcessInfo;
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

    public List<ProcessInfoDto> getAllProcessInfos() {
        return repository.getAllProcesses();
    }
}
