# Flight Reservation System - File Explanation

This document explains what each file in the project contains and its purpose.

---

## 📁 **ROOT FILES**

### `schema.sql`
**Purpose:** MySQL database schema definition  
**Contains:**
- Creates database `flight_booking_system`
- Defines all tables: `users`, `airports`, `airlines`, `routes`, `aircraft`, `flights`, `seats`, `passengers`, `payments`, `reservations`, `tickets`, `promotions`, `system_admin_permissions`
- Sets up foreign key relationships
- Uses single-table inheritance for users (Customer, FlightAgent, SystemAdmin in one table)

### `README.md`
**Purpose:** Basic project description (currently minimal)

### `SETUP_GUIDE.md`
**Purpose:** Step-by-step instructions to set up and run the project

### `compile.sh`
**Purpose:** Bash script to compile all Java files  
**What it does:**
- Compiles all `.java` files in the project
- Includes MySQL connector JAR in classpath
- Creates necessary directories for compiled `.class` files

### `LICENSE`
**Purpose:** Project license file

---

## 📁 **businesslogic/entities/** - Domain Model (Entity Classes)

These are the core business objects representing real-world concepts.

### `User.java` (Abstract Base Class)
**Purpose:** Base class for all user types  
**Contains:**
- Common fields: `userId`, `username`, `password`, `email`, `role`
- Abstract class - cannot be instantiated directly
- Extended by `Customer`, `FlightAgent`, `SystemAdmin`

### `Customer.java`
**Purpose:** Represents a customer user  
**Contains:**
- Extends `User`
- Customer-specific fields: `firstName`, `lastName`, `phone`, `address`, `dateOfBirth`, `membershipStatus`
- Used for booking flights

### `FlightAgent.java`
**Purpose:** Represents a flight agent user  
**Contains:**
- Extends `User`
- Agent-specific fields: `employeeId`, `hireDate`, `department`
- Can book flights on behalf of customers

### `SystemAdmin.java`
**Purpose:** Represents a system administrator  
**Contains:**
- Extends `User`
- Admin-specific fields: `adminLevel`, `permissions` (EnumSet)
- Methods: `hasPermission()`, `addPermission()`, `removePermission()`
- Manages system data (flights, routes, aircraft)

### `Flight.java`
**Purpose:** Represents a flight  
**Contains:**
- Fields: `flightNumber`, `departureTime`, `arrivalTime`, `status`, `availableSeats`, `price`
- References: `Aircraft`, `Route`
- Status enum: SCHEDULED, DELAYED, CANCELLED, DEPARTED, ARRIVED

### `Aircraft.java`
**Purpose:** Represents an aircraft/airplane  
**Contains:**
- Fields: `aircraftId`, `model`, `manufacturer`, `totalSeats`, `seatConfiguration`, `status`
- Example: Boeing 737-800 with 180 seats, configuration "3-3"

### `Airport.java`
**Purpose:** Represents an airport  
**Contains:**
- Fields: `airportCode` (primary key, e.g., "YYC"), `name`, `city`, `country`, `timezone`
- Example: YYC = Calgary International Airport

### `Airline.java`
**Purpose:** Represents an airline company  
**Contains:**
- Fields: `airlineId`, `name`, `code` (e.g., "AC" for Air Canada), `country`

### `Route.java`
**Purpose:** Represents a flight route (origin → destination)  
**Contains:**
- Fields: `routeId`, `origin` (Airport), `destination` (Airport), `distance` (km), `estimatedDuration` (minutes)
- Example: YYC → YYZ (Calgary to Toronto)

### `Reservation.java`
**Purpose:** Represents a booking/reservation  
**Contains:**
- Fields: `reservationId`, `bookingDate`, `status`, `totalPrice`
- References: `Customer`, `Flight`, `Payment`, `List<Seat>`
- Status: PENDING, CONFIRMED, CANCELLED, COMPLETED

### `Seat.java`
**Purpose:** Represents a seat on a flight  
**Contains:**
- Fields: `seatId`, `seatNumber` (e.g., "12A"), `seatClass`, `isAvailable`
- References: `Flight` (via flight_id in database)

### `Ticket.java`
**Purpose:** Represents an issued ticket  
**Contains:**
- Fields: `ticketNumber`, `issueDate`, `passengerName`, `barcode`
- References: `Reservation`, `Seat`

### `Passenger.java`
**Purpose:** Represents a passenger (person flying)  
**Contains:**
- Fields: `passengerId`, `firstName`, `lastName`, `dateOfBirth`, `passportNumber`, `nationality`
- Note: Passenger is separate from Customer (a customer can book for multiple passengers)

