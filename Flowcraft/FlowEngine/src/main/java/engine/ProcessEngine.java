package engine;

import engine.model.BpmnElement;
import engine.parser.BpmnParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ProcessEngine {
    //Тут поллер, воркеры, очередь и тд + обработчики элементов?
    private final Map<String, BpmnElement> bpmnProcess;
    private final List<TaskDelegate> userTaskImplementation;

    public ProcessEngine(Map<String, BpmnElement> bpmnProcess, List<TaskDelegate> userTaskImplementation) {
        this.bpmnProcess = bpmnProcess;
        this.userTaskImplementation = userTaskImplementation;
    }

    public Map<String, BpmnElement> getBpmnProcess() {
        //ConcurrentHashMap?
        return Map.copyOf(bpmnProcess);
    }

    public List<TaskDelegate> getUserTaskImplementation() {
        return List.copyOf(userTaskImplementation);
    }

    public static class ProcessEngineConfigurator {
        private String filePath;
        private List<TaskDelegate> userTaskImplementation;

        public ProcessEngineConfigurator setBpmnProcessFile(String bpmnFile) {
            filePath = bpmnFile;
            return this;
        }

        public ProcessEngineConfigurator setUserTaskImplementation(List<TaskDelegate> taskImplementation) {
            userTaskImplementation = taskImplementation;
            return this;
        }

        public ProcessEngine build() throws ParserConfigurationException, IOException, SAXException {
            var processMap = BpmnParser.parseFile(filePath);
            return new ProcessEngine(processMap, userTaskImplementation);
        }
    }
    //тут наверное определить общие методы для движка
}
