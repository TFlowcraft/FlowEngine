package engine;

public interface TaskDelegate {
    void execute(String businessData);
    void rollback();
}
