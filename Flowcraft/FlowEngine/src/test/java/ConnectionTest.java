import org.jooq.DSLContext;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import persistence.DatabaseConfig;


public class ConnectionTest {
    @Test
    public void testConnection() {
        DSLContext ctx = DatabaseConfig.getContext();
        var dataSource = DatabaseConfig.getDataSource();
        System.out.println("HikariCP Active Connections: " + dataSource.getHikariPoolMXBean().getActiveConnections());
    }
}
