package engine.common;

import engine.model.BpmnElement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProcessNavigator {
    private final Map<String, BpmnElement> elements;

    public ProcessNavigator(Map<String, BpmnElement> elements) {
        this.elements = Collections.unmodifiableMap(elements);
    }


    public List<BpmnElement> getOutgoingElements(String elementId) {
        return resolveElementReferences(elementId, true);
    }

    public Optional<BpmnElement> getSingleOutgoingElement(String elementId) {
        List<BpmnElement> outgoing = getOutgoingElements(elementId);
        return outgoing.size() == 1
                ? Optional.of(outgoing.getFirst())
                : Optional.empty();
    }

   public Optional<BpmnElement> findElementByType(String type) {
        for (BpmnElement element : elements.values()) {
            if (element.getType().equals(type)) {
                return Optional.of(element);
            }
        }
        return Optional.empty();
   }

    public List<BpmnElement> getIncomingElements(String elementId) {
        return resolveElementReferences(elementId, false);
    }

    public List<String> getIncomingElementsId(String elementId) {
        return getIncomingElements(elementId)
                .stream()
                .map(bpmnElement -> bpmnElement.getId().toLowerCase())
                .toList();
    }

    public GatewayType getGatewayType(String gatewayId) {
        return getElementById(gatewayId)
                .map(element -> {
                    String type = element.getType();
                    if (type.contains("Exclusive")) return GatewayType.EXCLUSIVE;
                    if (type.contains("Parallel")) return GatewayType.PARALLEL;
                    return GatewayType.UNSUPPORTED;
                })
                .orElse(GatewayType.UNSUPPORTED);
    }

    public Optional<BpmnElement> processExclusiveGateway(
            String gatewayId,
            Map<String, Object> processVariables
    ) {
        // Заглушка для реализации выбора пути
        return getSingleOutgoingElement(gatewayId);
    }

    public List<BpmnElement> processParallelGateway(String gatewayId) {
        // Все исходящие пути для параллельного шлюза
        return getOutgoingElements(gatewayId);
    }

    // Вспомогательные методы

    private List<BpmnElement> resolveElementReferences(String elementId, boolean isOutgoing) {
        return getElementById(elementId)
                .map(element -> {
                    List<String> refIds = isOutgoing
                            ? element.getOutgoing()
                            : element.getIncoming();

                    return refIds.stream()
                            .map(this::getElementById)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                })
                .orElse(Collections.emptyList());
    }

    public Optional<BpmnElement> getElementById(String elementId) {
        return Optional.ofNullable(elements.get(elementId));
    }

    public String getElementTypeById(String elementId) {
        return  getElementById(elementId).isPresent() ? getElementById(elementId).get().getType() : null;
    }



}