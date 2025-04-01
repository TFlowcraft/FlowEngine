import engine.common.TaskDelegate;
import engine.model.BpmnElement;
import engine.model.ExecutionContext;
import engine.parser.BpmnParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

class BpmnParserTest {
    private Map<String, BpmnElement> parsedElements;

    @ParameterizedTest
    @ValueSource(strings = {"/test2.bpmn"})
    public void parseScheme(String path) {
        try {
            var list = new ArrayList<TaskDelegate>();
            for (int i = 0 ; i < 3 ; i++) {
                list.add(new TaskDelegate() {
                    @Override
                    public void execute(ExecutionContext context) {

                    }

                    @Override
                    public void rollback(ExecutionContext context) {

                    }
                });
            }

            var result = BpmnParser.parseFile(getClass().getResourceAsStream(path), list);
            var delegates = result.delegates();
            delegates.forEach((id, element) -> System.out.println("ID: " + id + ", Element: " + element));
//            List<BpmnElement> values = scheme.values().stream().toList();
//            for (BpmnElement bpmnElement : values) {
//                System.out.println(bpmnElement.toString());
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
// @BeforeEach
//  void setUp() throws ParserConfigurationException, IOException, org.xml.sax.SAXException {
//        // Загрузка файла из classpath
//        ClassLoader classLoader = getClass().getClassLoader();
//       URL resource = classLoader.getResource(TEST_BPMN_FILE);
//       assertNotNull(resource, "Тестовый файл BPMN не найден: " + TEST_BPMN_FILE);
//      File file = new File(resource.getFile());
//       parsedElements = BpmnParser.parseFile(TEST_BPMN_FILE);
//    }
//
//    @Test
//    @DisplayName("Парсинг файла должен возвращать непустую карту элементов")
//    void testParseFileReturnsNonEmptyMap() {
//        assertNotNull(parsedElements);
//        assertFalse(parsedElements.isEmpty());
//    }
//
//    @Test
//    @DisplayName("Парсинг должен корректно обрабатывать StartEvent")
//    void testStartEventParsing() {
//        BpmnElement startEvent = parsedElements.get("StartEvent_1c9r4x9");
//        assertNotNull(startEvent);
//        assertEquals("StartEvent_1c9r4x9", startEvent.getId());
//        assertEquals("startEvent", startEvent.getType());
//        assertEquals(0, startEvent.getIncoming().size());
//        assertEquals(1, startEvent.getOutgoing().size());
//        assertEquals("Activity_1q0x9c0", startEvent.getOutgoing().getFirst());
//    }
//
//    @Test
//    @DisplayName("Парсинг должен корректно обрабатывать ServiceTask")
//    void testServiceTaskParsing() {
//        BpmnElement serviceTask = parsedElements.get("Activity_1q0x9c0");
//        assertNotNull(serviceTask);
//        assertEquals("Activity_1q0x9c0", serviceTask.getId());
//        assertEquals("serviceTask", serviceTask.getType());
//        assertEquals("CheckInfo", serviceTask.getName());
//        assertEquals(1, serviceTask.getIncoming().size());
//        assertEquals(1, serviceTask.getOutgoing().size());
//        assertEquals("StartEvent_1c9r4x9", serviceTask.getIncoming().getFirst());
//        assertEquals("Gateway_1sddnjb", serviceTask.getOutgoing().getFirst());
//    }
//
//    @Test
//    @DisplayName("Парсинг должен корректно обрабатывать ExclusiveGateway")
//    void testExclusiveGatewayParsing() {
//        BpmnElement gateway = parsedElements.get("Gateway_1sddnjb");
//        assertNotNull(gateway);
//        assertEquals("Gateway_1sddnjb", gateway.getId());
//        assertEquals("exclusiveGateway", gateway.getType());
//        assertEquals(1, gateway.getIncoming().size());
//        assertEquals(3, gateway.getOutgoing().size());
//        assertTrue(gateway.getOutgoing().containsAll(List.of("Activity_1a8bg9g", "Activity_12cmqxr", "Activity_1hc4pkq")));
//    }
//
//    @Test
//    @DisplayName("Парсинг должен корректно обрабатывать EndEvent")
//    void testEndEventParsing() {
//        BpmnElement endEvent = parsedElements.get("Event_0x8ivw4");
//        assertNotNull(endEvent);
//        assertEquals("Event_0x8ivw4", endEvent.getId());
//        assertEquals("endEvent", endEvent.getType());
//        assertEquals(1, endEvent.getIncoming().size());
//        assertEquals(0, endEvent.getOutgoing().size());
//        assertEquals("Gateway_0z3oqy5", endEvent.getIncoming().getFirst());
//    }
//
//    @Test
//    @DisplayName("Парсинг должен корректно обрабатывать SequenceFlow")
//    void testSequenceFlowResolution() {
//        BpmnElement activity1 = parsedElements.get("Activity_1a8bg9g");
//        BpmnElement activity2 = parsedElements.get("Activity_12cmqxr");
//        BpmnElement activity3 = parsedElements.get("Activity_1hc4pkq");
//
//        assertAll(
//                () -> assertEquals("Gateway_1sddnjb", activity1.getIncoming().getFirst()),
//                () -> assertEquals("Gateway_0z3oqy5", activity1.getOutgoing().getFirst()),
//                () -> assertEquals("Gateway_1sddnjb", activity2.getIncoming().getFirst()),
//                () -> assertEquals("Gateway_0z3oqy5", activity2.getOutgoing().getFirst()),
//                () -> assertEquals("Gateway_1sddnjb", activity3.getIncoming().getFirst()),
//                () -> assertEquals("Gateway_0z3oqy5", activity3.getOutgoing().getFirst())
//        );
//    }
}