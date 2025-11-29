package datalayer.impl;

import businesslogic.entities.Flight;
import businesslogic.entities.Aircraft;
import businesslogic.entities.Route;
import businesslogic.entities.enums.FlightStatus;
import datalayer.dao.FlightDAO;
import datalayer.dao.AircraftDAO;
import datalayer.dao.RouteDAO;
import datalayer.dao.SeatDAO;
import datalayer.database.DatabaseConnection;
import datalayer.database.TransactionManager;

import java.sql.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class FlightDAOImpl implements FlightDAO {
    
    // UTC Calendar for consistent timezone handling with MySQL (serverTimezone=UTC)
    private static final Calendar UTC_CALENDAR = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    private static final String INSERT_SQL =
            "INSERT INTO flights (flight_number, departure_time, arrival_time, status, " +
            "available_seats, price, aircraft_id, route_id, airline_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT * FROM flights WHERE flight_id = ?";

    private static final String SELECT_BY_FLIGHT_NUMBER_SQL =
            "SELECT * FROM flights WHERE flight_number = ?";

    private static final String SELECT_BY_AIRCRAFT_ID_SQL =
            "SELECT * FROM flights WHERE aircraft_id = ?";

    private static final String SELECT_BY_AIRLINE_ID_SQL =
            "SELECT * FROM flights WHERE airline_id = ?";

    private static final String SELECT_BY_ROUTE_ID_SQL =
            "SELECT * FROM flights WHERE route_id = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT * FROM flights";

    private static final String UPDATE_SQL =
            "UPDATE flights SET flight_number = ?, departure_time = ?, arrival_time = ?, " +
            "status = ?, available_seats = ?, price = ?, aircraft_id = ?, route_id = ?, airline_id = ? " +
            "WHERE flight_id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM flights WHERE flight_id = ?";

    private AircraftDAO aircraftDAO;
    private RouteDAO routeDAO;

    public FlightDAOImpl() throws SQLException {
        this.aircraftDAO = new AircraftDAOImpl();
        this.routeDAO = new RouteDAOImpl();
    }

    @Override
    public Flight save(Flight flight) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
    
        // Get airline_id from flight number prefix (e.g., "AC" from "AC123")
        Integer airlineId = getAirlineIdFromFlightNumber(flight.getFlightNumber());
        if (airlineId == null) {
            throw new SQLException("Cannot find airline for flight number: " + flight.getFlightNumber());
        }
    
        // Validate aircraft exists and get total seats
        if (flight.getAircraft() == null) {
            throw new SQLException("Aircraft is required for flight");
        }
        Aircraft aircraft = aircraftDAO.findById(flight.getAircraft().getAircraftId());
        if (aircraft == null) {
            throw new SQLException("Aircraft not found: " + flight.getAircraft().getAircraftId());
        }
        int totalSeats = aircraft.getTotalSeats();
        
        // Begin transaction to ensure atomicity of flight and seat creation
        TransactionManager.begin(conn);
        int generatedFlightId;
        
        try {
            // Save flight
            try (PreparedStatement stmt = conn.prepareStatement(
                    INSERT_SQL, Statement.RETURN_GENERATED_KEYS
            )) {
                stmt.setString(1, flight.getFlightNumber());
                // Convert LocalDateTime to Timestamp treating it as UTC (no timezone conversion)
                // This ensures the exact time entered is stored in the database
                Timestamp depTs = Timestamp.from(flight.getDepartureTime().atZone(ZoneId.of("UTC")).toInstant());
                Timestamp arrTs = Timestamp.from(flight.getArrivalTime().atZone(ZoneId.of("UTC")).toInstant());
                stmt.setTimestamp(2, depTs, UTC_CALENDAR);
                stmt.setTimestamp(3, arrTs, UTC_CALENDAR);
                stmt.setString(4, flight.getStatus().name());
                stmt.setInt(5, flight.getAvailableSeats());
                stmt.setDouble(6, flight.getPrice());
                stmt.setInt(7, flight.getAircraft().getAircraftId());
                stmt.setInt(8, flight.getRoute().getRouteId());
                stmt.setInt(9, airlineId);
        
                int affected = stmt.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Saving flight failed, no rows affected.");
                }
        
                // Get generated flight_id
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedFlightId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated flight_id.");
                    }
                }
            }
        
            // Automatically create seats for the new flight
            // Use aircraft's totalSeats and seatConfiguration
            // because the aircraft determines the actual number of seats and layout
            // Mark only the specified available seats as available
            SeatDAO seatDAO = new SeatDAOImpl();
            String seatConfig = aircraft.getSeatConfiguration();
            int availableSeats = flight.getAvailableSeats();
            seatDAO.createSeatsForFlight(generatedFlightId, totalSeats, seatConfig, availableSeats);
            
            // Commit transaction if everything succeeded
            TransactionManager.commit(conn);
        
        } catch (SQLException e) {
            // Rollback transaction if anything fails
            TransactionManager.rollback(conn);
            throw e;
        }
    
        // OPTIONAL: store flightId into Flight object if you add a field later
        // flight.setFlightId(generatedFlightId);
    
        return flight;
    }
    

    @Override
    public Flight findById(Integer id) throws SQLException {
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
    public Flight findByFlightNumber(String flightNumber) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_FLIGHT_NUMBER_SQL)) {
            stmt.setString(1, flightNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }

    @Override
    public List<Flight> findByAircraftId(Integer aircraftId) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Flight> list = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_AIRCRAFT_ID_SQL)) {
            stmt.setInt(1, aircraftId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    @Override
    public List<Flight> findByAirlineId(Integer airlineId) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Flight> list = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_AIRLINE_ID_SQL)) {
            stmt.setInt(1, airlineId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    @Override
    public List<Flight> findByRouteId(Integer routeId) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Flight> list = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ROUTE_ID_SQL)) {
            stmt.setInt(1, routeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    @Override
    public List<Flight> findAll() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Flight> list = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }

        return list;
    }

    @Override
    public boolean update(Flight flight) throws SQLException {
        // Since Flight doesn't have flightId, we need to find it by flightNumber first
        Flight existing = findByFlightNumber(flight.getFlightNumber());
        if (existing == null) {
            return false;
        }

        // Get flight_id and existing airline_id from database
        Connection conn = DatabaseConnection.getInstance().getConnection();
        Integer flightId = getFlightIdByNumber(flight.getFlightNumber());
        if (flightId == null) {
            return false;
        }

        // Get existing airline_id to preserve it, or get from flight number if flight number changed
        Integer airlineId = getExistingAirlineId(flightId);
        if (airlineId == null) {
            // If flight number changed, try to get airline from new flight number
            airlineId = getAirlineIdFromFlightNumber(flight.getFlightNumber());
            if (airlineId == null) {
                return false;
            }
        }

        // Check if available seats changed
        int newAvailableSeats = flight.getAvailableSeats();
        int oldAvailableSeats = existing.getAvailableSeats();
        boolean availableSeatsChanged = (newAvailableSeats != oldAvailableSeats);
        
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, flight.getFlightNumber());
            // Convert LocalDateTime to Timestamp treating it as UTC (no timezone conversion)
            // This ensures the exact time entered is stored in the database
            Timestamp depTs = Timestamp.from(flight.getDepartureTime().atZone(ZoneId.of("UTC")).toInstant());
            Timestamp arrTs = Timestamp.from(flight.getArrivalTime().atZone(ZoneId.of("UTC")).toInstant());
            stmt.setTimestamp(2, depTs, UTC_CALENDAR);
            stmt.setTimestamp(3, arrTs, UTC_CALENDAR);
            stmt.setString(4, flight.getStatus().name());
            stmt.setInt(5, flight.getAvailableSeats());
            stmt.setDouble(6, flight.getPrice());
            stmt.setInt(7, flight.getAircraft().getAircraftId());
            stmt.setInt(8, flight.getRoute().getRouteId());
            stmt.setInt(9, airlineId);
            stmt.setInt(10, flightId);

            int affected = stmt.executeUpdate();
            
            // If available seats changed, update seat availability
            if (affected > 0 && availableSeatsChanged) {
                SeatDAO seatDAO = new SeatDAOImpl();
                seatDAO.updateSeatAvailability(flightId, newAvailableSeats);
            }
            
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

    private Integer getFlightIdByNumber(String flightNumber) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT flight_id FROM flights WHERE flight_number = ?")) {
            stmt.setString(1, flightNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("flight_id");
                }
            }
        }
        return null;
    }

    private Integer getAirlineIdFromFlightNumber(String flightNumber) throws SQLException {
        // Extract airline code from flight number (e.g., "AC" from "AC123")
        // Flight numbers typically start with 2-letter airline code
        if (flightNumber == null || flightNumber.length() < 2) {
            return null;
        }
        
        String airlineCode = flightNumber.substring(0, 2).toUpperCase();
        
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT airline_id FROM airlines WHERE code = ?")) {
            stmt.setString(1, airlineCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("airline_id");
                }
            }
        }
        return null;
    }

    private Integer getExistingAirlineId(Integer flightId) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT airline_id FROM flights WHERE flight_id = ?")) {
            stmt.setInt(1, flightId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("airline_id");
                }
            }
        }
        return null;
    }

    private Flight mapRow(ResultSet rs) throws SQLException {
        Flight flight = new Flight();

        flight.setFlightNumber(rs.getString("flight_number"));
        
        // Use UTC calendar when reading to match how we write (treat as UTC, no conversion)
        Timestamp depTs = rs.getTimestamp("departure_time", UTC_CALENDAR);
        if (depTs != null) {
            // Convert from UTC timestamp back to LocalDateTime (treating as UTC)
            flight.setDepartureTime(depTs.toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
        }
        
        Timestamp arrTs = rs.getTimestamp("arrival_time", UTC_CALENDAR);
        if (arrTs != null) {
            // Convert from UTC timestamp back to LocalDateTime (treating as UTC)
            flight.setArrivalTime(arrTs.toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
        }
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            flight.setStatus(FlightStatus.valueOf(statusStr));
        }
        
        flight.setAvailableSeats(rs.getInt("available_seats"));
        flight.setPrice(rs.getDouble("price"));

        // Load related entities
        int aircraftId = rs.getInt("aircraft_id");
        Aircraft aircraft = aircraftDAO.findById(aircraftId);
        flight.setAircraft(aircraft);

        int routeId = rs.getInt("route_id");
        Route route = routeDAO.findById(routeId);
        flight.setRoute(route);

        return flight;
    }
}


