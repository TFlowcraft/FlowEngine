package engine.common;

public interface TaskDelegate {
    void execute(ExecutionContext context);
    void rollback(ExecutionContext context);
}
