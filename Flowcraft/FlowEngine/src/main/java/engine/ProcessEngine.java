package engine;

import engine.model.BpmnElement;
import engine.parser.BpmnParser;
import org.jooq.JSON;
import org.jooq.JSONB;
import org.xml.sax.SAXException;
import persistence.repository.impl.ProcessInstanceRepository;
import persistence.repository.impl.TaskRepository;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessEngine {
    //Тут поллер, воркеры, очередь и тд + обработчики элементов?
    private final Map<String, BpmnElement> bpmnProcess;
    private final List<TaskDelegate> userTaskImplementation;
    private final ProcessInstanceRepository processInstanceRepository;
    private final TaskRepository taskRepository;
    private final Map<String, Integer> tasksIdToUserImplIndex;

    public ProcessEngine(Map<String, BpmnElement> bpmnProcess, List<TaskDelegate> userTaskImplementation, ProcessInstanceRepository processInstanceRepository, TaskRepository taskRepository) {
        this.bpmnProcess = bpmnProcess;
        this.userTaskImplementation = userTaskImplementation;
        this.processInstanceRepository = processInstanceRepository;
        this.taskRepository = taskRepository;
        tasksIdToUserImplIndex = mapTaskIdsToUserTaskImplementationIndexes();
    }

    public void createProcessInstance(JSONB businessData) {
        processInstanceRepository.createNew(businessData);

    }

    public Map<String, BpmnElement> getBpmnProcess() {
        //ConcurrentHashMap?
        return Map.copyOf(bpmnProcess);
    }

    public TaskDelegate getUserTaskImplementation(String elementId) {
        return userTaskImplementation.get(tasksIdToUserImplIndex.get(elementId));
    }

    public Map<String, Integer> mapTaskIdsToUserTaskImplementationIndexes() {
        Map<String, Integer> mapping = new HashMap<>();
        int index = 0;
        for (Map.Entry<String, BpmnElement> entry : bpmnProcess.entrySet()) {
            BpmnElement element = entry.getValue();
            if (element.getType() != null && element.getType().toLowerCase().contains("task")) {
                if (index >= userTaskImplementation.size()) {
                    throw new IllegalStateException("Недостаточно имплементаций userTask. Ожидалось не менее " + (index + 1) + " элементов, а найдено " + userTaskImplementation.size());
                }
                mapping.put(entry.getKey(), index);
                index++;
            }
        }
        return mapping;
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
            return new ProcessEngine(processMap, userTaskImplementation, new ProcessInstanceRepository(), new TaskRepository());
        }
    }
    //тут наверное определить общие методы для движка
}
