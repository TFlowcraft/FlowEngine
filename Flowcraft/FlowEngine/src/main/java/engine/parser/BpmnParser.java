package engine.parser;



import engine.model.BpmnElement;
import engine.model.FlowInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
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

    public static Map<String, BpmnElement> parseFile(String path) throws ParserConfigurationException, IOException, org.xml.sax.SAXException {
        Map<String, BpmnElement> elementMap = new HashMap<>();
        Map<String, FlowInfo> flowMap = new HashMap<>();

        Document document = initializeDocumentBuilder().parse(new File(path));
        document.getDocumentElement().normalize();

        NodeList elementsNodeList = document.getElementsByTagName("*");

        processSequenceFlows(elementsNodeList, flowMap);
        processBpmnElements(elementsNodeList, flowMap, elementMap);

        return Collections.unmodifiableMap(elementMap);
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

    private static void processBpmnElements(NodeList nodeList, Map<String, FlowInfo> flowMap, Map<String, BpmnElement> elementMap) {
        processElements(nodeList,
                element -> isBpmnElement(element)
                        && !"sequenceFlow".equals(element.getLocalName())
                        && !"definitions".equals(element.getLocalName()),
                element -> {
                    String id = element.getAttribute("id");
                    if (id.isEmpty()) return;

                    List<String> incoming = getIncomingOrOutgoing(element, "incoming");
                    List<String> outgoing = getIncomingOrOutgoing(element, "outgoing");

                    elementMap.put(id, new BpmnElement(
                            id,
                            element.getAttribute("name"),
                            element.getLocalName(),
                            resolveFlowReferences(incoming, flowMap, Direction.SOURCE),
                            resolveFlowReferences(outgoing, flowMap, Direction.TARGET)
                    ));
                }
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
}
