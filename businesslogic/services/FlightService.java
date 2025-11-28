package businesslogic.services;

import businesslogic.entities.Flight;
import businesslogic.entities.Airport;
import businesslogic.entities.enums.FlightStatus;
import datalayer.dao.FlightDAO;
import datalayer.dao.AirportDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FlightService - Handles flight search and management business logic.
 *
 * Location: businesslogic/services/FlightService.java
 */
public class FlightService {

    private final FlightDAO flightDAO;
    private final AirportDAO airportDAO;

    public FlightService(FlightDAO flightDAO, AirportDAO airportDAO) {
        this.flightDAO = flightDAO;
        this.airportDAO = airportDAO;
    }

    // ============================================================
    // Search / Query
    // ============================================================

    /**
     * Search for flights by origin, destination, and date.
     *
     * @param originCode       Origin airport code (e.g., "YYC")
     * @param destinationCode  Destination airport code (e.g., "YYZ")
     * @param date             Departure date
     * @return List of matching flights
     */
    public List<Flight> searchFlights(String originCode,
                                      String destinationCode,
                                      LocalDate date) throws SQLException {

        // Validate inputs
        if (originCode == null || originCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Origin airport code is required.");
        }
        if (destinationCode == null || destinationCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Destination airport code is required.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Departure date is required.");
        }

        String originCodeUpper = originCode.toUpperCase();
        String destinationCodeUpper = destinationCode.toUpperCase();

        // Validate airports exist
        Airport origin = airportDAO.findById(originCodeUpper);
        if (origin == null) {
            throw new IllegalArgumentException("Origin airport not found: " + originCode);
        }

        Airport destination = airportDAO.findById(destinationCodeUpper);
        if (destination == null) {
            throw new IllegalArgumentException("Destination airport not found: " + destinationCode);
        }

        List<Flight> allFlights = flightDAO.findAll();

        return allFlights.stream()
                .filter(f -> {
                    // Route and airport match
                    if (f.getRoute() == null ||
                        f.getRoute().getOrigin() == null ||
                        f.getRoute().getDestination() == null) {
                        return false;
                    }

                    boolean originMatch = f.getRoute().getOrigin().getAirportCode()
                            .equalsIgnoreCase(originCodeUpper);
                    boolean destMatch = f.getRoute().getDestination().getAirportCode()
                            .equalsIgnoreCase(destinationCodeUpper);

                    return originMatch && destMatch;
                })
                .filter(f -> {
                    // Date matches
                    if (f.getDepartureTime() == null) return false;
                    return f.getDepartureTime().toLocalDate().equals(date);
                })
                .filter(f -> {
                    // Only show scheduled or delayed flights
                    if (f.getStatus() == null) return true;
                    return f.getStatus() == FlightStatus.SCHEDULED ||
                           f.getStatus() == FlightStatus.DELAYED;
                })
                .filter(f -> f.getAvailableSeats() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Search flights by origin, destination, date, and airline.
     * Currently delegates to base search and leaves airline filtering as a placeholder
     * until Flight has a direct airline reference.
     *
     * @param originCode       Origin airport code
     * @param destinationCode  Destination airport code
     * @param date             Departure date
     * @param airlineCode      Airline code (e.g., "AC")
     * @return List of matching flights
     */
    public List<Flight> searchFlights(String originCode,
                                      String destinationCode,
                                      LocalDate date,
                                      String airlineCode) throws SQLException {

        List<Flight> flights = searchFlights(originCode, destinationCode, date);

        if (airlineCode == null || airlineCode.isEmpty()) {
            return flights;
        }

        // Placeholder: implement actual airline-based filtering when Flight has airline reference
        return flights.stream()
                .filter(f -> {
                    // e.g., f.getAirline().getCode().equalsIgnoreCase(airlineCode)
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all available flights (scheduled or delayed, with seats, and in the future).
     *
     * @return List of available flights
     */
    public List<Flight> getAvailableFlights() throws SQLException {
        List<Flight> allFlights = flightDAO.findAll();

        return allFlights.stream()
                .filter(f -> {
                    if (f.getStatus() == null) return true;
                    return f.getStatus() == FlightStatus.SCHEDULED ||
                           f.getStatus() == FlightStatus.DELAYED;
                })
                .filter(f -> f.getAvailableSeats() > 0)
                .filter(f -> {
                    // Only future flights if departure time known
                    if (f.getDepartureTime() == null) return true;
                    return f.getDepartureTime().isAfter(LocalDateTime.now());
                })
                .collect(Collectors.toList());
    }

    /**
     * Get flights by status.
     *
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
     * Get flights by route (origin and destination, any date).
     */
    public List<Flight> getFlightsByRoute(String originCode, String destinationCode) throws SQLException {
        List<Flight> allFlights = flightDAO.findAll();

        return allFlights.stream()
                .filter(f -> {
                    if (f.getRoute() == null ||
                        f.getRoute().getOrigin() == null ||
                        f.getRoute().getDestination() == null) {
                        return false;
                    }

                    boolean originMatch = f.getRoute().getOrigin().getAirportCode()
                            .equalsIgnoreCase(originCode);
                    boolean destMatch = f.getRoute().getDestination().getAirportCode()
                            .equalsIgnoreCase(destinationCode);

                    return originMatch && destMatch;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get flights departing on a specific date (any route).
     */
    public List<Flight> getFlightsByDate(LocalDate date) throws SQLException {
        if (date == null) {
            return new ArrayList<>();
        }

        List<Flight> allFlights = flightDAO.findAll();

        return allFlights.stream()
                .filter(f -> f.getDepartureTime() != null &&
                             f.getDepartureTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    // ============================================================
    // Single flight / seats
    // ============================================================

    /**
     * Get flight by flight number.
     *
     * @param flightNumber Flight number (e.g., "AC123")
     * @return Flight or null if not found or invalid input
     */
    public Flight getFlightByNumber(String flightNumber) throws SQLException {
        if (flightNumber == null || flightNumber.trim().isEmpty()) {
            return null;
        }
        return flightDAO.findByFlightNumber(flightNumber);
    }

    /**
     * Update a flight.
     *
     * @param flight Flight to update
     * @return true if update was successful
     */
    public boolean updateFlight(Flight flight) throws SQLException {
        if (flight == null) {
            throw new IllegalArgumentException("Flight is required.");
        }
        return flightDAO.update(flight);
    }

    /**
     * Update flight status by flight number.
     *
     * @param flightNumber Flight number
     * @param newStatus    New flight status
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
     *
     * @param flightNumber Flight number
     * @param seatsBooked  Number of seats being booked
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
     *
     * @param flightNumber  Flight number
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

    /**
     * Check if a flight has available seats.
     */
    public boolean hasAvailableSeats(String flightNumber) throws SQLException {
        Flight flight = getFlightByNumber(flightNumber);
        return flight != null && flight.getAvailableSeats() > 0;
    }

    /**
     * Get number of available seats on a flight.
     *
     * @param flightNumber Flight number
     * @return Number of available seats, or 0 if flight not found
     */
    public int getAvailableSeatCount(String flightNumber) throws SQLException {
        Flight flight = getFlightByNumber(flightNumber);
        return (flight != null) ? flight.getAvailableSeats() : 0;
    }

    // ============================================================
    // Airports
    // ============================================================

    /**
     * Get all flights.
     */
    public List<Flight> getAllFlights() throws SQLException {
        return flightDAO.findAll();
    }

    /**
     * Get all airports (e.g., for dropdowns in search forms).
     */
    public List<Airport> getAllAirports() throws SQLException {
        return airportDAO.findAll();
    }

    /**
     * Get airport by code.
     *
     * @param code Airport code (e.g., "YYC")
     * @return Airport or null if not found
     */
    public Airport getAirportByCode(String code) throws SQLException {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        return airportDAO.findById(code.toUpperCase());
    }
}
