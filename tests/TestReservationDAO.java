// File: tests/TestReservationDAO.java
package tests;

import businesslogic.entities.Customer;
import businesslogic.entities.Flight;
import businesslogic.entities.Payment;
import businesslogic.entities.Reservation;
import businesslogic.entities.enums.ReservationStatus;
import datalayer.dao.ReservationDAO;
import datalayer.database.DatabaseConnection;
import datalayer.impl.ReservationDAOImpl;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class TestReservationDAO {

    public static void main(String[] args) {
        ReservationDAO reservationDAO = new ReservationDAOImpl();
        DatabaseConnection db = DatabaseConnection.getInstance();

        try (Connection conn = db.getConnection()) {

            conn.setAutoCommit(false);
            try {
                // ---------- CLEAN TABLES ----------
                System.out.println("==== CLEANING TABLES ====");
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("DELETE FROM reservations");
                    st.executeUpdate("DELETE FROM tickets");
                    st.executeUpdate("DELETE FROM payments");
                    st.executeUpdate("DELETE FROM flights");
                    st.executeUpdate("DELETE FROM routes");
                    st.executeUpdate("DELETE FROM airports");
                    st.executeUpdate("DELETE FROM airlines");
                    st.executeUpdate("DELETE FROM aircraft");
                    st.executeUpdate("DELETE FROM users");
                }

                // ---------- INSERT DEPENDENCIES ----------
                System.out.println("\n==== INSERTING DEPENDENCIES ====");
                int customerId = insertTestCustomer(conn);
                System.out.println("Customer ID: " + customerId);

                int aircraftId = insertTestAircraft(conn);
                System.out.println("Aircraft ID: " + aircraftId);

                int airlineId = insertTestAirline(conn);
                System.out.println("Airline ID: " + airlineId);

                insertTestAirports(conn);
                System.out.println("Inserted airports YYC and YVR");

                int routeId = insertTestRoute(conn);
                System.out.println("Route ID: " + routeId);

                int flightId = insertTestFlight(conn, aircraftId, airlineId, routeId);
                System.out.println("Flight ID: " + flightId);

                int paymentId = insertTestPayment(conn);
                System.out.println("Payment ID: " + paymentId);

                conn.commit();

                // ---------- BUILD RESERVATION ENTITY ----------
                Customer customer = new Customer();
                customer.setUserId(customerId);

                Flight flight = new Flight();
                flight.setFlightId(flightId);

                Payment payment = new Payment();
                payment.setPaymentId(paymentId);

                Reservation reservation = new Reservation();
                reservation.setBookingDate(LocalDateTime.now());
                reservation.setStatus(ReservationStatus.PENDING);
                reservation.setTotalPrice(500.00);
                reservation.setCustomer(customer);
                reservation.setFlight(flight);
                reservation.setPayment(payment);

                // ---------- TEST: SAVE ----------
                System.out.println("\n==== TEST: SAVE ====");
                Reservation saved = reservationDAO.save(reservation);
                System.out.println("Saved reservation with ID: " + saved.getReservationId());

                // ---------- TEST: FIND BY ID ----------
                System.out.println("\n==== TEST: FIND BY ID ====");
                Reservation found = reservationDAO.findById(saved.getReservationId());
                if (found != null) {
                    System.out.println("Found reservation:");
                    System.out.println("  ID: " + found.getReservationId());
                    System.out.println("  Status: " + found.getStatus());
                    System.out.println("  Total Price: " + found.getTotalPrice());
                    System.out.println("  Customer ID: " +
                            (found.getCustomer() != null ? found.getCustomer().getUserId() : "null"));
                    System.out.println("  Flight ID: " +
                            (found.getFlight() != null ? found.getFlight().getFlightId() : "null"));
                } else {
                    System.out.println("Reservation not found.");
                }

                // ---------- TEST: UPDATE ----------
                System.out.println("\n==== TEST: UPDATE ====");
                found.setStatus(ReservationStatus.CONFIRMED);
                boolean updateOk = reservationDAO.update(found);
                System.out.println("Update success: " + updateOk);
                Reservation updated = reservationDAO.findById(found.getReservationId());
                System.out.println("Updated reservation status: " +
                        (updated != null ? updated.getStatus() : "null"));

                // ---------- TEST: FIND BY CUSTOMER ----------
                System.out.println("\n==== TEST: FIND BY CUSTOMER ====");
                List<Reservation> customerReservations =
                        reservationDAO.findByCustomerId(customerId);
                System.out.println("Reservations for customer " + customerId + ": "
                        + customerReservations.size());

                // ---------- TEST: DELETE ----------
                System.out.println("\n==== TEST: DELETE ====");
                boolean deleteOk = reservationDAO.delete(updated.getReservationId());
                System.out.println("Delete success: " + deleteOk);
                Reservation deleted = reservationDAO.findById(updated.getReservationId());
                System.out.println("After delete, findById returns: " + deleted);

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------------------------
    // Helper methods to insert minimal data into each table
    // ----------------------------------------------------------------------

    private static int insertTestCustomer(Connection conn) throws SQLException {
        String sql = "INSERT INTO users " +
                "(username, password_hash, email, role, first_name, last_name, membership_status) " +
                "VALUES (?, ?, ?, 'CUSTOMER', ?, ?, 'REGULAR')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "test_customer");
            ps.setString(2, "password123");
            ps.setString(3, "test.customer@example.com");
            ps.setString(4, "Test");
            ps.setString(5, "Customer");
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to insert test customer");
    }

    private static int insertTestAircraft(Connection conn) throws SQLException {
        String sql = "INSERT INTO aircraft " +
                "(model, manufacturer, total_seats, seat_configuration, status) " +
                "VALUES (?, ?, ?, ?, 'ACTIVE')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "737-800");
            ps.setString(2, "Boeing");
            ps.setInt(3, 180);
            ps.setString(4, "3-3");
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to insert test aircraft");
    }

    private static int insertTestAirline(Connection conn) throws SQLException {
        String sql = "INSERT INTO airlines " +
                "(name, code, country) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Reservation Test Airline");
            ps.setString(2, "RT");
            ps.setString(3, "Canada");
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to insert test airline");
    }

    private static void insertTestAirports(Connection conn) throws SQLException {
        String sql = "INSERT INTO airports " +
                "(airport_code, name, city, country, timezone) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            // YYC
            ps.setString(1, "YYC");
            ps.setString(2, "Calgary International Airport");
            ps.setString(3, "Calgary");
            ps.setString(4, "Canada");
            ps.setString(5, "MST");
            ps.executeUpdate();

            // YVR
            ps.setString(1, "YVR");
            ps.setString(2, "Vancouver International Airport");
            ps.setString(3, "Vancouver");
            ps.setString(4, "Canada");
            ps.setString(5, "PST");
            ps.executeUpdate();
        }
    }

    private static int insertTestRoute(Connection conn) throws SQLException {
        String sql = "INSERT INTO routes " +
                "(origin_code, destination_code, distance_km, estimated_duration_minutes) " +
                "VALUES ('YYC', 'YVR', ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, 700);
            ps.setInt(2, 90);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to insert test route");
    }

    private static int insertTestFlight(Connection conn,
                                        int aircraftId,
                                        int airlineId,
                                        int routeId) throws SQLException {
        String sql = "INSERT INTO flights " +
                "(flight_number, departure_time, arrival_time, status, " +
                "available_seats, price, aircraft_id, route_id, airline_id) " +
                "VALUES (?, ?, ?, 'SCHEDULED', ?, ?, ?, ?, ?)";

        LocalDateTime dep = LocalDateTime.now().plusDays(1);
        LocalDateTime arr = dep.plusHours(1);

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "RS100");
            ps.setTimestamp(2, Timestamp.valueOf(dep));
            ps.setTimestamp(3, Timestamp.valueOf(arr));
            ps.setInt(4, 100);
            ps.setDouble(5, 500.00);
            ps.setInt(6, aircraftId);
            ps.setInt(7, routeId);
            ps.setInt(8, airlineId);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to insert test flight");
    }

    private static int insertTestPayment(Connection conn) throws SQLException {
        String sql = "INSERT INTO payments " +
                "(amount, payment_date, payment_method, transaction_id, status) " +
                "VALUES (?, ?, 'CREDIT_CARD', ?, 'COMPLETED')";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDouble(1, 500.00);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(3, "TX-RES-001");
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to insert test payment");
    }
}
