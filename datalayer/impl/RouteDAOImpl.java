package datalayer.impl;

import businesslogic.entities.Airport;
import businesslogic.entities.Route;
import datalayer.dao.RouteDAO;
import datalayer.dao.AirportDAO;
import datalayer.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RouteDAOImpl implements RouteDAO {

    private static final String INSERT_SQL =
            "INSERT INTO routes (origin_code, destination_code, distance_km, estimated_duration_minutes) " +
            "VALUES (?, ?, ?, ?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT route_id, origin_code, destination_code, distance_km, estimated_duration_minutes " +
            "FROM routes WHERE route_id = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT route_id, origin_code, destination_code, distance_km, estimated_duration_minutes " +
            "FROM routes";

    private static final String SELECT_BY_AIRPORT_CODE_SQL =
            "SELECT route_id, origin_code, destination_code, distance_km, estimated_duration_minutes " +
            "FROM routes WHERE origin_code = ? OR destination_code = ?";

    private static final String UPDATE_SQL =
            "UPDATE routes SET origin_code = ?, destination_code = ?, distance_km = ?, " +
            "estimated_duration_minutes = ? WHERE route_id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM routes WHERE route_id = ?";

    private final Connection connection;
    private AirportDAO airportDAO;

    public RouteDAOImpl() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
        this.airportDAO = new AirportDAOImpl();
    }

    @Override
    public Route save(Route route) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, route.getOrigin().getAirportCode());
            ps.setString(2, route.getDestination().getAirportCode());
            ps.setDouble(3, route.getDistance());
            ps.setInt(4, route.getEstimatedDuration());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    route.setRouteId(generatedId);
                }
            }
        }
        return route;
    }

    @Override
    public Route findById(Integer id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_SQL)) {
            ps.setInt(1, id);  // Integer autoboxes to int
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToRoute(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Route> findAll() throws SQLException {
        List<Route> routes = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                routes.add(mapRowToRoute(rs));
            }
        }
        return routes;
    }

    @Override
    public List<Route> findByAirportCode(String airportCode) throws SQLException {
        List<Route> routes = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_AIRPORT_CODE_SQL)) {
            ps.setString(1, airportCode);
            ps.setString(2, airportCode);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    routes.add(mapRowToRoute(rs));
                }
            }
        }
        return routes;
    }

    @Override
    public boolean update(Route route) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_SQL)) {

            ps.setString(1, route.getOrigin().getAirportCode());
            ps.setString(2, route.getDestination().getAirportCode());
            ps.setDouble(3, route.getDistance());
            ps.setInt(4, route.getEstimatedDuration());
            ps.setInt(5, route.getRouteId());

            int affected = ps.executeUpdate();
            return affected == 1;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            return affected == 1;
        }
    }

    private Route mapRowToRoute(ResultSet rs) throws SQLException {
        int routeId = rs.getInt("route_id");
        String originCode = rs.getString("origin_code");
        String destinationCode = rs.getString("destination_code");
        double distance = rs.getDouble("distance_km");
        int duration = rs.getInt("estimated_duration_minutes");

        // Load full Airport objects using AirportDAO
        Airport origin = airportDAO.findById(originCode);
        if (origin == null) {
            // Fallback: create minimal Airport object if not found
            origin = new Airport();
        origin.setAirportCode(originCode);
        }

        Airport destination = airportDAO.findById(destinationCode);
        if (destination == null) {
            // Fallback: create minimal Airport object if not found
            destination = new Airport();
        destination.setAirportCode(destinationCode);
        }

        Route route = new Route();
        route.setRouteId(routeId);
        route.setOrigin(origin);
        route.setDestination(destination);
        route.setDistance(distance);
        route.setEstimatedDuration(duration);

        return route;
    }
}
