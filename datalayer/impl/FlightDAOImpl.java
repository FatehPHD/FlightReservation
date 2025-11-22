package datalayer.impl;

import businesslogic.entities.Aircraft;
import businesslogic.entities.Airline;
import businesslogic.entities.Flight;
import businesslogic.entities.Route;
import businesslogic.entities.enums.FlightStatus;
import datalayer.dao.FlightDAO;
import datalayer.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlightDAOImpl implements FlightDAO {

    private final DatabaseConnection db;

    public FlightDAOImpl() {
        this.db = DatabaseConnection.getInstance();
    }

    // ---------- Helper to map a ResultSet row to a Flight ----------
    private Flight mapRow(ResultSet rs) throws SQLException {
        Flight flight = new Flight();

        flight.setFlightId(rs.getInt("flight_id"));
        flight.setFlightNumber(rs.getString("flight_number"));

        Timestamp depTs = rs.getTimestamp("departure_time");
        if (depTs != null) flight.setDepartureTime(depTs.toLocalDateTime());

        Timestamp arrTs = rs.getTimestamp("arrival_time");
        if (arrTs != null) flight.setArrivalTime(arrTs.toLocalDateTime());

        String statusStr = rs.getString("status");
        if (statusStr != null) flight.setStatus(FlightStatus.valueOf(statusStr));

        flight.setAvailableSeats(rs.getInt("available_seats"));
        flight.setPrice(rs.getDouble("price"));

        // Aircraft
        Aircraft aircraft = new Aircraft();
        aircraft.setAircraftId(rs.getInt("aircraft_id"));
        flight.setAircraft(aircraft);

        // Route
        Route route = new Route();
        route.setRouteId(rs.getInt("route_id"));
        flight.setRoute(route);

        // Airline
        Airline airline = new Airline();
        airline.setAirlineId(rs.getInt("airline_id"));
        flight.setAirline(airline);

        return flight;
    }

    // ---------- BaseDAO implementation ----------

    @Override
    public Flight save(Flight flight) throws SQLException {
        String sql =
                "INSERT INTO flights (" +
                        "flight_number, departure_time, arrival_time, status, " +
                        "available_seats, price, aircraft_id, route_id, airline_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = db.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, flight.getFlightNumber());
            ps.setTimestamp(2, Timestamp.valueOf(flight.getDepartureTime()));
            ps.setTimestamp(3, Timestamp.valueOf(flight.getArrivalTime()));
            ps.setString(4, flight.getStatus().name());
            ps.setInt(5, flight.getAvailableSeats());
            ps.setDouble(6, flight.getPrice());
            ps.setInt(7, flight.getAircraft().getAircraftId());
            ps.setInt(8, flight.getRoute().getRouteId());
            ps.setInt(9, flight.getAirline().getAirlineId());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    flight.setFlightId(keys.getInt(1));
                }
            }
        }

        return flight;
    }

    @Override
    public Flight findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM flights WHERE flight_id = ?";
        Connection conn = db.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);   // Integer autounboxes to int

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }

        return null;
    }

    @Override
    public List<Flight> findAll() throws SQLException {
        List<Flight> flights = new ArrayList<>();
        String sql = "SELECT * FROM flights";

        Connection conn = db.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) flights.add(mapRow(rs));
        }

        return flights;
    }

    @Override
    public boolean update(Flight flight) throws SQLException {
        String sql =
                "UPDATE flights SET " +
                        "flight_number = ?, departure_time = ?, arrival_time = ?, status = ?, " +
                        "available_seats = ?, price = ?, aircraft_id = ?, route_id = ?, airline_id = ? " +
                        "WHERE flight_id = ?";

        Connection conn = db.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, flight.getFlightNumber());
            ps.setTimestamp(2, Timestamp.valueOf(flight.getDepartureTime()));
            ps.setTimestamp(3, Timestamp.valueOf(flight.getArrivalTime()));
            ps.setString(4, flight.getStatus().name());
            ps.setInt(5, flight.getAvailableSeats());
            ps.setDouble(6, flight.getPrice());
            ps.setInt(7, flight.getAircraft().getAircraftId());
            ps.setInt(8, flight.getRoute().getRouteId());
            ps.setInt(9, flight.getAirline().getAirlineId());
            ps.setInt(10, flight.getFlightId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM flights WHERE flight_id = ?";
        Connection conn = db.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);   // Integer → int
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Flight findByFlightNumber(String flightNumber) throws SQLException {
        String sql = "SELECT * FROM flights WHERE flight_number = ?";

        Connection conn = db.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, flightNumber);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }

        return null;
    }

    @Override
    public List<Flight> findByStatus(FlightStatus status) throws SQLException {
        String sql = "SELECT * FROM flights WHERE status = ?";
        List<Flight> flights = new ArrayList<>();

        Connection conn = db.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) flights.add(mapRow(rs));
            }
        }

        return flights;
    }

    @Override
    public List<Flight> findByRoute(String origin, String destination) throws SQLException {
        String sql =
                "SELECT f.* FROM flights f " +
                        "JOIN routes r ON f.route_id = r.route_id " +
                        "WHERE r.origin_code = ? AND r.destination_code = ?";

        List<Flight> flights = new ArrayList<>();
        Connection conn = db.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, origin);
            ps.setString(2, destination);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) flights.add(mapRow(rs));
            }
        }

        return flights;
    }

    @Override
    public List<Flight> findByDepartureRange(LocalDateTime from, LocalDateTime to) throws SQLException {
        String sql =
                "SELECT * FROM flights " +
                        "WHERE departure_time BETWEEN ? AND ? " +
                        "ORDER BY departure_time";

        List<Flight> flights = new ArrayList<>();
        Connection conn = db.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(from));
            ps.setTimestamp(2, Timestamp.valueOf(to));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) flights.add(mapRow(rs));
            }
        }

        return flights;
    }
}
