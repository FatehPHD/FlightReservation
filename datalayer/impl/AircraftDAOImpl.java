package datalayer.impl;

import businesslogic.entities.Aircraft;
import datalayer.dao.AircraftDAO;
import datalayer.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AircraftDAOImpl implements AircraftDAO {

    private static final String INSERT_SQL =
            "INSERT INTO aircraft (model, manufacturer, total_seats, seat_configuration, status) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT * FROM aircraft WHERE aircraft_id = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT * FROM aircraft";

    private static final String UPDATE_SQL =
            "UPDATE aircraft SET model = ?, manufacturer = ?, total_seats = ?, " +
            "seat_configuration = ?, status = ? WHERE aircraft_id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM aircraft WHERE aircraft_id = ?";

    @Override
    public Aircraft save(Aircraft aircraft) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS
        )) {
            stmt.setString(1, aircraft.getModel());
            stmt.setString(2, aircraft.getManufacturer());
            stmt.setInt(3, aircraft.getTotalSeats());
            stmt.setString(4, aircraft.getSeatConfiguration());
            stmt.setString(5, aircraft.getStatus());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Saving aircraft failed, no rows affected.");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    aircraft.setAircraftId(keys.getInt(1));
                }
            }
        }

        return aircraft;
    }

    @Override
    public Aircraft findById(Integer id) throws SQLException {
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
    public List<Aircraft> findAll() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Aircraft> list = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }

        return list;
    }

    @Override
    public boolean update(Aircraft aircraft) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, aircraft.getModel());
            stmt.setString(2, aircraft.getManufacturer());
            stmt.setInt(3, aircraft.getTotalSeats());
            stmt.setString(4, aircraft.getSeatConfiguration());
            stmt.setString(5, aircraft.getStatus());
            stmt.setInt(6, aircraft.getAircraftId());

            int affected = stmt.executeUpdate();
            return affected > 0;
        }
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

    private Aircraft mapRow(ResultSet rs) throws SQLException {
        Aircraft a = new Aircraft();

        a.setAircraftId(rs.getInt("aircraft_id"));
        a.setModel(rs.getString("model"));
        a.setManufacturer(rs.getString("manufacturer"));
        a.setTotalSeats(rs.getInt("total_seats"));
        a.setSeatConfiguration(rs.getString("seat_configuration"));
        a.setStatus(rs.getString("status"));

        return a;
    }
}
