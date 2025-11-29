package datalayer.impl;

import businesslogic.entities.Seat;
import businesslogic.entities.enums.SeatClass;
import datalayer.dao.SeatDAO;
import datalayer.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeatDAOImpl implements SeatDAO {

    private static final String INSERT_SQL =
            "INSERT INTO seats (flight_id, seat_number, seat_class, is_available) " +
            "VALUES (?, ?, ?, ?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT * FROM seats WHERE seat_id = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT * FROM seats";

    private static final String SELECT_BY_FLIGHT_ID_SQL =
            "SELECT * FROM seats WHERE flight_id = ?";

    private static final String SELECT_AVAILABLE_BY_FLIGHT_ID_SQL =
            "SELECT * FROM seats WHERE flight_id = ? AND is_available = TRUE";

    private static final String SELECT_BY_FLIGHT_ID_AND_CLASS_SQL =
            "SELECT * FROM seats WHERE flight_id = ? AND seat_class = ?";

    private static final String UPDATE_SQL =
            "UPDATE seats SET flight_id = ?, seat_number = ?, seat_class = ?, is_available = ? " +
            "WHERE seat_id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM seats WHERE seat_id = ?";

    @Override
    public Seat save(Seat seat) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS
        )) {
            // Note: Seat entity doesn't have flightId field, so we need to get it from somewhere
            // For now, assuming it's passed via a different mechanism or set separately
            // This is a limitation - Seat should have a Flight reference or flightId field
            stmt.setInt(1, 0); // Placeholder - should be set properly when Seat has flightId
            stmt.setString(2, seat.getSeatNumber());
            stmt.setString(3, seat.getSeatClass().name());
            stmt.setBoolean(4, seat.isAvailable());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Saving seat failed, no rows affected.");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    seat.setSeatId(keys.getInt(1));
                }
            }
        }

        return seat;
    }



    @Override
    public Seat findById(Integer id) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }

    @Override
    public List<Seat> findAll() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Seat> list = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }

        return list;
    }

    @Override
    public List<Seat> findByFlightId(Integer flightId) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Seat> list = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_FLIGHT_ID_SQL)) {
            stmt.setInt(1, flightId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    @Override
    public List<Seat> findAvailableSeatsByFlightId(Integer flightId) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Seat> list = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_AVAILABLE_BY_FLIGHT_ID_SQL)) {
            stmt.setInt(1, flightId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    @Override
    public List<Seat> findByFlightIdAndSeatClass(Integer flightId, SeatClass seatClass) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Seat> list = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_FLIGHT_ID_AND_CLASS_SQL)) {
            stmt.setInt(1, flightId);
            stmt.setString(2, seatClass.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    @Override
    public boolean update(Seat seat) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        // Get existing flight_id from database to preserve it
        Integer existingFlightId = getFlightIdForSeat(conn, seat.getSeatId());
        if (existingFlightId == null) {
            throw new SQLException("Seat not found: " + seat.getSeatId());
        }

        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            stmt.setInt(1, existingFlightId); // Preserve existing flight_id
            stmt.setString(2, seat.getSeatNumber());
            stmt.setString(3, seat.getSeatClass().name());
            stmt.setBoolean(4, seat.isAvailable());
            stmt.setInt(5, seat.getSeatId());

            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    private Integer getFlightIdForSeat(Connection conn, int seatId) throws SQLException {
        String sql = "SELECT flight_id FROM seats WHERE seat_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, seatId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("flight_id");
                }
            }
        }
        return null;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);

            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    private Seat mapRow(ResultSet rs) throws SQLException {
        Seat seat = new Seat();

        seat.setSeatId(rs.getInt("seat_id"));
        seat.setSeatNumber(rs.getString("seat_number"));
        
        String seatClassStr = rs.getString("seat_class");
        if (seatClassStr != null) {
            seat.setSeatClass(SeatClass.valueOf(seatClassStr));
        }
        
        seat.setAvailable(rs.getBoolean("is_available"));

        return seat;
    }

    @Override
    public void createSeatsForFlight(int flightId, int totalSeats, String seatConfiguration, int availableSeats) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        // Validate availableSeats doesn't exceed totalSeats
        if (availableSeats > totalSeats) {
            throw new SQLException("Available seats (" + availableSeats + ") cannot exceed total seats (" + totalSeats + ")");
        }

        // Calculate seats per row from configuration (e.g., "3-3" = 6, "2-4-2" = 8)
        int seatsPerRow = calculateSeatsPerRow(seatConfiguration);

        try (PreparedStatement stmt = conn.prepareStatement(
            "INSERT INTO seats (flight_id, seat_number, seat_class, is_available) VALUES (?, ?, ?, ?)"
        )) {
            for (int i = 1; i <= totalSeats; i++) {
                String seatNumber = generateSeatNumber(i, seatsPerRow);

                stmt.setInt(1, flightId);
                stmt.setString(2, seatNumber);
                stmt.setString(3, "ECONOMY");
                // Mark first 'availableSeats' as available, rest as unavailable
                stmt.setBoolean(4, i <= availableSeats);

                stmt.addBatch();
            }

            stmt.executeBatch();
        }
    }

    /**
     * Calculate seats per row from seat configuration string.
     * Examples: "3-3" = 6, "2-4-2" = 8, "2-2" = 4
     */
    private int calculateSeatsPerRow(String seatConfiguration) {
        if (seatConfiguration == null || seatConfiguration.trim().isEmpty()) {
            return 6; // Default to 6 seats per row
        }
        
        // Parse configuration like "3-3" or "2-4-2"
        String[] parts = seatConfiguration.split("-");
        int total = 0;
        for (String part : parts) {
            try {
                total += Integer.parseInt(part.trim());
            } catch (NumberFormatException e) {
                // If parsing fails, default to 6
                return 6;
            }
        }
        
        return total > 0 ? total : 6; // Default to 6 if calculation fails
    }

    /**
     * Generate seat number based on index and seats per row.
     * Examples: index 1, seatsPerRow 6 -> "1A", index 7, seatsPerRow 6 -> "2A"
     */
    private String generateSeatNumber(int index, int seatsPerRow) {
        int row = ((index - 1) / seatsPerRow) + 1;
        int colIndex = ((index - 1) % seatsPerRow);
        char col = (char) ('A' + colIndex);
        return row + String.valueOf(col);
    }

    @Override
    public void updateSeatAvailability(int flightId, int availableSeats) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        
        // Get all seats for this flight, ordered by seat_id to ensure consistent ordering
        List<Seat> allSeats = findByFlightId(flightId);
        
        if (allSeats.isEmpty()) {
            return; // No seats to update
        }
        
        if (availableSeats > allSeats.size()) {
            throw new SQLException("Available seats (" + availableSeats + ") cannot exceed total seats (" + allSeats.size() + ")");
        }
        
        // Sort seats by seat_id to ensure consistent ordering
        allSeats.sort((s1, s2) -> Integer.compare(s1.getSeatId(), s2.getSeatId()));
        
        // Update seats: first 'availableSeats' as available, rest as unavailable
        try (PreparedStatement stmt = conn.prepareStatement(
            "UPDATE seats SET is_available = ? WHERE seat_id = ?"
        )) {
            for (int i = 0; i < allSeats.size(); i++) {
                Seat seat = allSeats.get(i);
                boolean shouldBeAvailable = (i < availableSeats);
                
                // Only update if availability changed
                if (seat.isAvailable() != shouldBeAvailable) {
                    stmt.setBoolean(1, shouldBeAvailable);
                    stmt.setInt(2, seat.getSeatId());
                    stmt.addBatch();
                }
            }
            
            stmt.executeBatch();
        }
    }

}

