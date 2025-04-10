package engine.common;

import engine.model.BpmnElement;

import java.util.*;
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

    public List<String> getIncomingElementsId(String elementId) {
        List<BpmnElement> elements = getIncomingElements(elementId);
        List<String> result = new ArrayList<>(elements.size());
        for (BpmnElement element : elements) {
            result.add(element.getId());
        }
        return result;
    }

    public GatewayType getGatewayType(String gatewayId) {
        Optional<BpmnElement> elementOpt = getElementById(gatewayId);
        if (elementOpt.isEmpty()) {
            return GatewayType.UNSUPPORTED;
        }

        BpmnElement element = elementOpt.get();
        String type = element.getType();

        if (type.contains("Exclusive")) {
            return GatewayType.EXCLUSIVE;
        }
        if (type.contains("Parallel")) {
            return GatewayType.PARALLEL;
        }
        return GatewayType.UNSUPPORTED;
    }

    public List<BpmnElement> getIncomingElements(String elementId) {
        return resolveElementReferences(elementId, false);
    }

    public List<BpmnElement> processParallelGateway(String gatewayId) {
        return getOutgoingElements(gatewayId);
    }

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
        return  getElementById(elementId).isPresent() ? getElementById(elementId).get().getType() : "";
    }
}