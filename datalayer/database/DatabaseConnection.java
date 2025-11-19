package datalayer.database;
// DatabaseConnection.java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            Class.forName(DatabaseConfig.JDBC_DRIVER);
            this.connection = DriverManager.getConnection(
                    DatabaseConfig.DB_URL,
                    DatabaseConfig.DB_USER,
                    DatabaseConfig.DB_PASSWORD
            );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC Driver not found: " + DatabaseConfig.JDBC_DRIVER, e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + DatabaseConfig.DB_URL, e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    // Optional: helper for testing connectivity
    public boolean isValid(int timeoutSeconds) {
        try {
            return connection != null && connection.isValid(timeoutSeconds);
        } catch (SQLException e) {
            return false;
        }
    }
}
