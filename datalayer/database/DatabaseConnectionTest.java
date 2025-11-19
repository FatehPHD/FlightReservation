package datalayer.database;
// DatabaseConnectionTest.java

public class DatabaseConnectionTest {

    public static void main(String[] args) {
        DatabaseConnection db = DatabaseConnection.getInstance();

        if (db.isValid(5)) {
            System.out.println("Database connection OK!");
        } else {
            System.out.println("Database connection FAILED.");
        }
    }
}
