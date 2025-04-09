package persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.util.Objects;

public class DatabaseConfig {
    public static final int DEFAULT_POOL_SIZE = 100;
    public static final long DEFAULT_CONNECTION_TIMEOUT_MS = 30_000;
    public static final long DEFAULT_IDLE_TIMEOUT_MS = 600_000;
    public static final long DEFAULT_MAX_LIFETIME_MS = 1_800_000;

    private static HikariDataSource dataSource;

    private DatabaseConfig() {
    }

    public static void setupConfig(String jdbcUrl,
                                   String jdbcUser,
                                   String jdbcPassword,
                                   int poolSize,
                                   long connectionTimeoutMs,
                                   long idleTimeoutMs,
                                   long maxLifetimeMs) {
        validateConnectionParams(jdbcUrl, jdbcUser, jdbcPassword);
        validatePoolSettings(poolSize, connectionTimeoutMs, idleTimeoutMs, maxLifetimeMs);

        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(jdbcUser);
        config.setPassword(jdbcPassword);
        config.setMaximumPoolSize(poolSize);
        config.setConnectionTimeout(connectionTimeoutMs);
        config.setIdleTimeout(idleTimeoutMs);
        config.setMaxLifetime(maxLifetimeMs);

        dataSource = new HikariDataSource(config);
    }

    public static void setupConfig(String jdbcUrl, String jdbcUser, String jdbcPassword) {
        setupConfig(
                jdbcUrl,
                jdbcUser,
                jdbcPassword,
                DEFAULT_POOL_SIZE,
                DEFAULT_CONNECTION_TIMEOUT_MS,
                DEFAULT_IDLE_TIMEOUT_MS,
                DEFAULT_MAX_LIFETIME_MS
        );
    }

    public static void setupFromEnv() {
        Dotenv dotenv = Dotenv.load();

        String jdbcUrl = dotenv.get("DB_URL");
        String jdbcUser = dotenv.get("DB_USER");
        String jdbcPassword = dotenv.get("DB_PASSWORD");

        if (jdbcUrl == null || jdbcUser == null || jdbcPassword == null) {
            throw new IllegalStateException("Missing required DB environment variables (DB_URL, DB_USER, DB_PASSWORD)");
        }

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

    private static void validateConnectionParams(String url, String user, String password) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("JDBC URL must not be null or empty");
        }
        if (user == null || user.isBlank()) {
            throw new IllegalArgumentException("JDBC User must not be null or empty");
        }
        if (password == null) {
            throw new IllegalArgumentException("JDBC Password must not be null");
        }
    }

    // ===== ✅ Валидация настроек пула =====
    private static void validatePoolSettings(int poolSize,
                                             long connectionTimeoutMs,
                                             long idleTimeoutMs,
                                             long maxLifetimeMs) {
        if (poolSize <= 0) {
            throw new IllegalArgumentException("Pool size must be greater than 0");
        }
        if (connectionTimeoutMs < 1000) {
            throw new IllegalArgumentException("Connection timeout must be at least 1000ms");
        }
        if (idleTimeoutMs < 10_000) {
            throw new IllegalArgumentException("Idle timeout must be at least 10000ms");
        }
        if (maxLifetimeMs <= idleTimeoutMs) {
            throw new IllegalArgumentException("Max lifetime must be greater than idle timeout");
        }
    }
}
