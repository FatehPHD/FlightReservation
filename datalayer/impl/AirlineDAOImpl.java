package datalayer.impl;

import businesslogic.entities.Airline;
import datalayer.dao.AirlineDAO;
import datalayer.database.DatabaseConnection;
import datalayer.database.QueryBuilder;
import datalayer.database.TransactionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AirlineDAOImpl implements AirlineDAO {

    private static final String TABLE_NAME = "airlines";
    private static final String COL_ID = "airline_id";
    private static final String COL_NAME = "name";
    private static final String COL_CODE = "code";
    private static final String COL_COUNTRY = "country";

    // Shared connection from DatabaseConnection (do not close in DAO)
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    private Map<String, Object> toColumnMap(Airline airline, boolean includeId) {
        Map<String, Object> map = new LinkedHashMap<>();

        if (includeId) {
            map.put(COL_ID, airline.getAirlineId());
        }
        map.put(COL_NAME, airline.getName());
        map.put(COL_CODE, airline.getCode());
        map.put(COL_COUNTRY, airline.getCountry());

        return map;
    }

    private Airline mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt(COL_ID);
        String name = rs.getString(COL_NAME);
        String code = rs.getString(COL_CODE);
        String country = rs.getString(COL_COUNTRY);

        return new Airline(id, name, code, country);
    }

    @Override
    public Airline save(Airline airline) throws SQLException {
        Map<String, Object> columns = toColumnMap(airline, false);
        String sql = QueryBuilder.createInsertQuery(TABLE_NAME, columns);

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            TransactionManager.begin(conn);

            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            int index = 1;
            for (Object value : columns.values()) {
                ps.setObject(index++, value);
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    airline.setAirlineId(keys.getInt(1));
                }
            }

            TransactionManager.commit(conn);
            return airline;
        } catch (SQLException e) {
            if (conn != null) {
                TransactionManager.rollback(conn);
            }
            throw e;
        } finally {
            if (ps != null) {
                try { ps.close(); } catch (SQLException ignore) {}
            }
        }
    }

    @Override
    public Airline findById(Integer id) throws SQLException {
        String sql = QueryBuilder.createSelectByIdQuery(TABLE_NAME, COL_ID);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException ignore) {}
            }
            if (ps != null) {
                try { ps.close(); } catch (SQLException ignore) {}
            }
        }
    }

    @Override
    public List<Airline> findAll() throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME;
        List<Airline> airlines = new ArrayList<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                airlines.add(mapRow(rs));
            }

            return airlines;
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException ignore) {}
            }
            if (ps != null) {
                try { ps.close(); } catch (SQLException ignore) {}
            }
        }
    }

    @Override
    public boolean update(Airline airline) throws SQLException {
        Map<String, Object> columns = toColumnMap(airline, true);
        String sql = QueryBuilder.createUpdateQuery(TABLE_NAME, columns, COL_ID);

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            TransactionManager.begin(conn);

            ps = conn.prepareStatement(sql);

            int index = 1;
            for (Map.Entry<String, Object> entry : columns.entrySet()) {
                if (!entry.getKey().equals(COL_ID)) {
                    ps.setObject(index++, entry.getValue());
                }
            }
            ps.setObject(index, columns.get(COL_ID));

            int rows = ps.executeUpdate();
            TransactionManager.commit(conn);
            return rows > 0;
        } catch (SQLException e) {
            if (conn != null) {
                TransactionManager.rollback(conn);
            }
            throw e;
        } finally {
            if (ps != null) {
                try { ps.close(); } catch (SQLException ignore) {}
            }
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = QueryBuilder.createDeleteByIdQuery(TABLE_NAME, COL_ID);

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            TransactionManager.begin(conn);

            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            int rows = ps.executeUpdate();
            TransactionManager.commit(conn);
            return rows > 0;
        } catch (SQLException e) {
            if (conn != null) {
                TransactionManager.rollback(conn);
            }
            throw e;
        } finally {
            if (ps != null) {
                try { ps.close(); } catch (SQLException ignore) {}
            }
        }
    }
}
