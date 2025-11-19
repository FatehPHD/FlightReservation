package datalayer.impl;

import businesslogic.entities.Airport;
import datalayer.dao.AirportDAO;
import datalayer.database.DatabaseConnection;
import datalayer.database.TransactionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AirportDAOImpl implements AirportDAO {

    private static final String TABLE_NAME   = "airports";
    private static final String COL_CODE     = "airport_code";
    private static final String COL_NAME     = "name";
    private static final String COL_CITY     = "city";
    private static final String COL_COUNTRY  = "country";
    private static final String COL_TIMEZONE = "timezone";

    // Shared connection from DatabaseConnection (do not close in DAO)
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Airport mapRow(ResultSet rs) throws SQLException {
        return new Airport(
                rs.getString(COL_CODE),
                rs.getString(COL_NAME),
                rs.getString(COL_CITY),
                rs.getString(COL_COUNTRY),
                rs.getString(COL_TIMEZONE)
        );
    }

    @Override
    public Airport save(Airport airport) throws SQLException {
        String sql =
                "INSERT INTO " + TABLE_NAME +
                " (" + COL_CODE + ", " + COL_NAME + ", " + COL_CITY + ", " +
                COL_COUNTRY + ", " + COL_TIMEZONE + ") VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            TransactionManager.begin(conn);

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, airport.getAirportCode());
            stmt.setString(2, airport.getName());
            stmt.setString(3, airport.getCity());
            stmt.setString(4, airport.getCountry());
            stmt.setString(5, airport.getTimezone());

            stmt.executeUpdate();
            TransactionManager.commit(conn);
            return airport;
        } catch (SQLException e) {
            if (conn != null) {
                TransactionManager.rollback(conn);
            }
            throw e;
        } finally {
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException ignore) {}
            }
        }
    }

    @Override
    public Airport findById(String code) throws SQLException {
        String sql =
                "SELECT " + COL_CODE + ", " + COL_NAME + ", " + COL_CITY + ", " +
                COL_COUNTRY + ", " + COL_TIMEZONE +
                " FROM " + TABLE_NAME +
                " WHERE " + COL_CODE + " = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, code);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException ignore) {}
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException ignore) {}
            }
        }
    }

    @Override
    public List<Airport> findAll() throws SQLException {
        String sql =
                "SELECT " + COL_CODE + ", " + COL_NAME + ", " + COL_CITY + ", " +
                COL_COUNTRY + ", " + COL_TIMEZONE +
                " FROM " + TABLE_NAME;

        List<Airport> airports = new ArrayList<>();

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                airports.add(mapRow(rs));
            }
            return airports;
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException ignore) {}
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException ignore) {}
            }
        }
    }

    @Override
    public boolean update(Airport airport) throws SQLException {
        String sql =
                "UPDATE " + TABLE_NAME +
                " SET " + COL_NAME + " = ?, " +
                         COL_CITY + " = ?, " +
                         COL_COUNTRY + " = ?, " +
                         COL_TIMEZONE + " = ? " +
                " WHERE " + COL_CODE + " = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            TransactionManager.begin(conn);

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, airport.getName());
            stmt.setString(2, airport.getCity());
            stmt.setString(3, airport.getCountry());
            stmt.setString(4, airport.getTimezone());
            stmt.setString(5, airport.getAirportCode());

            int rows = stmt.executeUpdate();
            TransactionManager.commit(conn);
            return rows > 0;
        } catch (SQLException e) {
            if (conn != null) {
                TransactionManager.rollback(conn);
            }
            throw e;
        } finally {
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException ignore) {}
            }
        }
    }

    @Override
    public boolean delete(String code) throws SQLException {
        String sql =
                "DELETE FROM " + TABLE_NAME +
                " WHERE " + COL_CODE + " = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            TransactionManager.begin(conn);

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, code);

            int rows = stmt.executeUpdate();
            TransactionManager.commit(conn);
            return rows > 0;
        } catch (SQLException e) {
            if (conn != null) {
                TransactionManager.rollback(conn);
            }
            throw e;
        } finally {
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException ignore) {}
            }
        }
    }
}
