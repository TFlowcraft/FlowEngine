package persistence;


import java.sql.Connection;
import java.sql.SQLException;


public final class TransactionManager {
    public static void executeInTransaction(TransactionalOperation operation) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getDataSource().getConnection();
            conn.setAutoCommit(false);
            operation.execute(conn);
            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
                throw e;
            }
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @FunctionalInterface
    public interface TransactionalOperation {
        void execute(Connection connection) throws SQLException;
    }
}
