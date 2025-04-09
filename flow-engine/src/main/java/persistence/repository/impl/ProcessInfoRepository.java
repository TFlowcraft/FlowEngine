package persistence.repository.impl;

import api.dto.ProcessInfoDto;
import com.database.entity.generated.Public;
import com.database.entity.generated.tables.pojos.ProcessInfo;
import com.database.entity.generated.tables.records.ProcessInfoRecord;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.XML;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.database.entity.generated.tables.ProcessInfo.PROCESS_INFO;

public class ProcessInfoRepository {
    private final DSLContext context;

    public ProcessInfoRepository(DSLContext context) {
        this.context = context;
    }

    public XML getBpmnFileByProcessName(String name) {
        return context
                .select(PROCESS_INFO.BPMN_FILE)
                .from(PROCESS_INFO)
                .where(PROCESS_INFO.PROCESS_NAME.eq(name))
                .fetchOneInto(XML.class);
    }

    public UUID insertProcessInfo(String bpmnProcessId, String processName, String xmlContent) {
        ProcessInfoRecord record = context.newRecord(PROCESS_INFO);
        record.setProcessName(processName);
        record.setBpmnProcessId(bpmnProcessId);
        record.setBpmnFile(XML.valueOf(xmlContent));
        record.store();
        return record.getId();
    }

    public List<ProcessInfoDto> getAllProcesses() {
        return context
                .selectFrom(PROCESS_INFO)
                .fetchInto(ProcessInfoDto.class);
    }

    public ProcessInfo getProcessByProcessId(UUID processId) {
        return context.selectFrom(PROCESS_INFO)
                .where(PROCESS_INFO.ID.eq(processId))
                .fetchOneInto(ProcessInfo.class);
    }
}
