Flight Reservation System - Setup Guide

Prerequisites

    1. Java JDK 8 or higher - Check with: `java -version`
    2. MySQL Server** - Check with: `mysql --version`
    3. MySQL Workbench or command-line MySQL client

---

Step 1: Set Up MySQL Database

    Option A: Using MySQL Command Line

        1. Start MySQL server (if not running):
        ```bash
        # On macOS with Homebrew:
        brew services start mysql
        
        # Or check if it's running:
        mysql.server status
        ```

        2. Log into MySQL:
        ```bash
        mysql -u root -p
        ```
        (Enter your MySQL root password when prompted)

        3. Run the schema file:
        ```sql
        source '/Users/macbook/1aaaaaaa/clone 480 proj/flight_reservation/schema.sql';
        ```
        
        OR copy and paste the entire contents of `schema.sql` into your MySQL client.

    Option B: Using MySQL Workbench

        1. Open MySQL Workbench
        2. Connect to your local MySQL server
        3. Open `schema.sql` file
        4. Execute the entire script (Cmd+Shift+Enter on Mac, Ctrl+Shift+Enter on Windows)

        ### Verify Database Creation

        ```sql
        USE flight_booking_system;
        SHOW TABLES;
        ```

        You should see tables like: `users`, `airports`, `airlines`, `routes`, `aircraft`, `flights`, etc.

---

Step 2: Update Database Configuration

    Edit `datalayer/database/DatabaseConfig.java` and update these values:

    ```java
    public static final String DB_USER = "root";           // Your MySQL username
    public static final String DB_PASSWORD = "root123";   // Your MySQL password
    ```

    **Important:** Change `"root123"` to your actual MySQL root password!

---
Step 3: Compile the Java Code

    Option A: Using the provided compile script

        Run:
        ```bash
        ./compile.sh
        ```

    Option B: Manual compilation

        From the project root directory:

        ```bash
        # Compile all Java files
        javac -cp "lib/mysql-connector-j-9.0.0.jar" \
        businesslogic/entities/*.java \
        businesslogic/entities/enums/*.java \
        businesslogic/services/*.java \
        datalayer/dao/*.java \
        datalayer/impl/*.java \
        datalayer/database/*.java \
        tests/*.java
        ```

---

Step 4: Test the Database Connection

    Run the connection test:

        ```bash
        java -cp ".:lib/mysql-connector-j-9.0.0.jar" \
        datalayer.database.DatabaseConnectionTest
        ```

        **Expected output:** `Database connection OK!`

        If you see an error, check:
        - MySQL server is running
        - Database credentials in `DatabaseConfig.java` are correct
        - Database `flight_booking_system` exists

---

Step 5: Run Tests (Optional)

    Test the UserDAO:

        ```bash
        java -cp ".:lib/mysql-connector-j-9.0.0.jar" \
        tests.TestUserDAO
        ```

        This will create test users (Customer, FlightAgent, SystemAdmin) and test CRUD operations.

    Available Test Classes:

        ```bash
        # Test User DAO
        java -cp ".:lib/mysql-connector-j-9.0.0.jar" tests.TestUserDAO

        # Test Flight DAO
        java -cp ".:lib/mysql-connector-j-9.0.0.jar" tests.TestFlightDAO

        # Test Reservation DAO
        java -cp ".:lib/mysql-connector-j-9.0.0.jar" tests.TestReservationDAO

        # Test Payment DAO
        java -cp ".:lib/mysql-connector-j-9.0.0.jar" tests.TestPaymentDAO

        # Test Seat DAO
        java -cp ".:lib/mysql-connector-j-9.0.0.jar" tests.TestSeatDAO

        # Test Aircraft DAO
        java -cp ".:lib/mysql-connector-j-9.0.0.jar" tests.TestAircraftDAO

        # Test Airline DAO
        java -cp ".:lib/mysql-connector-j-9.0.0.jar" tests.TestAirlineDAO

        # Test Airport DAO
        java -cp ".:lib/mysql-connector-j-9.0.0.jar" tests.TestAirportDAO

        # Test Route DAO
        java -cp ".:lib/mysql-connector-j-9.0.0.jar" tests.TestRouteDAO
        ```

---

Troubleshooting

    "ClassNotFoundException: com.mysql.cj.jdbc.Driver"
        - Make sure `lib/mysql-connector-j-9.0.0.jar` exists
        - Check the classpath includes the JAR file

    "Access denied for user 'root'@'localhost'"
        - Update `DB_USER` and `DB_PASSWORD` in `DatabaseConfig.java`
        - Verify your MySQL credentials

    "Unknown database 'flight_booking_system'"
        - Run `schema.sql` to create the database
        - Check database name matches in `DatabaseConfig.java`

    "Connection refused"
        - Start MySQL server: `brew services start mysql` (macOS) or check MySQL service status

---

Next Steps
    Once the database connection works:
        1. ✅ Database layer is ready (all DAOs implemented)
        2. ✅ Service classes implemented (FlightService, ReservationService, PaymentService, CustomerService, AdminService)
        3. ✅ All test classes available
        4. ⏳ Build the GUI (Swing interface)
        5. ⏳ Implement business logic integration
        6. ⏳ Create main application entry point

---

Quick Start Commands Summary

    ```bash
    # 1. Compile
    ./compile.sh

    # 2. Test connection
    java -cp ".:lib/mysql-connector-j-9.0.0.jar" datalayer.database.DatabaseConnectionTest

    # 3. Run any test (examples)
    java -cp ".:lib/mysql-connector-j-9.0.0.jar" tests.TestUserDAO
    java -cp ".:lib/mysql-connector-j-9.0.0.jar" tests.TestFlightDAO
    java -cp ".:lib/mysql-connector-j-9.0.0.jar" tests.TestPaymentDAO
    ```



 Seperate Terminal
   ```bash
    # Leave MySQL client
    mysql -u root -p     
    ```


    
Quick End Commands Summary

    ```bash
    # Leave MySQL client
    exit;                                
    
    # Clean old builds
    find . -name "*.class" -delete       
    ```

Log into MySQL:
        ```bash
        mysql -u root -p
        ```