package com.github.flowengine.springboot.autoconfigure;

import engine.ProcessEngine;
import engine.common.TaskDelegate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
@ConditionalOnClass(ProcessEngine.class)
@EnableConfigurationProperties(FlowEngineProperties.class)
public class FlowEngineAutoConfiguration {

    private final FlowEngineProperties properties;
    private List<TaskDelegate> taskDelegates = Collections.emptyList();

    public FlowEngineAutoConfiguration(FlowEngineProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessEngine processEngine() throws Exception {
        Map<String, TaskDelegate> explicitMappings = new HashMap<>();
        List<TaskDelegate> orderedDelegates = new ArrayList<>();

        for (TaskDelegate delegate : taskDelegates) {
            FlowTaskDelegate ann = delegate.getClass().getAnnotation(FlowTaskDelegate.class);
            if (ann != null && ann.value().length > 0) {
                for (String bpmnId : ann.value()) {
                    explicitMappings.put(bpmnId, delegate);
                }
            } else {
                orderedDelegates.add(delegate);
            }
        }
        return new ProcessEngine.ProcessEngineConfigurator()
                .setBpmnProcessFile(properties.getBpmnFile())
                .setHikariPoolDbSettings(
                        properties.getDbUrl(),
                        properties.getDbUser(),
                        properties.getDbPassword()
                )
                .setHikariPoolSettings(
                        properties.getPoolSize(),
                        properties.getConnectionTimeoutMs(),
                        properties.getIdleTimeoutMs(),
                        properties.getMaxLifetimeMs()
                )
                .setUserTaskImplementation(orderedDelegates)
                .setExplicitMappings(explicitMappings)
                .setRetriesAmount(properties.getRetries())
                .setProcessTaskAmount(properties.getProcessTaskAmount())
                .setApiPort(8080)
                .build();
    }

    @Bean
    @ConditionalOnWebApplication
    public Object engineInitializer(ProcessEngine engine) {
        return (ApplicationRunner) args -> engine.start();
    }
}