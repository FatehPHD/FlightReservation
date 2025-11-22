// File: datalayer/impl/SeatDAOImpl.java
package datalayer.impl;

import businesslogic.entities.Seat;
import businesslogic.entities.enums.SeatClass;
import datalayer.dao.SeatDAO;
import datalayer.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeatDAOImpl implements SeatDAO {

    private final Connection connection;

    public SeatDAOImpl() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // ---------------- BaseDAO<Seat, Integer> ----------------

    @Override
    public Seat save(Seat seat) throws SQLException {
        String sql = "INSERT INTO seats (flight_id, seat_number, seat_class, is_available) " +
                     "VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, seat.getFlightId());                // from Seat.flightId
            stmt.setString(2, seat.getSeatNumber());
            stmt.setString(3, seat.getSeatClass().name());
            stmt.setBoolean(4, seat.isAvailable());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Saving seat failed, no rows affected.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    seat.setSeatId(rs.getInt(1));               // seatId is int in your entity
                }
            }
        }

        return seat;
    }

    @Override
    public Seat findById(Integer id) throws SQLException {
        String sql = "SELECT seat_id, flight_id, seat_number, seat_class, is_available " +
                     "FROM seats WHERE seat_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToSeat(rs);
                }
            }
        }

        return null;
    }

    @Override
    public List<Seat> findAll() throws SQLException {
        String sql = "SELECT seat_id, flight_id, seat_number, seat_class, is_available FROM seats";
        List<Seat> seats = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                seats.add(mapRowToSeat(rs));
            }
        }

        return seats;
    }

    @Override
    public boolean update(Seat seat) throws SQLException {
        String sql = "UPDATE seats " +
                     "SET flight_id = ?, seat_number = ?, seat_class = ?, is_available = ? " +
                     "WHERE seat_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, seat.getFlightId());
            stmt.setString(2, seat.getSeatNumber());
            stmt.setString(3, seat.getSeatClass().name());
            stmt.setBoolean(4, seat.isAvailable());
            stmt.setInt(5, seat.getSeatId());

            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM seats WHERE seat_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    // ---------------- Custom SeatDAO methods ----------------

    @Override
    public List<Seat> findByFlightId(long flightId) throws SQLException {
        String sql = "SELECT seat_id, flight_id, seat_number, seat_class, is_available " +
                     "FROM seats WHERE flight_id = ? ORDER BY seat_number";
        List<Seat> seats = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, flightId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seats.add(mapRowToSeat(rs));
                }
            }
        }

        return seats;
    }

    @Override
    public List<Seat> findAvailableByFlight(long flightId) throws SQLException {
        String sql = "SELECT seat_id, flight_id, seat_number, seat_class, is_available " +
                     "FROM seats WHERE flight_id = ? AND is_available = TRUE " +
                     "ORDER BY seat_number";
        List<Seat> seats = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, flightId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    seats.add(mapRowToSeat(rs));
                }
            }
        }

        return seats;
    }

    @Override
    public Seat findByFlightAndSeatNumber(long flightId, String seatNumber) throws SQLException {
        String sql = "SELECT seat_id, flight_id, seat_number, seat_class, is_available " +
                     "FROM seats WHERE flight_id = ? AND seat_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, flightId);
            stmt.setString(2, seatNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToSeat(rs);
                }
            }
        }

        return null;
    }

    @Override
    public boolean updateAvailability(int seatId, boolean available) throws SQLException {
        String sql = "UPDATE seats SET is_available = ? WHERE seat_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, available);
            stmt.setInt(2, seatId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    // ---------------- Helper ----------------

    private Seat mapRowToSeat(ResultSet rs) throws SQLException {
        Seat seat = new Seat();
        seat.setSeatId(rs.getInt("seat_id"));
        seat.setSeatNumber(rs.getString("seat_number"));
        seat.setSeatClass(SeatClass.valueOf(rs.getString("seat_class")));
        seat.setAvailable(rs.getBoolean("is_available"));
        seat.setFlightId(rs.getInt("flight_id"));
        return seat;
    }
}
