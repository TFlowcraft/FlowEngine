package engine;

import api.controller.ControllerSetup;
import api.controller.impl.HistoryController;
import api.controller.impl.ProcessController;
import api.controller.impl.ProcessInstanceController;
import api.controller.impl.TaskController;
import api.service.*;
import com.database.entity.generated.tables.pojos.InstanceTasks;
import engine.common.ProcessNavigator;
import engine.common.TaskDelegate;
import engine.executor.TaskExecutor;
import engine.model.BpmnElement;
import engine.parser.BpmnParser;
import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;
import org.jooq.DSLContext;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ProcessEngine {
    private static Javalin app;

    private final Map<String, BpmnElement> bpmnProcess;
    private final ProcessInstanceRepository processInstanceRepository;
    private final TaskRepository taskRepository;
    private final ProcessPoller processPoller;
    private final TaskExecutor taskExecutor;
    private final ProcessNavigator processNavigator;
    private final ProcessInfoRepository processInfoRepository;


    public ProcessEngine(Map<String, BpmnElement> bpmnProcess, ProcessInstanceRepository processInstanceRepository, TaskRepository taskRepository, ProcessPoller processPoller, TaskExecutor taskExecutor, ProcessInfoRepository processInfoRepository) {
        this.bpmnProcess = bpmnProcess;
        this.processInstanceRepository = processInstanceRepository;
        this.taskRepository = taskRepository;
        this.processPoller = processPoller;
        this.taskExecutor = taskExecutor;
        processNavigator = new ProcessNavigator(bpmnProcess);
        this.processInfoRepository = processInfoRepository;
    }

    public void createProcessInstance(UUID processId, Map<String, Object> businessData) {
        UUID processInstanceId = processInstanceRepository.insertProcessInstance(processId, businessData);
        BpmnElement startEvent = processNavigator.findElementByType("startEvent").orElseThrow();
        taskRepository.createTaskForInstance(processInstanceId, startEvent.getId());
    }


    public UUID createProcess(String bpmnProcessId, String processName, String xmlFileName) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(xmlFileName)) {
            if (inputStream == null) {
                Path xmlFilePath = Path.of(xmlFileName);
                if (Files.exists(xmlFilePath)) {
                    try (InputStream fileInputStream = Files.newInputStream(xmlFilePath)) {
                        String xmlContent = new String(fileInputStream.readAllBytes(), StandardCharsets.UTF_8);
                        UUID id = UUID.randomUUID();
                        return processInfoRepository.insertProcessInfo(bpmnProcessId, processName, xmlContent);
                    }
                } else {
                    throw new IOException("File not found in classpath or filesystem: " + xmlFileName);
                }
            }
            String xmlContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            UUID id = UUID.randomUUID();
            return processInfoRepository.insertProcessInfo(bpmnProcessId, processName, xmlContent);

        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Error reading the file: " + xmlFileName, e);
        }
    }


    public ProcessNavigator getProcessNavigator() {
        return processNavigator;
    }

    public void shutdown() {
        app.stop();
        processPoller.stopPolling();
        taskExecutor.shutdown();
        DatabaseConfig.closeDataSource();
    }

    public void start() {
        processPoller.start();
        taskExecutor.start();
        System.out.println("Starting process engine");
    }

    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public Map<String, BpmnElement> getBpmnProcess() {
        return bpmnProcess;
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
        private long connectionTimeoutMs;
        private long idleTimeoutMs;
        private long maxLifetimeMs;
        private boolean defaultHikariSettings = true;

        public ProcessEngineConfigurator setBpmnProcessFile(String bpmnFile) {
            inputStream = getClass().getResourceAsStream(bpmnFile);
            return this;
        }

        public ProcessEngineConfigurator setUserTaskImplementation(List<TaskDelegate> taskImplementation) {
            userTaskImplementation = taskImplementation;
            return this;
        }

        public ProcessEngineConfigurator setHikariPoolDbSettings(String dbUrl, String dbUser, String dbPassword) {
            this.dbUrl = dbUrl;
            this.dbUser = dbUser;
            this.dbPassword = dbPassword;
            return this;
        }

        public ProcessEngineConfigurator setHikariPoolSettings(int poolSize, long connectionTimeoutMs, long idleTimeoutMs, long maxLifetimeMs) {
            this.poolSize = poolSize;
            this.connectionTimeoutMs = connectionTimeoutMs;
            this.idleTimeoutMs = idleTimeoutMs;
            this.maxLifetimeMs = maxLifetimeMs;
            this.defaultHikariSettings = false;
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

        private void startApiServer(ControllerSetup ... controllers) {
            if (ProcessEngine.app == null) {
                ProcessEngine.app = Javalin.create().start(port);
                for (var controller : controllers) {
                    controller.registerEndpoints(app);
                }
            }
        }

        private void validate() {
            if (inputStream == null) throw new IllegalStateException("BPMN file must be set");
            if (dbUrl == null || dbUser == null || dbPassword == null)
                throw new IllegalStateException("Database configuration must be set");
            if (userTaskImplementation == null)
                throw new IllegalStateException("Task delegates must be set");
        }

        public ProcessEngineConfigurator useDefaults(String bpmnFilePath, List<TaskDelegate> taskDelegates) {
            this.inputStream = getClass().getResourceAsStream(bpmnFilePath);
            if (this.inputStream == null) {
                throw new IllegalArgumentException("BPMN file not found at: " + bpmnFilePath);
            }
            Dotenv dotenv = Dotenv.load();
            String dbUrl = dotenv.get("DB_URL");
            String dbUser = dotenv.get("DB_USER");
            String dbPassword = dotenv.get("DB_PASSWORD");

            this.userTaskImplementation = taskDelegates;
            this.dbUrl = dbUrl != null ? dbUrl : "jdbc:postgresql://localhost:5432/process_engine";
            this.dbUser = dbUser != null ? dbUser : "postgres";
            this.dbPassword = dbPassword != null ? dbPassword : "postgres";
            this.poolSize = 10;
            this.retriesAmount = 5;
            this.processTaskAmount = 100;
            this.port = 8080;
            return this;
        }

        private void initializeEngineApi(DSLContext context, ProcessInstanceRepository processInstanceRepository, TaskRepository taskRepository) {
            HistoryController historyController = new HistoryController(new HistoryService(new HistoryRepository(context)));
            ProcessInfoRepository processInfoRepository = new ProcessInfoRepository(context);
            ProcessInfoService processInfoService = new ProcessInfoService(processInfoRepository);
            ProcessInstanceController processInstanceController = new ProcessInstanceController(new ProcessInstanceService(processInstanceRepository),
                    processInfoService);
            ProcessController processController = new ProcessController(processInfoService);
            TaskController taskController = new TaskController(new TaskService(taskRepository));
            startApiServer(historyController, processInstanceController, taskController, processController);
        }

        private void initDbConfig() {
            if (!defaultHikariSettings) {
                DatabaseConfig.setupConfig(dbUrl, dbUser, dbPassword, poolSize, connectionTimeoutMs, idleTimeoutMs, maxLifetimeMs);
            } else {
                DatabaseConfig.setupConfig(dbUrl, dbUser, dbPassword);
            }
        }


        public ProcessEngine build() throws ParserConfigurationException, IOException, SAXException {
            validate();
            initDbConfig();
            DSLContext context = DatabaseConfig.getContext();
            var parserResult = BpmnParser.parseFile(inputStream, userTaskImplementation);
            BlockingQueue<InstanceTasks> engineQueue = new ArrayBlockingQueue<>(processTaskAmount);
            TaskRepository taskRepository = new TaskRepository(context);
            ProcessInstanceRepository processInstanceRepository = new ProcessInstanceRepository(context);
            ProcessPoller processPoller = new ProcessPoller(engineQueue, taskRepository, new ScheduledThreadPoolExecutor(poolSize));
            ProcessInfoRepository processInfoRepository = new ProcessInfoRepository(context);
            TaskExecutor taskExecutor = new TaskExecutor(engineQueue, poolSize, processInstanceRepository, taskRepository, parserResult.delegates(), retriesAmount, new ProcessNavigator(parserResult.elements()));
            initializeEngineApi(context, processInstanceRepository, taskRepository);
            return new ProcessEngine(parserResult.elements(), processInstanceRepository, taskRepository, processPoller, taskExecutor, processInfoRepository);
        }


    }
}
