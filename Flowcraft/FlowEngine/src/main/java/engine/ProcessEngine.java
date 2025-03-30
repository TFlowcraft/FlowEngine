package engine;

import com.database.entity.generated.tables.pojos.InstanceTasks;
import engine.executor.TaskExecutor;
import engine.model.BpmnElement;
import engine.parser.BpmnParser;
import org.jooq.JSONB;
import org.xml.sax.SAXException;
import persistence.poller.ProcessPoller;
import persistence.repository.impl.ProcessInstanceRepository;
import persistence.repository.impl.TaskRepository;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ProcessEngine {
    //Тут поллер, воркеры, очередь и тд + обработчики элементов?
    private final Map<String, BpmnElement> bpmnProcess;
    private final List<TaskDelegate> userTaskImplementation;
    private final ProcessInstanceRepository processInstanceRepository;
    private final TaskRepository taskRepository;
    private final Map<String, Integer> tasksIdToUserImplIndex;
    private final ProcessPoller processPoller;
    private final TaskExecutor taskExecutor;

    public ProcessEngine(Map<String, BpmnElement> bpmnProcess, List<TaskDelegate> userTaskImplementation, ProcessInstanceRepository processInstanceRepository, TaskRepository taskRepository, ProcessPoller processPoller, TaskExecutor taskExecutor) {
        this.bpmnProcess = bpmnProcess;
        this.userTaskImplementation = userTaskImplementation;
        this.processInstanceRepository = processInstanceRepository;
        this.taskRepository = taskRepository;
        this.processPoller = processPoller;
        this.taskExecutor = taskExecutor;
        taskExecutor.setProcessEngine(this);
        tasksIdToUserImplIndex = mapTaskIdsToUserTaskImplementationIndexes();
    }

    public void createProcessInstance(JSONB businessData) {
        UUID id = processInstanceRepository.createNew(businessData);
        //хард код и вообще тут отдельный метод должен быть чтоб либо первое движение сделать либо отдельный сервис
        var entry = getEntryByContainType("startEvent");
        var next = entry != null ? entry.getValue().getOutgoing() : null;
        for (var outgoing : next) {
            taskRepository.createTaskForInstance(id, outgoing);
        }
    }

    private Map.Entry<String, BpmnElement> getEntryByContainType(String type) {
        for (Map.Entry<String, BpmnElement> entry : bpmnProcess.entrySet()) {
            if (entry.getValue().getType().equals(type)) {
                return entry;
            }
        }
        return null;
    }

    public void start() {
        processPoller.start();
        taskExecutor.start();
    }

    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
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
                    throw new IllegalStateException("Недостаточно имплементаций userTask. Ожидалось не менее "
                            + (index + 1) + " элементов, а найдено " + userTaskImplementation.size());
                }
                mapping.put(entry.getKey(), index);
                index++;
            }
        }
        return mapping;
    }

    public static class ProcessEngineConfigurator {
        private InputStream inputStream;
        private List<TaskDelegate> userTaskImplementation;
        private BlockingQueue<InstanceTasks> engineQueue;
        private ProcessPoller processPoller;
        private TaskExecutor taskExecutor;
        private ProcessInstanceRepository processInstanceRepository;
        private TaskRepository taskRepository;
        private int poolSize;

        public ProcessEngineConfigurator setBpmnProcessFile(String bpmnFile) {
            inputStream = getClass().getResourceAsStream(bpmnFile);
            return this;
        }

        public ProcessEngineConfigurator setUserTaskImplementation(List<TaskDelegate> taskImplementation) {
            userTaskImplementation = taskImplementation;
            return this;
        }

        public ProcessEngineConfigurator setProcessInstanceRepository(ProcessInstanceRepository processInstanceRepository) {
            this.processInstanceRepository = processInstanceRepository;
            return this;
        }

        public ProcessEngineConfigurator setTaskRepository(TaskRepository taskRepository) {
            this.taskRepository = taskRepository;
            return this;
        }

        public ProcessEngineConfigurator setEngineQueue(BlockingQueue<InstanceTasks> engineQueue) {
            this.engineQueue = engineQueue;
            return this;
        }

        public ProcessEngineConfigurator setPoolSize(int size) {
            poolSize = size;
            return this;
        }

        public ProcessEngine build() throws ParserConfigurationException, IOException, SAXException {
            var processMap = BpmnParser.parseFile(inputStream);
            processPoller = new ProcessPoller(engineQueue, taskRepository, new ScheduledThreadPoolExecutor(poolSize));
            taskExecutor = new TaskExecutor(engineQueue, poolSize, taskRepository, null);
            return new ProcessEngine(processMap, userTaskImplementation, processInstanceRepository, taskRepository, processPoller, taskExecutor);
        }
    }
    //тут наверное определить общие методы для движка
}
