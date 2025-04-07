package persistence.repository.impl;

import com.database.entity.generated.tables.pojos.ProcessInfo;
import org.jooq.DSLContext;
import org.jooq.XML;
import java.util.List;

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
                .where(PROCESS_INFO.PROCESSNAME.eq(name))
                .fetchOneInto(XML.class);
    }

    public List<ProcessInfo> getAllProcesses() {
        return context
                .selectFrom(PROCESS_INFO)
                .fetchInto(ProcessInfo.class);
    }
}
