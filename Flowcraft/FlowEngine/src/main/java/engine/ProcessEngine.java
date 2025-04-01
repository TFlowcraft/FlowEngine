package engine;

import com.database.entity.generated.tables.pojos.InstanceTasks;
import engine.common.TaskDelegate;
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
    private final Map<String, BpmnElement> bpmnProcess;
    private final ProcessInstanceRepository processInstanceRepository;
    private final TaskRepository taskRepository;
    private final ProcessPoller processPoller;
    private final TaskExecutor taskExecutor;

    public ProcessEngine(Map<String, BpmnElement> bpmnProcess, List<TaskDelegate> userTaskImplementation, ProcessInstanceRepository processInstanceRepository, TaskRepository taskRepository, ProcessPoller processPoller, TaskExecutor taskExecutor) {
        this.bpmnProcess = bpmnProcess;
        this.processInstanceRepository = processInstanceRepository;
        this.taskRepository = taskRepository;
        this.processPoller = processPoller;
        this.taskExecutor = taskExecutor;
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
        return Map.copyOf(bpmnProcess);
    }

    public static class ProcessEngineConfigurator {
        private InputStream inputStream;
        private List<TaskDelegate> userTaskImplementation;
        private BlockingQueue<InstanceTasks> engineQueue;
        private ProcessInstanceRepository processInstanceRepository;
        private TaskRepository taskRepository;
        private int poolSize;
        private int retriesAmount;

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

        public ProcessEngineConfigurator setRetriesAmount(int retriesAmount) {
            this.retriesAmount = retriesAmount;
            return this;
        }

        public ProcessEngineConfigurator setPoolSize(int size) {
            poolSize = size;
            return this;
        }

        public ProcessEngine build() throws ParserConfigurationException, IOException, SAXException {
            var parserResult = BpmnParser.parseFile(inputStream, new ArrayList<>());
            ProcessPoller processPoller = new ProcessPoller(engineQueue, taskRepository, new ScheduledThreadPoolExecutor(poolSize));
            TaskExecutor taskExecutor = new TaskExecutor(engineQueue, poolSize, processInstanceRepository, taskRepository, parserResult.delegates(), retriesAmount);
            return new ProcessEngine(parserResult.elements(), userTaskImplementation, processInstanceRepository, taskRepository, processPoller, taskExecutor);
        }
    }
}
