import engine.parser.BpmnParser;


public class Test {
    public static void main(String[] args) {
        try {
            var scheme = BpmnParser.parseFile("C:\\Users\\degl\\Downloads\\Telegram Desktop\\diagram.bpmn");
            scheme.forEach((id, element) -> System.out.println("ID: " + id + ", Element: " + element));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