### `Payment.java`
**Purpose:** Represents a payment transaction  
**Contains:**
- Fields: `paymentId`, `amount`, `paymentDate`, `paymentMethod`, `transactionId`, `status`
- Payment methods: CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER
- Status: PENDING, COMPLETED, FAILED, REFUNDED

### `Promotion.java`
**Purpose:** Represents a promotional offer  
**Contains:**
- Fields: `promotionId`, `title`, `description`, `discountPercent`, `validFrom`, `validTo`
- References: `List<Route>` (applicable routes)

### `EntityTest.java`
**Purpose:** Test file for entity classes (basic testing)

---

## 📁 **businesslogic/entities/enums/** - Enumeration Types

These define fixed sets of values used throughout the system.

### `UserRole.java`
**Values:** `CUSTOMER`, `FLIGHT_AGENT`, `SYSTEM_ADMIN`

### `FlightStatus.java`
**Values:** `SCHEDULED`, `DELAYED`, `CANCELLED`, `DEPARTED`, `ARRIVED`

### `ReservationStatus.java`
**Values:** `PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`

### `PaymentMethod.java`
**Values:** `CREDIT_CARD`, `DEBIT_CARD`, `PAYPAL`, `BANK_TRANSFER`

### `PaymentStatus.java`
**Values:** `PENDING`, `COMPLETED`, `FAILED`, `REFUNDED`

### `SeatClass.java`
**Values:** `ECONOMY`, `BUSINESS`, `FIRST`

### `MembershipStatus.java`
**Values:** `REGULAR`, `SILVER`, `GOLD`, `PLATINUM`

### `SystemAdminPermission.java`
**Values:** `ALL`, `MANAGE_USERS`, `MANAGE_FLIGHTS`, `MANAGE_ROUTES`, `MANAGE_AIRCRAFT`, `VIEW_REPORTS`, `MODIFY_SYSTEM_SETTINGS`

---

## 📁 **datalayer/database/** - Database Infrastructure

### `DatabaseConfig.java`
**Purpose:** Database configuration constants  
**Contains:**
- Database connection settings: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- JDBC URL and driver class name
- **⚠️ IMPORTANT:** Update `DB_USER` and `DB_PASSWORD` to match your MySQL credentials!

### `DatabaseConnection.java`
**Purpose:** Singleton database connection manager  
**Contains:**
- Singleton pattern implementation
- Creates and maintains a single database connection
- Methods: `getInstance()`, `getConnection()`, `isValid()`
- Handles connection initialization

### `DatabaseConnectionTest.java`
**Purpose:** Test class to verify database connection works  
**Contains:**
- Simple main method that tests if connection is valid
- Output: "Database connection OK!" or "Database connection FAILED."

### `QueryBuilder.java`
**Purpose:** Utility class to build SQL queries dynamically  
**Contains:**
- Static methods: `createInsertQuery()`, `createUpdateQuery()`, `createSelectByIdQuery()`, `createDeleteByIdQuery()`
- Helps avoid writing raw SQL strings

### `TransactionManager.java`
**Purpose:** Utility class for database transactions  
**Contains:**
- Static methods: `begin()`, `commit()`, `rollback()`
- Manages transaction boundaries (start, commit, rollback on error)

---

## 📁 **datalayer/dao/** - Data Access Object Interfaces

These define the contract for database operations (interfaces).

### `BaseDAO.java`
**Purpose:** Generic interface for all DAOs  
**Contains:**
- Generic CRUD operations: `save()`, `findById()`, `findAll()`, `update()`, `delete()`
- All DAOs extend this interface

### `UserDAO.java`
**Purpose:** Interface for user database operations  
**Contains:**
- Extends `BaseDAO<User, Integer>`
- Additional methods: `findByUsername()`, `findAllCustomers()`, `findAllFlightAgents()`, `findAllSystemAdmins()`
- Handles single-table inheritance (Customer, FlightAgent, SystemAdmin)

### `AircraftDAO.java`
**Purpose:** Interface for aircraft database operations  
**Contains:**
- Extends `BaseDAO<Aircraft, Integer>`
- Standard CRUD operations for aircraft

### `AirlineDAO.java`
**Purpose:** Interface for airline database operations  
**Contains:**
- Extends `BaseDAO<Airline, Integer>`
- Standard CRUD operations for airlines

