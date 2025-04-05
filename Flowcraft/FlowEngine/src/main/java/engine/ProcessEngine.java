package engine;

import api.controller.ControllerSetup;
import api.controller.impl.HistoryController;
import api.controller.impl.ProcessInstanceController;
import api.service.HistoryService;
import api.service.ProcessInfoService;
import api.service.ProcessInstanceService;
import com.database.entity.generated.tables.pojos.InstanceTasks;
import engine.common.ProcessNavigator;
import engine.common.TaskDelegate;
import engine.executor.TaskExecutor;
import engine.model.BpmnElement;
import engine.parser.BpmnParser;
import io.javalin.Javalin;
import org.jooq.JSONB;
import org.xml.sax.SAXException;
import persistence.DatabaseConfig;
import persistence.poller.ProcessPoller;
import persistence.repository.impl.HistoryRepository;
import persistence.repository.impl.ProcessInfoRepository;
import persistence.repository.impl.ProcessInstanceRepository;
import persistence.repository.impl.TaskRepository;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ProcessEngine {
    private final Map<String, BpmnElement> bpmnProcess;
    private final ProcessInstanceRepository processInstanceRepository;
    private final TaskRepository taskRepository;
    private final ProcessPoller processPoller;
    private final TaskExecutor taskExecutor;
    private final ProcessNavigator processNavigator;
    private final Javalin app;


    public ProcessEngine(Map<String, BpmnElement> bpmnProcess, ProcessInstanceRepository processInstanceRepository, TaskRepository taskRepository, ProcessPoller processPoller, TaskExecutor taskExecutor, Javalin app) {
        this.bpmnProcess = bpmnProcess;
        this.processInstanceRepository = processInstanceRepository;
        this.taskRepository = taskRepository;
        this.processPoller = processPoller;
        this.taskExecutor = taskExecutor;
        processNavigator = new ProcessNavigator(bpmnProcess);
        this.app = app;
    }

    public void createProcessInstance(JSONB businessData) {
        UUID id = processInstanceRepository.createNew(businessData);
        var startEvent = processNavigator.findElementByType("startEvent").get();
        var outputEvent = processNavigator.getOutgoingElements(startEvent.getId());
        for (var el : outputEvent) {
            taskRepository.createTaskForInstance(id, el.getId());
        }
    }

    public void shutdown() {
        app.stop();
    }

    public void start() {
        processPoller.start();
        taskExecutor.start();
    }

    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }


    public static class ProcessEngineConfigurator {
        private InputStream inputStream;
        private List<TaskDelegate> userTaskImplementation;
        private int poolSize;
        private int retriesAmount;
        private int port;
        private int processTaskAmount;
        private String dbUrl;
        private String dbUser;
        private String dbPassword;

        public ProcessEngineConfigurator setBpmnProcessFile(String bpmnFile) {
            inputStream = getClass().getResourceAsStream(bpmnFile);
            return this;
        }

        public ProcessEngineConfigurator setUserTaskImplementation(List<TaskDelegate> taskImplementation) {
            userTaskImplementation = taskImplementation;
            return this;
        }

        public ProcessEngineConfigurator setHikariPoolSettings(String dbUrl, String dbUser, String dbPassword) {
            this.dbUrl = dbUrl;
            this.dbUser = dbUser;
            this.dbPassword = dbPassword;
            return this;
        }

        public ProcessEngineConfigurator setProcessTaskAmount(int processTaskAmount) {
            this.processTaskAmount = processTaskAmount;
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

        public ProcessEngineConfigurator setApiPort(int port) {
            this.port = port;
            return this;
        }

        private Javalin createEngineAPI(int port, ControllerSetup ... controllers) {
            Javalin app = Javalin.create().start(port);
            for (var controller : controllers) {
                controller.registerEndpoints(app);
            }
            System.out.printf("Server started on http://localhost:%d\n", port);
            return app;
        }

        public ProcessEngine build() throws ParserConfigurationException, IOException, SAXException {
            DatabaseConfig.setupConfig(dbUrl, dbUser, dbPassword);
            var parserResult = BpmnParser.parseFile(inputStream, userTaskImplementation);
            var engineQueue = new ArrayBlockingQueue<InstanceTasks>(processTaskAmount);
            var taskRepository = new TaskRepository();
            var processInstanceRepository = new ProcessInstanceRepository();
            ProcessPoller processPoller = new ProcessPoller(engineQueue, taskRepository, new ScheduledThreadPoolExecutor(poolSize));
            TaskExecutor taskExecutor = new TaskExecutor(engineQueue, poolSize, processInstanceRepository, taskRepository, parserResult.delegates(), retriesAmount);
            HistoryController ControllerHistory = new HistoryController(new HistoryService(new HistoryRepository()));
            ProcessInstanceController ControllerProcessInstance = new ProcessInstanceController(new ProcessInstanceService(processInstanceRepository),
                    new ProcessInfoService(new ProcessInfoRepository()));
            Javalin app = createEngineAPI(port, ControllerHistory, ControllerProcessInstance);
            return new ProcessEngine(parserResult.elements(), processInstanceRepository, taskRepository, processPoller, taskExecutor,app);
        }
    }
}
