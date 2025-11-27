package businesslogic.services;

import businesslogic.entities.Flight;
import businesslogic.entities.Aircraft;
import businesslogic.entities.Airline;
import businesslogic.entities.Airport;
import businesslogic.entities.Route;
import businesslogic.entities.SystemAdmin;
import businesslogic.entities.User;
import businesslogic.entities.enums.FlightStatus;
import datalayer.dao.FlightDAO;
import datalayer.dao.AircraftDAO;
import datalayer.dao.AirlineDAO;
import datalayer.dao.AirportDAO;
import datalayer.dao.RouteDAO;
import datalayer.dao.UserDAO;
import datalayer.dao.SeatDAO;
import datalayer.dao.ReservationDAO;
import datalayer.database.TransactionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for system administrator operations.
 * Handles management of flights, routes, aircraft, and other system data.
 */
public class AdminService {
    
    private FlightDAO flightDAO;
    private AircraftDAO aircraftDAO;
    private AirlineDAO airlineDAO;
    private AirportDAO airportDAO;
    private RouteDAO routeDAO;
    private UserDAO userDAO;
    private SeatDAO seatDAO;
    private ReservationDAO reservationDAO;
    
    public AdminService(FlightDAO flightDAO, AircraftDAO aircraftDAO,
                       AirlineDAO airlineDAO, AirportDAO airportDAO,
                       RouteDAO routeDAO, UserDAO userDAO, SeatDAO seatDAO,
                       ReservationDAO reservationDAO) {
        this.flightDAO = flightDAO;
        this.aircraftDAO = aircraftDAO;
        this.airlineDAO = airlineDAO;
        this.airportDAO = airportDAO;
        this.routeDAO = routeDAO;
        this.userDAO = userDAO;
        this.seatDAO = seatDAO;
        this.reservationDAO = reservationDAO;
    }
    
    // ========== Flight Management ==========
    
    /**
     * Add a new flight.
     * @param flight Flight to add
     * @return Created flight
     */
    public Flight addFlight(Flight flight) throws SQLException {
        if (flight == null) {
            throw new IllegalArgumentException("Flight is required");
        }
        
        if (flight.getFlightNumber() == null || flight.getFlightNumber().isEmpty()) {
            throw new IllegalArgumentException("Flight number is required");
        }
        
        // Validate flight number is unique
        Flight existing = getFlightByNumber(flight.getFlightNumber());
        if (existing != null) {
            throw new IllegalStateException("Flight number already exists");
        }
        
        return flightDAO.save(flight);
    }
    
    /**
     * Update flight details.
     * @param flight Flight to update
     * @return true if update successful
     */
    public boolean updateFlight(Flight flight) throws SQLException {
        if (flight == null || flight.getFlightNumber() == null) {
            throw new IllegalArgumentException("Valid flight is required");
        }
        
        return flightDAO.update(flight);
    }
    
    /**
     * Remove a flight (set status to CANCELLED or delete).
     * @param flightNumber Flight number
     * @return true if removal successful
     */
    public boolean removeFlight(String flightNumber) throws SQLException {
        Flight flight = getFlightByNumber(flightNumber);
        if (flight == null) {
            return false;
        }
        
        // Option 1: Cancel the flight
        flight.setStatus(FlightStatus.CANCELLED);
        return flightDAO.update(flight);
        
        // Option 2: Delete the flight (uncomment if preferred)
        // return flightDAO.delete(flight.getFlightId());
    }
    
