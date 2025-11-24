package tests;

import businesslogic.entities.Seat;
import businesslogic.entities.enums.SeatClass;
import datalayer.dao.SeatDAO;
import datalayer.impl.SeatDAOImpl;
import datalayer.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class TestSeatDAO {

    public static void main(String[] args) {
        try {
            // Ensure a test flight exists
            int testFlightId = ensureTestFlightExists();

            SeatDAO seatDAO = new SeatDAOImpl();

            System.out.println("==== TEST: SAVE ====");
            // Use a unique seat number to avoid duplicate key errors
            String uniqueSeatNumber = "TEST-" + System.currentTimeMillis() % 10000 + "A";
            Seat seat1 = new Seat();
            seat1.setSeatNumber(uniqueSeatNumber);
            seat1.setSeatClass(SeatClass.ECONOMY);
            seat1.setAvailable(true);

            // Note: Seat entity doesn't have flightId, so we need to set it via direct SQL
            // For testing, we'll save it with the flight_id
            Seat saved = saveSeatWithFlightId(seatDAO, seat1, testFlightId);
            System.out.println("Saved seat with ID: " + saved.getSeatId());
            System.out.println("Seat: " + saved);

            System.out.println("\n==== TEST: FIND BY ID ====");
            Seat found = seatDAO.findById(saved.getSeatId());
            System.out.println("Found seat: " + found);

            System.out.println("\n==== TEST: FIND BY FLIGHT ID ====");
            List<Seat> seatsByFlight = seatDAO.findByFlightId(testFlightId);
            System.out.println("Seats for flight " + testFlightId + ": " + seatsByFlight.size());
            for (Seat s : seatsByFlight) {
                System.out.println("  " + s);
            }

            System.out.println("\n==== TEST: FIND AVAILABLE SEATS BY FLIGHT ID ====");
            List<Seat> availableSeats = seatDAO.findAvailableSeatsByFlightId(testFlightId);
            System.out.println("Available seats for flight " + testFlightId + ": " + availableSeats.size());
            for (Seat s : availableSeats) {
                System.out.println("  " + s);
            }

            System.out.println("\n==== TEST: FIND BY FLIGHT ID AND SEAT CLASS ====");
            List<Seat> economySeats = seatDAO.findByFlightIdAndSeatClass(testFlightId, SeatClass.ECONOMY);
            System.out.println("Economy seats for flight " + testFlightId + ": " + economySeats.size());
            for (Seat s : economySeats) {
                System.out.println("  " + s);
            }

            System.out.println("\n==== TEST: UPDATE ====");
            found.setAvailable(false);
            found.setSeatClass(SeatClass.BUSINESS);
            boolean updated = seatDAO.update(found);
            System.out.println("Update result: " + updated);

            Seat updatedFromDb = seatDAO.findById(found.getSeatId());
            System.out.println("Updated seat from DB: " + updatedFromDb);

            System.out.println("\n==== TEST: FIND ALL ====");
            List<Seat> all = seatDAO.findAll();
            System.out.println("Total seats in DB: " + all.size());
            for (Seat s : all) {
                System.out.println(s);
            }

            System.out.println("\n==== TEST: DELETE ====");
            boolean deleted = seatDAO.delete(found.getSeatId());
            System.out.println("Delete result: " + deleted);

            Seat afterDelete = seatDAO.findById(found.getSeatId());
            System.out.println("Find after delete (should be null): " + afterDelete);

            System.out.println("\n==== CLEANUP: DELETE TEST SEAT ====");
            // Delete the seat that was created in this test (if it still exists)
            if (saved != null && saved.getSeatId() > 0) {
                Seat testSeat = seatDAO.findById(saved.getSeatId());
                if (testSeat != null) {
                    seatDAO.delete(saved.getSeatId());
                    System.out.println("Deleted test seat with ID: " + saved.getSeatId());
                } else {
                    System.out.println("Test seat already deleted");
                }
            }

            System.out.println("\n==== TESTS COMPLETED ====");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int ensureTestFlightExists() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        // First ensure airports exist
        ensureTestAirportsExist(conn);

        // Ensure aircraft exists
        int aircraftId = ensureTestAircraftExists(conn);

        // Ensure route exists
        int routeId = ensureTestRouteExists(conn);

        // Ensure airline exists
        int airlineId = ensureTestAirlineExists(conn);

        // Check if test flight already exists
        String checkSql = "SELECT flight_id FROM flights WHERE flight_number = 'AC-001'";
        try (PreparedStatement ps = conn.prepareStatement(checkSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("flight_id");
            }
        }

        // Create test flight
        String insertSql = "INSERT INTO flights (flight_number, departure_time, arrival_time, " +
                          "status, available_seats, price, aircraft_id, route_id, airline_id) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "AC-001");
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().plusDays(1)));
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().plusDays(1).plusHours(2)));
            ps.setString(4, "SCHEDULED");
            ps.setInt(5, 150);
            ps.setDouble(6, 299.99);
            ps.setInt(7, aircraftId);
            ps.setInt(8, routeId);
            ps.setInt(9, airlineId);

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("Failed to create test flight");
    }

    private static void ensureTestAirportsExist(Connection conn) throws SQLException {
        String upsertSql = "INSERT INTO airports (airport_code, name, city, country, timezone) " +
                          "VALUES (?, ?, ?, ?, ?) " +
                          "ON DUPLICATE KEY UPDATE name = VALUES(name)";
        try (PreparedStatement ps = conn.prepareStatement(upsertSql)) {
            ps.setString(1, "YYC");
            ps.setString(2, "Calgary International");
            ps.setString(3, "Calgary");
            ps.setString(4, "Canada");
            ps.setString(5, "MST");
            ps.executeUpdate();

            ps.setString(1, "YYZ");
            ps.setString(2, "Toronto Pearson");
            ps.setString(3, "Toronto");
            ps.setString(4, "Canada");
            ps.setString(5, "EST");
            ps.executeUpdate();
        }
    }

    private static int ensureTestAircraftExists(Connection conn) throws SQLException {
        String checkSql = "SELECT aircraft_id FROM aircraft WHERE model = 'TEST-737' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(checkSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("aircraft_id");
            }
        }

        String insertSql = "INSERT INTO aircraft (model, manufacturer, total_seats, seat_configuration, status) " +
                          "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "TEST-737");
            ps.setString(2, "Boeing");
            ps.setInt(3, 180);
            ps.setString(4, "3-3");
            ps.setString(5, "ACTIVE");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create test aircraft");
    }

    private static int ensureTestRouteExists(Connection conn) throws SQLException {
        String checkSql = "SELECT route_id FROM routes WHERE origin_code = 'YYC' AND destination_code = 'YYZ' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(checkSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("route_id");
            }
        }

        String insertSql = "INSERT INTO routes (origin_code, destination_code, distance_km, estimated_duration_minutes) " +
                          "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "YYC");
            ps.setString(2, "YYZ");
            ps.setDouble(3, 2700.0);
            ps.setInt(4, 240);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create test route");
    }

    private static int ensureTestAirlineExists(Connection conn) throws SQLException {
        String checkSql = "SELECT airline_id FROM airlines WHERE code = 'AC' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(checkSql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("airline_id");
            }
        }

        String insertSql = "INSERT INTO airlines (name, code, country) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Air Canada");
            ps.setString(2, "AC");
            ps.setString(3, "Canada");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create test airline");
    }

    private static Seat saveSeatWithFlightId(SeatDAO seatDAO, Seat seat, int flightId) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        
        // Check if seat already exists for this flight
        String checkSql = "SELECT seat_id FROM seats WHERE flight_id = ? AND seat_number = ?";
        try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, flightId);
            checkPs.setString(2, seat.getSeatNumber());
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next()) {
                    // Seat already exists, return it
                    int existingSeatId = rs.getInt("seat_id");
                    return seatDAO.findById(existingSeatId);
                }
            }
        }
        
        // Insert new seat
        String sql = "INSERT INTO seats (flight_id, seat_number, seat_class, is_available) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, flightId);
            ps.setString(2, seat.getSeatNumber());
            ps.setString(3, seat.getSeatClass().name());
            ps.setBoolean(4, seat.isAvailable());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    seat.setSeatId(keys.getInt(1));
                }
            }
        }
        return seat;
    }
}

