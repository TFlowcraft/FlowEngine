package com.github.flowengine.springboot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "flow.engine")
public class FlowEngineProperties {
    private String bpmnFile;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private int poolSize;
    private int retries;
    private int processTaskAmount;
    private long connectionTimeoutMs;
    private long idleTimeoutMs;
    private long maxLifetimeMs;

    public int getProcessTaskAmount() {
        return processTaskAmount;
    }

    public void setProcessTaskAmount(int processTaskAmount) {
        this.processTaskAmount = processTaskAmount;
    }

    public long getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(long connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public long getIdleTimeoutMs() {
        return idleTimeoutMs;
    }

    public void setIdleTimeoutMs(long idleTimeoutMs) {
        this.idleTimeoutMs = idleTimeoutMs;
    }

    public long getMaxLifetimeMs() {
        return maxLifetimeMs;
    }

    public void setMaxLifetimeMs(long maxLifetimeMs) {
        this.maxLifetimeMs = maxLifetimeMs;
    }

    public String getBpmnFile() {
        return bpmnFile;
    }

    public void setBpmnFile(String bpmnFile) {
        this.bpmnFile = bpmnFile;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }
}
