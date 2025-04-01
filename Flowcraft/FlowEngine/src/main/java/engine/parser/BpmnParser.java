package engine.parser;



import engine.common.TaskDelegate;
import engine.model.BpmnElement;
import engine.model.FlowInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class BpmnParser {
    private static final String BPMN_MODEL_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";
    private static final String FACTORY_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";

    private BpmnParser() {}

    private enum Direction {
        SOURCE, TARGET
    }

    public static BpmnParseResult parseFile(InputStream inputStream, List<TaskDelegate> delegates)
            throws ParserConfigurationException, IOException, org.xml.sax.SAXException {

        Map<String, BpmnElement> elementMap = new HashMap<>();
        Map<String, FlowInfo> flowMap = new HashMap<>();
        Map<String, TaskDelegate> delegateMap = new HashMap<>();

        Document document = initializeDocumentBuilder().parse(inputStream);
        document.getDocumentElement().normalize();

        NodeList elementsNodeList = document.getElementsByTagName("*");
        List<Element> processElements = new ArrayList<>();

        processSequenceFlows(elementsNodeList, flowMap);
        collectProcessElements(elementsNodeList, processElements);

        linkDelegates(processElements, delegates, delegateMap, elementMap, flowMap);

        return new BpmnParseResult(
                Collections.unmodifiableMap(elementMap),
                Collections.unmodifiableMap(delegateMap)
        );
    }

    private static void collectProcessElements(NodeList nodeList, List<Element> processElements) {
        processElements.clear();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (isProcessElement(element)) {
                    processElements.add(element);
                }
            }
        }
    }

    private static boolean isProcessElement(Element element) {
        String localName = element.getLocalName();
        return isBpmnElement(element) &&
                !"sequenceFlow".equals(localName) &&
                !"definitions".equals(localName) &&
                !localName.toLowerCase().contains("event");
    }

    private static void linkDelegates(List<Element> processElements,
                                      List<TaskDelegate> delegates,
                                      Map<String, TaskDelegate> delegateMap,
                                      Map<String, BpmnElement> elementMap,
                                      Map<String, FlowInfo> flowMap) {
        int delegateIndex = 0;
        int taskCount = 0;

        for (Element element : processElements) {
            if (isTaskElement(element)) {
                taskCount++;
            }
        }

        if (taskCount != delegates.size()) {
            throw new IllegalStateException("Количество делегатов (" + delegates.size() +
                    ") не соответствует количеству задач в BPMN (" + taskCount + ")");
        }

        for (Element element : processElements) {
            String id = element.getAttribute("id");
            if (id.isEmpty()) continue;

            BpmnElement bpmnElement = createBpmnElement(element, flowMap);
            elementMap.put(id, bpmnElement);

            if (isTaskElement(element)) {
                delegateMap.put(id, delegates.get(delegateIndex));
                delegateIndex++;
            }
        }
    }

    private static boolean isTaskElement(Element element) {
        String localName = element.getLocalName();
        return localName.contains("task");
    }

    private static void processSequenceFlows(NodeList nodeList, Map<String, FlowInfo> flowMap) {
        processElements(nodeList,
                element -> isBpmnElement(element) && "sequenceFlow".equals(element.getLocalName()),
                element -> flowMap.put(
                        element.getAttribute("id"),
                        new FlowInfo(
                                element.getAttribute("sourceRef"),
                                element.getAttribute("targetRef")
                        )
                )
        );
    }

    private static BpmnElement createBpmnElement(Element element, Map<String, FlowInfo> flowMap) {
        return new BpmnElement(
                element.getAttribute("id"),
                element.getAttribute("name"),
                element.getLocalName(),
                resolveFlowReferences(getIncomingOrOutgoing(element, "incoming"), flowMap, Direction.SOURCE),
                resolveFlowReferences(getIncomingOrOutgoing(element, "outgoing"), flowMap, Direction.TARGET)
        );
    }

    private static List<String> getIncomingOrOutgoing(Element element, String tagName) {
        List<String> result = new ArrayList<>(2);
        NodeList nodeList = element.getElementsByTagNameNS(BPMN_MODEL_NAMESPACE, tagName);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                result.add(node.getTextContent().trim());
            }
        }
        return Collections.unmodifiableList(result);
    }

    private static boolean isBpmnElement(Element element) {
        return BPMN_MODEL_NAMESPACE.equals(element.getNamespaceURI());
    }

    private static List<String> resolveFlowReferences(List<String> flowIds, Map<String, FlowInfo> flowMap, Direction direction) {
        List<String> references = new ArrayList<>(flowIds.size());
        flowIds.forEach(flowId -> {
            FlowInfo flow = flowMap.get(flowId);
            if (flow != null) references.add(direction == Direction.TARGET ? flow.targetRef() : flow.sourceRef());
        });
        return Collections.unmodifiableList(references);
    }

    private static DocumentBuilder initializeDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        factory.setFeature(FACTORY_FEATURE, true);
        return factory.newDocumentBuilder();
    }

    private static void processElements(NodeList nodeList, Predicate<Element> filter, Consumer<Element> processor) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (filter.test(element)) processor.accept(element);
            }
        }
    }

    public record BpmnParseResult(Map<String, BpmnElement> elements, Map<String, TaskDelegate> delegates) {
    }
}
