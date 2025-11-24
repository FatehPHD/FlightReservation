package datalayer.database;
// DatabaseConfig.java
public final class DatabaseConfig {

    private DatabaseConfig() {
        // prevent instantiation
    }

    public static final String DB_HOST = "localhost";
    public static final String DB_PORT = "3306";
    public static final String DB_NAME = "flight_booking_system";

    // Change these to your actual MySQL username/password
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "";

    public static final String DB_URL =
            "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    public static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
}
