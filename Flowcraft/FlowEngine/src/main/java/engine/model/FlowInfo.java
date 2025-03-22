package engine.model;

public class FlowInfo {
    private final String sourceRef;
    private final String targetRef;

    public FlowInfo(String sourceRef, String targetRef) {
        this.sourceRef = sourceRef;
        this.targetRef = targetRef;
    }

    public String getSourceRef() {
        return sourceRef;
    }

    public String getTargetRef() {
        return targetRef;
    }
}
