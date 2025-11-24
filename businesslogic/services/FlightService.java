package businesslogic.services;

import businesslogic.entities.Flight;
import businesslogic.entities.enums.FlightStatus;
import datalayer.dao.FlightDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for flight-related business logic.
 * Handles flight search, viewing, and management operations.
 */
public class FlightService {
    
    private FlightDAO flightDAO;
    
    public FlightService(FlightDAO flightDAO) {
        this.flightDAO = flightDAO;
    }
    
    /**
     * Search flights by origin, destination, and date.
     * @param originCode Origin airport code (e.g., "YYC")
     * @param destinationCode Destination airport code (e.g., "YYZ")
     * @param date Departure date
     * @return List of matching flights
     */
    public List<Flight> searchFlights(String originCode, String destinationCode, 
                                     LocalDate date) throws SQLException {
        if (originCode == null || destinationCode == null || date == null) {
            throw new IllegalArgumentException("Origin, destination, and date are required");
        }
        
        List<Flight> allFlights = flightDAO.findAll();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        
        return allFlights.stream()
            .filter(flight -> {
                if (flight.getRoute() == null || flight.getRoute().getOrigin() == null || 
                    flight.getRoute().getDestination() == null) {
                    return false;
                }
                
                String flightOrigin = flight.getRoute().getOrigin().getAirportCode();
                String flightDest = flight.getRoute().getDestination().getAirportCode();
                LocalDateTime depTime = flight.getDepartureTime();
                
                return flightOrigin.equalsIgnoreCase(originCode) &&
                       flightDest.equalsIgnoreCase(destinationCode) &&
                       depTime != null &&
                       depTime.isAfter(startOfDay) &&
                       depTime.isBefore(endOfDay) &&
                       flight.getStatus() == FlightStatus.SCHEDULED &&
                       flight.getAvailableSeats() > 0;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Search flights by origin, destination, date, and airline.
     * @param originCode Origin airport code
     * @param destinationCode Destination airport code
     * @param date Departure date
     * @param airlineCode Airline code (e.g., "AC")
     * @return List of matching flights
     */
    public List<Flight> searchFlights(String originCode, String destinationCode, 
                                     LocalDate date, String airlineCode) throws SQLException {
        List<Flight> flights = searchFlights(originCode, destinationCode, date);
        
        if (airlineCode == null || airlineCode.isEmpty()) {
            return flights;
        }
        
        return flights.stream()
            .filter(flight -> {
                // Note: Flight entity needs airline reference - assuming it exists
                // For now, filter by airline code if available
                return true; // Placeholder - implement when Flight has airline reference
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Get flight details by flight number.
     * @param flightNumber Flight number (e.g., "AC123")
     * @return Flight object or null if not found
     */
    public Flight getFlightByNumber(String flightNumber) throws SQLException {
        if (flightNumber == null || flightNumber.isEmpty()) {
            throw new IllegalArgumentException("Flight number is required");
        }
        
        return flightDAO.findByFlightNumber(flightNumber);
    }
    
    /**
     * Get all available flights (scheduled with available seats).
     * @return List of available flights
     */
    public List<Flight> getAvailableFlights() throws SQLException {
        List<Flight> allFlights = flightDAO.findAll();
        return allFlights.stream()
            .filter(f -> f.getStatus() == FlightStatus.SCHEDULED && f.getAvailableSeats() > 0)
            .collect(Collectors.toList());
    }
    
    /**
     * Get flights by status.
     * @param status Flight status
     * @return List of flights with the specified status
     */
    public List<Flight> getFlightsByStatus(FlightStatus status) throws SQLException {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        
        List<Flight> allFlights = flightDAO.findAll();
        return allFlights.stream()
            .filter(f -> f.getStatus() == status)
            .collect(Collectors.toList());
    }
    
    /**
     * Update flight status.
     * @param flightNumber Flight number
     * @param newStatus New flight status
     * @return true if update successful
     */
    public boolean updateFlightStatus(String flightNumber, FlightStatus newStatus) throws SQLException {
        Flight flight = getFlightByNumber(flightNumber);
        if (flight == null) {
            return false;
        }
        
        flight.setStatus(newStatus);
        return flightDAO.update(flight);
    }
    
    /**
     * Decrease available seats when a booking is made.
     * @param flightNumber Flight number
     * @param seatsBooked Number of seats being booked
     * @return true if seats were available and updated
     */
    public boolean bookSeats(String flightNumber, int seatsBooked) throws SQLException {
        if (seatsBooked <= 0) {
            throw new IllegalArgumentException("Number of seats must be positive");
        }
        
        Flight flight = getFlightByNumber(flightNumber);
        if (flight == null) {
            return false;
        }
        
        if (flight.getAvailableSeats() < seatsBooked) {
            return false; // Not enough seats available
        }
        
        flight.setAvailableSeats(flight.getAvailableSeats() - seatsBooked);
        return flightDAO.update(flight);
    }
    
    /**
     * Release seats when a reservation is cancelled.
     * @param flightNumber Flight number
     * @param seatsReleased Number of seats being released
     * @return true if update successful
     */
    public boolean releaseSeats(String flightNumber, int seatsReleased) throws SQLException {
        if (seatsReleased <= 0) {
            throw new IllegalArgumentException("Number of seats must be positive");
        }
        
        Flight flight = getFlightByNumber(flightNumber);
        if (flight == null) {
            return false;
        }
        
        flight.setAvailableSeats(flight.getAvailableSeats() + seatsReleased);
        return flightDAO.update(flight);
    }
}