### `AirportDAO.java`
**Purpose:** Interface for airport database operations  
**Contains:**
- Extends `BaseDAO<Airport, String>` (uses airport code as ID)
- Additional methods: `findByCode()`, `deleteByCode()`

### `RouteDAO.java`
**Purpose:** Interface for route database operations  
**Contains:**
- Extends `BaseDAO<Route, Integer>`
- Standard CRUD operations for routes

---

## 📁 **datalayer/impl/** - Data Access Object Implementations

These implement the DAO interfaces with actual database code.

### `UserDAOImpl.java`
**Purpose:** Implementation of `UserDAO`  
**Contains:**
- Implements all CRUD operations for users
- Handles single-table inheritance (maps Customer/FlightAgent/SystemAdmin to/from database)
- Manages `system_admin_permissions` table (many-to-many relationship)
- Complex mapping logic to convert database rows to appropriate user subclass

### `AircraftDAOImpl.java`
**Purpose:** Implementation of `AircraftDAO`  
**Contains:**
- Implements CRUD operations for aircraft
- Maps database rows to `Aircraft` objects
- Uses prepared statements for SQL operations

### `AirlineDAOImpl.java`
**Purpose:** Implementation of `AirlineDAO`  
**Contains:**
- Implements CRUD operations for airlines
- Uses `QueryBuilder` utility for dynamic SQL generation
- Uses `TransactionManager` for transaction handling

### `AirportDAOImpl.java`
**Purpose:** Implementation of `AirportDAO`  
**Contains:**
- Implements CRUD operations for airports
- Uses airport code (String) as primary key
- Handles timezone and location data

### `RouteDAOImpl.java`
**Purpose:** Implementation of `RouteDAO`  
**Contains:**
- Implements CRUD operations for routes
- Maps origin/destination airport codes to `Airport` objects
- Handles distance and duration calculations

---

## 📁 **tests/** - Test Classes

These test the DAO implementations.

### `TestUserDAO.java`
**Purpose:** Comprehensive test for `UserDAO`  
**Contains:**
- Tests saving Customer, FlightAgent, SystemAdmin
- Tests finding by ID and username
- Tests updating users
- Tests finding all users and by role
- Tests deleting users
- **Output:** Shows all CRUD operations working correctly

### `TestAircraftDAO.java`
**Purpose:** Test for `AircraftDAO`  
**Contains:**
- Tests save, findById, update, findAll, delete operations
- Creates test aircraft (Boeing 737-800)

### `TestAirlineDAO.java`
**Purpose:** Test for `AirlineDAO`  
**Contains:**
- Tests CRUD operations for airlines

### `TestAirportDAO.java`
**Purpose:** Test for `AirportDAO`  
**Contains:**
- Tests CRUD operations for airports

### `TestRouteDAO.java`
**Purpose:** Test for `RouteDAO`  
**Contains:**
- Tests CRUD operations for routes

---

## 📁 **lib/** - External Libraries

### `mysql-connector-j-9.0.0.jar`
**Purpose:** MySQL JDBC driver  
**Contains:**
- Java library to connect to MySQL database
- Required for all database operations
- Included in classpath when compiling/running

---

## ❌ **MISSING FILES** (Not Yet Implemented)

### Missing DAOs:
- `FlightDAO.java` / `FlightDAOImpl.java`
- `ReservationDAO.java` / `ReservationDAOImpl.java`
- `SeatDAO.java` / `SeatDAOImpl.java`
- `PaymentDAO.java` / `PaymentDAOImpl.java`
- `TicketDAO.java` / `TicketDAOImpl.java`
- `PassengerDAO.java` / `PassengerDAOImpl.java`
- `PromotionDAO.java` / `PromotionDAOImpl.java`

### Missing Business Logic:
- Control/Service classes (BookingController, FlightSearchController, etc.)
- Business rules and validation logic

### Missing Presentation Layer:
- No GUI files (Swing windows)
- No login screen
- No flight search interface
- No booking interface
- No admin dashboard

---

## 📊 **Summary**

**What's Complete:**
- ✅ Database schema
- ✅ All entity classes
- ✅ All enum types
- ✅ Database connection infrastructure
- ✅ Basic DAOs (User, Aircraft, Airline, Airport, Route)
- ✅ Test classes for implemented DAOs

**What's Missing:**
- ❌ Flight, Reservation, Seat, Payment, Ticket, Passenger, Promotion DAOs
- ❌ Business logic/control classes
- ❌ GUI/Presentation layer (Swing)
- ❌ Core functionality (search, booking, payment processing)

**Current Completion:** ~30% of full project


