package engine.model;

import java.util.List;

public class BpmnElement {
    private final String uuid;
    private final String name;
    private final String type;
    private final List<String> incoming;
    private final List<String> outgoing;

    public BpmnElement(String uuid, String name, String type, List<String> incoming, List<String> outgoing) {
        this.uuid = uuid;
        this.name = name;
        this.type = type;
        this.incoming = incoming;
        this.outgoing = outgoing;
    }

    public String getId() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<String> getIncoming() {
        return incoming;
    }

    public List<String> getOutgoing() {
        return outgoing;
    }

    @Override
    public String toString() {
        return "BpmnElement{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", incoming=" + incoming +
                ", outgoing=" + outgoing +
                '}';
    }
}
