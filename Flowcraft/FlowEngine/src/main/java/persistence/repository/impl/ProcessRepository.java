package persistence.repository.impl;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import persistence.DatabaseConfig;

public class ProcessRepository {
    private final DSLContext context;

    public ProcessRepository() {
        this.context = DatabaseConfig.getContext();
    }

    public String getProcessName() {
        return new String("Process");
    }
}
