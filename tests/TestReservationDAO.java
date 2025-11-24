package tests;

import businesslogic.entities.Reservation;
import businesslogic.entities.Customer;
import businesslogic.entities.Flight;
import businesslogic.entities.Payment;
import businesslogic.entities.enums.ReservationStatus;
import businesslogic.entities.enums.UserRole;
import datalayer.dao.ReservationDAO;
import datalayer.impl.ReservationDAOImpl;
import datalayer.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestReservationDAO {

    public static void main(String[] args) {
        try {
            // Ensure dependencies exist
            int customerId = ensureTestCustomerExists();
            int flightId = ensureTestFlightExists();
            int paymentId = ensureTestPaymentExists();

            ReservationDAO reservationDAO = new ReservationDAOImpl();

            System.out.println("==== TEST: SAVE ====");
            Reservation reservation = new Reservation(
                    0, // ID will be generated
                    LocalDateTime.now(),
                    ReservationStatus.PENDING,
                    599.98,
                    getCustomerById(customerId),
                    getFlightById(flightId),
                    getPaymentById(paymentId),
                    new ArrayList<>() // Seats will be empty for now
            );

            Reservation saved = reservationDAO.save(reservation);
            System.out.println("Saved reservation with ID: " + saved.getReservationId());
            System.out.println("Reservation: " + saved);

            System.out.println("\n==== TEST: FIND BY ID ====");
            Reservation found = reservationDAO.findById(saved.getReservationId());
            System.out.println("Found reservation: " + found);

            System.out.println("\n==== TEST: FIND BY CUSTOMER ID ====");
            List<Reservation> byCustomer = reservationDAO.findByCustomerId(customerId);
            System.out.println("Reservations for customer " + customerId + ": " + byCustomer.size());
            for (Reservation r : byCustomer) {
                System.out.println("  " + r);
            }

            System.out.println("\n==== TEST: FIND BY FLIGHT ID ====");
            List<Reservation> byFlight = reservationDAO.findByFlightId(flightId);
            System.out.println("Reservations for flight " + flightId + ": " + byFlight.size());
            for (Reservation r : byFlight) {
                System.out.println("  " + r);
            }

            System.out.println("\n==== TEST: UPDATE ====");
            found.setStatus(ReservationStatus.CONFIRMED);
            boolean updated = reservationDAO.update(found);
            System.out.println("Update result: " + updated);

            Reservation updatedFromDb = reservationDAO.findById(found.getReservationId());
            System.out.println("Updated reservation from DB: " + updatedFromDb);

            System.out.println("\n==== TEST: FIND ALL ====");
            List<Reservation> all = reservationDAO.findAll();
            System.out.println("Total reservations in DB: " + all.size());
            for (Reservation r : all) {
                System.out.println(r);
            }

            System.out.println("\n==== TEST: DELETE ====");
            boolean deleted = reservationDAO.delete(found.getReservationId());
            System.out.println("Delete result: " + deleted);

            Reservation afterDelete = reservationDAO.findById(found.getReservationId());
            System.out.println("Find after delete (should be null): " + afterDelete);

            System.out.println("\n==== TESTS COMPLETED ====");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int ensureTestCustomerExists() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        String checkSql = "SELECT user_id FROM users WHERE username = 'testcustomer' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(checkSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        }

        String insertSql = "INSERT INTO users (username, password_hash, email, role, first_name, last_name, " +
                          "phone, address, date_of_birth, membership_status) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "testcustomer");
            ps.setString(2, "password123");
            ps.setString(3, "test@example.com");
            ps.setString(4, "CUSTOMER");
            ps.setString(5, "Test");
            ps.setString(6, "Customer");
            ps.setString(7, "555-0000");
            ps.setString(8, "Test Address");
            ps.setDate(9, java.sql.Date.valueOf(LocalDate.of(1990, 1, 1)));
            ps.setString(10, "REGULAR");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create test customer");
    }

    private static int ensureTestFlightExists() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        // Ensure dependencies
        ensureTestDependencies(conn);

        String checkSql = "SELECT flight_id FROM flights WHERE flight_number = 'AC-RES-001' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(checkSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("flight_id");
            }
        }

        String insertSql = "INSERT INTO flights (flight_number, departure_time, arrival_time, " +
                          "status, available_seats, price, aircraft_id, route_id, airline_id) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "AC-RES-001");
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(3)));
            ps.setString(4, "SCHEDULED");
            ps.setInt(5, 150);
            ps.setDouble(6, 299.99);
            ps.setInt(7, getAircraftId(conn));
            ps.setInt(8, getRouteId(conn));
            ps.setInt(9, getAirlineId(conn));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create test flight");
    }

    private static int ensureTestPaymentExists() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        String checkSql = "SELECT payment_id FROM payments WHERE transaction_id = 'TEST-TXN-001' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(checkSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("payment_id");
            }
        }

        String insertSql = "INSERT INTO payments (amount, payment_date, payment_method, transaction_id, status) " +
                          "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, 599.98);
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(3, "CREDIT_CARD");
            ps.setString(4, "TEST-TXN-001");
            ps.setString(5, "COMPLETED");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create test payment");
    }

    private static void ensureTestDependencies(Connection conn) throws SQLException {
        // Ensure airports
        ensureAirport(conn, "YYC", "Calgary International", "Calgary", "Canada", "MST");
        ensureAirport(conn, "YYZ", "Toronto Pearson", "Toronto", "Canada", "EST");

        // Ensure aircraft
        String checkAircraft = "SELECT aircraft_id FROM aircraft WHERE model = 'TEST-737' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(checkAircraft);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                String sql = "INSERT INTO aircraft (model, manufacturer, total_seats, seat_configuration, status) " +
                            "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps2 = conn.prepareStatement(sql)) {
                    ps2.setString(1, "TEST-737");
                    ps2.setString(2, "Boeing");
                    ps2.setInt(3, 180);
                    ps2.setString(4, "3-3");
                    ps2.setString(5, "ACTIVE");
                    ps2.executeUpdate();
                }
            }
        }

        // Ensure route
        String checkRoute = "SELECT route_id FROM routes WHERE origin_code = 'YYC' AND destination_code = 'YYZ' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(checkRoute);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                String sql = "INSERT INTO routes (origin_code, destination_code, distance_km, estimated_duration_minutes) " +
                            "VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps2 = conn.prepareStatement(sql)) {
                    ps2.setString(1, "YYC");
                    ps2.setString(2, "YYZ");
                    ps2.setDouble(3, 2700.0);
                    ps2.setInt(4, 240);
                    ps2.executeUpdate();
                }
            }
        }

        // Ensure airline
        String checkAirline = "SELECT airline_id FROM airlines WHERE code = 'AC' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(checkAirline);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                String sql = "INSERT INTO airlines (name, code, country) VALUES (?, ?, ?)";
                try (PreparedStatement ps2 = conn.prepareStatement(sql)) {
                    ps2.setString(1, "Air Canada");
                    ps2.setString(2, "AC");
                    ps2.setString(3, "Canada");
                    ps2.executeUpdate();
                }
            }
        }
    }

    private static void ensureAirport(Connection conn, String code, String name, String city, String country, String timezone) throws SQLException {
        String sql = "INSERT INTO airports (airport_code, name, city, country, timezone) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE name = VALUES(name)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setString(3, city);
            ps.setString(4, country);
            ps.setString(5, timezone);
            ps.executeUpdate();
        }
    }

    private static int getAircraftId(Connection conn) throws SQLException {
        String sql = "SELECT aircraft_id FROM aircraft WHERE model = 'TEST-737' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("aircraft_id");
            }
        }
        throw new SQLException("Test aircraft not found");
    }

    private static int getRouteId(Connection conn) throws SQLException {
        String sql = "SELECT route_id FROM routes WHERE origin_code = 'YYC' AND destination_code = 'YYZ' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("route_id");
            }
        }
        throw new SQLException("Test route not found");
    }

    private static int getAirlineId(Connection conn) throws SQLException {
        String sql = "SELECT airline_id FROM airlines WHERE code = 'AC' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("airline_id");
            }
        }
        throw new SQLException("Test airline not found");
    }

    private static Customer getCustomerById(int customerId) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Customer customer = new Customer();
                    customer.setUserId(rs.getInt("user_id"));
                    customer.setUsername(rs.getString("username"));
                    customer.setEmail(rs.getString("email"));
                    customer.setRole(UserRole.CUSTOMER);
                    customer.setFirstName(rs.getString("first_name"));
                    customer.setLastName(rs.getString("last_name"));
                    return customer;
                }
            }
        }
        return null;
    }

    private static Flight getFlightById(int flightId) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT * FROM flights WHERE flight_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, flightId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Flight flight = new Flight();
                    flight.setFlightNumber(rs.getString("flight_number"));
                    flight.setDepartureTime(rs.getTimestamp("departure_time").toLocalDateTime());
                    flight.setArrivalTime(rs.getTimestamp("arrival_time").toLocalDateTime());
                    flight.setStatus(businesslogic.entities.enums.FlightStatus.valueOf(rs.getString("status")));
                    flight.setAvailableSeats(rs.getInt("available_seats"));
                    flight.setPrice(rs.getDouble("price"));
                    return flight;
                }
            }
        }
        return null;
    }

    private static Payment getPaymentById(int paymentId) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        String sql = "SELECT * FROM payments WHERE payment_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Payment(
                        rs.getInt("payment_id"),
                        rs.getDouble("amount"),
                        rs.getTimestamp("payment_date").toLocalDateTime(),
                        businesslogic.entities.enums.PaymentMethod.valueOf(rs.getString("payment_method")),
                        rs.getString("transaction_id"),
                        businesslogic.entities.enums.PaymentStatus.valueOf(rs.getString("status"))
                    );
                }
            }
        }
        return null;
    }
}

