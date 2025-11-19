package datalayer.database;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    public static void begin(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
    }

    public static void commit(Connection conn) throws SQLException {
        conn.commit();
        conn.setAutoCommit(true);
    }

    public static void rollback(Connection conn) {
        try {
            conn.rollback();
            conn.setAutoCommit(true);
        } catch (SQLException ignore) {}
    }
}
