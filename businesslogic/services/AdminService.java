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

import java.sql.SQLException;
import java.util.List;

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
    
    public AdminService(FlightDAO flightDAO, AircraftDAO aircraftDAO,
                       AirlineDAO airlineDAO, AirportDAO airportDAO,
                       RouteDAO routeDAO, UserDAO userDAO) {
        this.flightDAO = flightDAO;
        this.aircraftDAO = aircraftDAO;
        this.airlineDAO = airlineDAO;
        this.airportDAO = airportDAO;
        this.routeDAO = routeDAO;
        this.userDAO = userDAO;
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
     * @param aircraft Aircraft to update
     * @return true if update successful
     */
    public boolean updateAircraft(Aircraft aircraft) throws SQLException {
        if (aircraft == null || aircraft.getAircraftId() <= 0) {
            throw new IllegalArgumentException("Valid aircraft is required");
        }
        
        return aircraftDAO.update(aircraft);
    }
    
    /**
     * Remove an aircraft.
     * @param aircraftId Aircraft ID
     * @return true if removal successful
     */
    public boolean removeAircraft(int aircraftId) throws SQLException {
        return aircraftDAO.delete(aircraftId);
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
