package persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class DatabaseConfig {
    private static final int POOL_SIZE = 10;
    private static final long CONNECTION_TIMEOUT_MS = 30_000;
    private static final long IDLE_TIMEOUT_MS = 600_000;
    private static final long MAX_LIFETIME_MS = 1_800_000;

    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(System.getenv("DB_URL"));
        config.setUsername(System.getenv("DB_USER"));
        config.setPassword(System.getenv("DB_PASSWORD"));
        config.setMaximumPoolSize(POOL_SIZE);
        config.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
        config.setIdleTimeout(IDLE_TIMEOUT_MS);
        config.setMaxLifetime(MAX_LIFETIME_MS);

        dataSource = new HikariDataSource(config);
    }

    public static DSLContext getContext() {
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
