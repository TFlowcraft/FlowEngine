package engine.common;

import engine.model.ExecutionContext;

public interface TaskDelegate {
    void execute(ExecutionContext context);
    void rollback(ExecutionContext context);
}