    /**
     * Delete a flight permanently from the database.
     * This method handles cascading deletes for all related entities:
     * 1. Deletes tickets (which reference reservations and seats)
     * 2. Deletes reservations (which reference flights)
     * 3. Deletes seats (which reference flights)
     * 4. Deletes the flight itself
     * 
     * @param flightNumber Flight number
     * @return true if deletion successful
     */
    public boolean deleteFlight(String flightNumber) throws SQLException {
        Flight flight = getFlightByNumber(flightNumber);
        if (flight == null) {
            return false;
        }
        
        // Get flight_id from database
        Connection conn = datalayer.database.DatabaseConnection.getInstance().getConnection();
        Integer flightId = null;
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT flight_id FROM flights WHERE flight_number = ?")) {
            stmt.setString(1, flightNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    flightId = rs.getInt("flight_id");
                }
            }
        }
        
        if (flightId == null) {
            return false;
        }
        
        // Use transaction to ensure atomicity
        TransactionManager.begin(conn);
        try {
            // Step 1: Delete all tickets for reservations of this flight
            // (Tickets reference both reservations and seats)
            java.util.List<businesslogic.entities.Reservation> reservations = reservationDAO.findByFlightId(flightId);
            for (businesslogic.entities.Reservation reservation : reservations) {
                // Delete tickets for this reservation
                try (PreparedStatement deleteTicketsStmt = conn.prepareStatement(
                        "DELETE FROM tickets WHERE reservation_id = ?")) {
                    deleteTicketsStmt.setInt(1, reservation.getReservationId());
                    deleteTicketsStmt.executeUpdate();
                }
            }
            
            // Step 2: Delete all reservations for this flight
            for (businesslogic.entities.Reservation reservation : reservations) {
                reservationDAO.delete(reservation.getReservationId());
            }
            
            // Step 3: Delete all seats for this flight
            java.util.List<businesslogic.entities.Seat> seats = seatDAO.findByFlightId(flightId);
            for (businesslogic.entities.Seat seat : seats) {
                seatDAO.delete(seat.getSeatId());
            }
            
            // Step 4: Delete the flight itself
            boolean success = flightDAO.delete(flightId);
            
            if (success) {
                TransactionManager.commit(conn);
            } else {
                TransactionManager.rollback(conn);
            }
            
            return success;
        } catch (SQLException e) {
            TransactionManager.rollback(conn);
            throw e;
        }
    }
    
    /**
     * Get flight by flight number.
     * @param flightNumber Flight number
     * @return Flight or null if not found
     */
    public Flight getFlightByNumber(String flightNumber) throws SQLException {
        return flightDAO.findByFlightNumber(flightNumber);
    }
    
    /**
     * Get all flights.
     * @return List of all flights
     */
    public List<Flight> getAllFlights() throws SQLException {
        return flightDAO.findAll();
    }
    
    // ========== Aircraft Management ==========
    
    /**
     * Add a new aircraft.
     * @param aircraft Aircraft to add
     * @return Created aircraft
     */
    public Aircraft addAircraft(Aircraft aircraft) throws SQLException {
        if (aircraft == null) {
            throw new IllegalArgumentException("Aircraft is required");
        }
        
        return aircraftDAO.save(aircraft);
    }
    
    /**
     * Update aircraft details.
     * If aircraft status is changed to MAINTENANCE or INACTIVE,
     * automatically updates all upcoming scheduled flights using this aircraft:
     * - MAINTENANCE → flights set to DELAYED
     * - INACTIVE → flights set to CANCELLED
     * 
     * @param aircraft Aircraft to update
     * @return true if update successful
     */
    public boolean updateAircraft(Aircraft aircraft) throws SQLException {
        if (aircraft == null || aircraft.getAircraftId() <= 0) {
            throw new IllegalArgumentException("Valid aircraft is required");
        }
        
        // Get old aircraft status before updating
        Aircraft oldAircraft = aircraftDAO.findById(aircraft.getAircraftId());
        if (oldAircraft == null) {
            return false;
        }
        
        String oldStatus = oldAircraft.getStatus();
        String newStatus = aircraft.getStatus();
        
        // Update the aircraft
        boolean success = aircraftDAO.update(aircraft);
        
        if (!success) {
            return false;
        }
        
        // Always update related flights when status changes
        if (newStatus != null && !newStatus.equals(oldStatus)) {
            updateRelatedFlights(aircraft.getAircraftId(), oldStatus, newStatus);
        }
        
        return true;
    }
    
    /**
     * Update all upcoming flights for an aircraft based on status change.
     * Handles all transitions:
     * - ACTIVE: Restore DELAYED/CANCELLED flights to SCHEDULED
     * - MAINTENANCE: Set SCHEDULED/CANCELLED flights to DELAYED
     * - INACTIVE: Set SCHEDULED/DELAYED flights to CANCELLED
     * 
     * @param aircraftId Aircraft ID
     * @param oldStatus Previous aircraft status
     * @param newStatus New aircraft status
     */
    private void updateRelatedFlights(int aircraftId, String oldStatus, String newStatus) throws SQLException {
        // Get all flights using this aircraft
        List<Flight> flights = flightDAO.findByAircraftId(aircraftId);
        
        if (flights.isEmpty()) {
            return;
        }
        
        // Filter for upcoming flights (departure time in the future)
        LocalDateTime now = LocalDateTime.now();
        List<Flight> upcomingFlights = flights.stream()
            .filter(flight -> flight.getDepartureTime() != null && 
                             flight.getDepartureTime().isAfter(now))
            .collect(Collectors.toList());
        
        if (upcomingFlights.isEmpty()) {
            return;
        }
        
        // Determine which flights to update and their new status based on transition
        List<Flight> flightsToUpdate = new java.util.ArrayList<>();
        FlightStatus targetStatus = null;
        
        if ("ACTIVE".equals(newStatus)) {
            // Restore flights that were affected by previous status
            // Restore DELAYED or CANCELLED flights back to SCHEDULED
            flightsToUpdate = upcomingFlights.stream()
                .filter(flight -> flight.getStatus() == FlightStatus.DELAYED || 
                                 flight.getStatus() == FlightStatus.CANCELLED)
                .collect(Collectors.toList());
            targetStatus = FlightStatus.SCHEDULED;
        } else if ("MAINTENANCE".equals(newStatus)) {
            // Set SCHEDULED and CANCELLED flights to DELAYED
            flightsToUpdate = upcomingFlights.stream()
                .filter(flight -> flight.getStatus() == FlightStatus.SCHEDULED || 
                                 flight.getStatus() == FlightStatus.CANCELLED)
                .collect(Collectors.toList());
            targetStatus = FlightStatus.DELAYED;
        } else if ("INACTIVE".equals(newStatus)) {
            // Set SCHEDULED and DELAYED flights to CANCELLED
            flightsToUpdate = upcomingFlights.stream()
                .filter(flight -> flight.getStatus() == FlightStatus.SCHEDULED || 
                                 flight.getStatus() == FlightStatus.DELAYED)
                .collect(Collectors.toList());
            targetStatus = FlightStatus.CANCELLED;
        }
        
        if (flightsToUpdate.isEmpty() || targetStatus == null) {
            return;
        }
        
        // Update each flight
        Connection conn = datalayer.database.DatabaseConnection.getInstance().getConnection();
        TransactionManager.begin(conn);
        
        try {
            for (Flight flight : flightsToUpdate) {
                // Get flight_id from database
                Integer flightId = null;
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT flight_id FROM flights WHERE flight_number = ?")) {
                    stmt.setString(1, flight.getFlightNumber());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            flightId = rs.getInt("flight_id");
                        }
                    }
                }
                
                if (flightId != null) {
                    // Update flight status
                    flight.setStatus(targetStatus);
                    flightDAO.update(flight);
                }
            }
            
            TransactionManager.commit(conn);
        } catch (SQLException e) {
            TransactionManager.rollback(conn);
            throw e;
        }
    }
    
    /**
     * Remove an aircraft.
     * This method handles cascading deletes for all related entities:
     * 1. Finds all flights using this aircraft
     * 2. For each flight, deletes tickets, reservations, seats, and the flight itself
     * 3. Deletes the aircraft
     * 
     * @param aircraftId Aircraft ID
     * @return true if removal successful
     */
    public boolean removeAircraft(int aircraftId) throws SQLException {
        // Get all flights using this aircraft
        List<Flight> flights = flightDAO.findByAircraftId(aircraftId);
        
        if (flights.isEmpty()) {
            // No flights use this aircraft, safe to delete directly
            return aircraftDAO.delete(aircraftId);
        }
        
        // Use transaction to ensure atomicity
        Connection conn = datalayer.database.DatabaseConnection.getInstance().getConnection();
        TransactionManager.begin(conn);
        
        try {
            // Delete all flights that use this aircraft (with all their dependencies)
            for (Flight flight : flights) {
                // Get flight_id from database
                Integer flightId = null;
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT flight_id FROM flights WHERE flight_number = ?")) {
                    stmt.setString(1, flight.getFlightNumber());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            flightId = rs.getInt("flight_id");
                        }
                    }
                }
                
                if (flightId != null) {
                    // Step 1: Delete all tickets for reservations of this flight
                    java.util.List<businesslogic.entities.Reservation> reservations = 
                        reservationDAO.findByFlightId(flightId);
                    for (businesslogic.entities.Reservation reservation : reservations) {
                        // Delete tickets for this reservation
                        try (PreparedStatement deleteTicketsStmt = conn.prepareStatement(
                                "DELETE FROM tickets WHERE reservation_id = ?")) {
                            deleteTicketsStmt.setInt(1, reservation.getReservationId());
                            deleteTicketsStmt.executeUpdate();
                        }
                    }
                    
                    // Step 2: Delete all reservations for this flight
                    for (businesslogic.entities.Reservation reservation : reservations) {
                        reservationDAO.delete(reservation.getReservationId());
                    }
                    
                    // Step 3: Delete all seats for this flight
                    java.util.List<businesslogic.entities.Seat> seats = seatDAO.findByFlightId(flightId);
                    for (businesslogic.entities.Seat seat : seats) {
                        seatDAO.delete(seat.getSeatId());
                    }
                    
                    // Step 4: Delete the flight itself
                    flightDAO.delete(flightId);
                }
            }
            
            // Step 5: Delete the aircraft itself
            boolean success = aircraftDAO.delete(aircraftId);
            
            if (success) {
                TransactionManager.commit(conn);
            } else {
                TransactionManager.rollback(conn);
            }
            
            return success;
        } catch (SQLException e) {
            TransactionManager.rollback(conn);
            throw e;
        }
    }
    
    /**
     * Get all aircraft.
     * @return List of all aircraft
     */
    public List<Aircraft> getAllAircraft() throws SQLException {
        return aircraftDAO.findAll();
    }
    
    // ========== Airline Management ==========
    
    /**
     * Add a new airline.
     * @param airline Airline to add
     * @return Created airline
     */
    public Airline addAirline(Airline airline) throws SQLException {
        if (airline == null) {
            throw new IllegalArgumentException("Airline is required");
        }
        
        return airlineDAO.save(airline);
    }
    
    /**
     * Update airline details.
     * @param airline Airline to update
     * @return true if update successful
     */
    public boolean updateAirline(Airline airline) throws SQLException {
        if (airline == null || airline.getAirlineId() <= 0) {
            throw new IllegalArgumentException("Valid airline is required");
        }
        
        return airlineDAO.update(airline);
    }
    
    /**
     * Remove an airline.
     * @param airlineId Airline ID
     * @return true if removal successful
     */
    public boolean removeAirline(int airlineId) throws SQLException {
        return airlineDAO.delete(airlineId);
    }
    
    /**
     * Get all airlines.
     * @return List of all airlines
     */
    public List<Airline> getAllAirlines() throws SQLException {
        return airlineDAO.findAll();
    }
    
    // ========== Airport Management ==========
    
    /**
     * Add a new airport.
     * @param airport Airport to add
     * @return Created airport
     */
    public Airport addAirport(Airport airport) throws SQLException {
        if (airport == null) {
            throw new IllegalArgumentException("Airport is required");
        }
        
        if (airport.getAirportCode() == null || airport.getAirportCode().isEmpty()) {
            throw new IllegalArgumentException("Airport code is required");
        }
        
        return airportDAO.save(airport);
    }
    
    /**
     * Update airport details.
     * @param airport Airport to update
     * @return true if update successful
     */
    public boolean updateAirport(Airport airport) throws SQLException {
        if (airport == null || airport.getAirportCode() == null) {
            throw new IllegalArgumentException("Valid airport is required");
        }
        
        return airportDAO.update(airport);
    }
    
    /**
     * Remove an airport.
     * @param airportCode Airport code
     * @return true if removal successful
     */
    public boolean removeAirport(String airportCode) throws SQLException {
        return airportDAO.delete(airportCode);
    }
    
    /**
     * Get all airports.
     * @return List of all airports
     */
    public List<Airport> getAllAirports() throws SQLException {
        return airportDAO.findAll();
    }
    
    // ========== Route Management ==========
    
    /**
     * Add a new route.
     * @param route Route to add
     * @return Created route
     */
    public Route addRoute(Route route) throws SQLException {
        if (route == null) {
            throw new IllegalArgumentException("Route is required");
        }
        
        if (route.getOrigin() == null || route.getDestination() == null) {
            throw new IllegalArgumentException("Route origin and destination are required");
        }
        
        return routeDAO.save(route);
    }
    
    /**
     * Update route details.
     * @param route Route to update
     * @return true if update successful
     */
    public boolean updateRoute(Route route) throws SQLException {
        if (route == null || route.getRouteId() <= 0) {
            throw new IllegalArgumentException("Valid route is required");
        }
        
        return routeDAO.update(route);
    }
    
    /**
     * Remove a route.
     * @param routeId Route ID
     * @return true if removal successful
     */
    public boolean removeRoute(int routeId) throws SQLException {
        return routeDAO.delete(routeId);
    }
    
    /**
     * Get all routes.
     * @return List of all routes
     */
    public List<Route> getAllRoutes() throws SQLException {
        return routeDAO.findAll();
    }
    
    // ========== Admin User Management ==========
    
    /**
     * Verify if a user is a system admin.
     * @param userId User ID
     * @return SystemAdmin if user is admin, null otherwise
     */
    public SystemAdmin getAdminById(int userId) throws SQLException {
        User user = userDAO.findById(userId);
        if (user instanceof SystemAdmin) {
            return (SystemAdmin) user;
        }
        return null;
    }
    
    /**
     * Get all system admins.
     * @return List of all system admins
     */
    public List<SystemAdmin> getAllAdmins() throws SQLException {
        return userDAO.findAllSystemAdmins();
    }
}
