package persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class DatabaseConfig {
    private static final int POOL_SIZE = 100;
    private static final long CONNECTION_TIMEOUT_MS = 30_000;
    private static final long IDLE_TIMEOUT_MS = 600_000;
    private static final long MAX_LIFETIME_MS = 1_800_000;

    private static HikariDataSource dataSource;

    private DatabaseConfig() {
    }

    public static void setupConfig(String jdbcUrl, String jdbcUser, String jdbcPassword) {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(jdbcUser);
        config.setPassword(jdbcPassword);
        config.setMaximumPoolSize(POOL_SIZE);
        config.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        config.setIdleTimeout(IDLE_TIMEOUT_MS);
        config.setMaxLifetime(MAX_LIFETIME_MS);

        dataSource = new HikariDataSource(config);
    }

    public static void setupFromEnv() {
        Dotenv dotenv = Dotenv.load();
        String jdbcUrl = dotenv.get("DB_URL");
        String jdbcUser = dotenv.get("DB_USER");
        String jdbcPassword = dotenv.get("DB_PASSWORD");

        setupConfig(jdbcUrl, jdbcUser, jdbcPassword);
    }

    public static DSLContext getContext() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not configured. Call setupConfig first.");
        }
        return DSL.using(dataSource, SQLDialect.POSTGRES);
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }

    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}