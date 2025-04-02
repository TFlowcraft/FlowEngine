package persistence.repository.impl;

import org.jooq.DSLContext;
import org.jooq.XML;
import persistence.DatabaseConfig;
import static com.database.entity.generated.tables.ProcessInfo.PROCESS_INFO;

public class ProcessInfoRepository {
    private final DSLContext context;

    public ProcessInfoRepository() {
        this.context = DatabaseConfig.getContext();
    }

    public XML getBpmnFileByProcessName(String name) {
        return context
                .select(PROCESS_INFO.BPMN_FILE)
                .from(PROCESS_INFO)
                .where(PROCESS_INFO.PROCESSNAME.eq(name))
                .fetchOneInto(XML.class);
    }
}
