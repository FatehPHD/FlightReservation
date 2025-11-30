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
            // NOTE: This save() is not typically used for bulk flight seeding.
            // createSeatsForFlight(...) handles normal seat creation with a real flightId.
            stmt.setInt(1, 0); // Placeholder; real code should pass a proper flightId
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

        // Preserve existing flight_id from DB
        Integer existingFlightId = getFlightIdForSeat(conn, seat.getSeatId());
        if (existingFlightId == null) {
            throw new SQLException("Seat not found: " + seat.getSeatId());
        }

        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            stmt.setInt(1, existingFlightId);
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

        if (availableSeats > totalSeats) {
            throw new SQLException("Available seats (" + availableSeats + ") cannot exceed total seats (" + totalSeats + ")");
        }

        int seatsPerRow = calculateSeatsPerRow(seatConfiguration);

        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO seats (flight_id, seat_number, seat_class, is_available) VALUES (?, ?, ?, ?)"
        )) {
            for (int i = 1; i <= totalSeats; i++) {
                String seatNumber = generateSeatNumber(i, seatsPerRow);

                stmt.setInt(1, flightId);
                stmt.setString(2, seatNumber);
                stmt.setString(3, "ECONOMY");
                stmt.setBoolean(4, i <= availableSeats);

                stmt.addBatch();
            }

            stmt.executeBatch();
        }
    }

    private int calculateSeatsPerRow(String seatConfiguration) {
        if (seatConfiguration == null || seatConfiguration.trim().isEmpty()) {
            return 6;
        }

        String[] parts = seatConfiguration.split("-");
        int total = 0;
        for (String part : parts) {
            try {
                total += Integer.parseInt(part.trim());
            } catch (NumberFormatException e) {
                return 6;
            }
        }

        return total > 0 ? total : 6;
    }

    private String generateSeatNumber(int index, int seatsPerRow) {
        int row = ((index - 1) / seatsPerRow) + 1;
        int colIndex = ((index - 1) % seatsPerRow);
        char col = (char) ('A' + colIndex);
        return row + String.valueOf(col);
    }

    @Override
    public void updateSeatAvailability(int flightId, int availableSeats) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        List<Seat> allSeats = findByFlightId(flightId);
        if (allSeats.isEmpty()) {
            return;
        }

        if (availableSeats > allSeats.size()) {
            throw new SQLException("Available seats (" + availableSeats + ") cannot exceed total seats (" + allSeats.size() + ")");
        }

        // Count how many seats are currently available
        int currentAvailable = 0;
        for (Seat s : allSeats) {
            if (s.isAvailable()) {
                currentAvailable++;
            }
        }

        // If the count already matches, do nothing.
        // This prevents overwriting explicit seat selections made via SeatService/ReservationService.
        if (currentAvailable == availableSeats) {
            return;
        }

        // Fallback behavior: if you ever call this for admin-type bulk changes,
        // you can still adjust the distribution. For safety, keep current pattern
        // but trim or open seats starting from the end.
        allSeats.sort((s1, s2) -> Integer.compare(s1.getSeatId(), s2.getSeatId()));

        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE seats SET is_available = ? WHERE seat_id = ?"
        )) {
            if (availableSeats < currentAvailable) {
                // Need to reduce the number of available seats: close some currently available ones.
                int toClose = currentAvailable - availableSeats;

                for (int i = allSeats.size() - 1; i >= 0 && toClose > 0; i--) {
                    Seat seat = allSeats.get(i);
                    if (seat.isAvailable()) {
                        stmt.setBoolean(1, false);
                        stmt.setInt(2, seat.getSeatId());
                        stmt.addBatch();
                        toClose--;
                    }
                }
            } else {
                // Need to increase the number of available seats: open some currently unavailable ones.
                int toOpen = availableSeats - currentAvailable;

                for (Seat seat : allSeats) {
                    if (!seat.isAvailable() && toOpen > 0) {
                        stmt.setBoolean(1, true);
                        stmt.setInt(2, seat.getSeatId());
                        stmt.addBatch();
                        toOpen--;
                    }
                }
            }

            stmt.executeBatch();
        }
    }
}
