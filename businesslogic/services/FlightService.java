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
 * Handles flight search and seat availability management.
 */
public class FlightService {
    
    private final FlightDAO flightDAO;
    private final AirportDAO airportDAO;

    public FlightService(FlightDAO flightDAO, AirportDAO airportDAO) {
        this.flightDAO = flightDAO;
        this.airportDAO = airportDAO;
    }

    /**
     * Search flights by route and date. Only returns scheduled/delayed flights with available seats.
     */
    public List<Flight> searchFlights(String originCode,
                                      String destinationCode,
                                      LocalDate date) throws SQLException {

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
                    if (f.getDepartureTime() == null) return false;
                    return f.getDepartureTime().toLocalDate().equals(date);
                })
                .filter(f -> {
                    if (f.getStatus() == null) return true;
                    return f.getStatus() == FlightStatus.SCHEDULED ||
                           f.getStatus() == FlightStatus.DELAYED;
                })
                .filter(f -> f.getAvailableSeats() > 0)
                .collect(Collectors.toList());
    }

    public List<Flight> searchFlights(String originCode,
                                      String destinationCode,
                                      LocalDate date,
                                      String airlineCode) throws SQLException {

        List<Flight> flights = searchFlights(originCode, destinationCode, date);

        if (airlineCode == null || airlineCode.isEmpty()) {
            return flights;
        }

        return flights.stream()
                .filter(f -> true)
                .collect(Collectors.toList());
    }

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
                    if (f.getDepartureTime() == null) return true;
                    return f.getDepartureTime().isAfter(LocalDateTime.now());
                })
                .collect(Collectors.toList());
    }

    public List<Flight> getFlightsByStatus(FlightStatus status) throws SQLException {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }

        List<Flight> allFlights = flightDAO.findAll();
        return allFlights.stream()
                .filter(f -> f.getStatus() == status)
                .collect(Collectors.toList());
    }

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

    public Flight getFlightByNumber(String flightNumber) throws SQLException {
        if (flightNumber == null || flightNumber.trim().isEmpty()) {
            return null;
        }
        return flightDAO.findByFlightNumber(flightNumber);
    }

    public boolean updateFlight(Flight flight) throws SQLException {
        if (flight == null) {
            throw new IllegalArgumentException("Flight is required.");
        }
        return flightDAO.update(flight);
    }

    public boolean updateFlightStatus(String flightNumber, FlightStatus newStatus) throws SQLException {
        Flight flight = getFlightByNumber(flightNumber);
        if (flight == null) {
            return false;
        }

        flight.setStatus(newStatus);
        return flightDAO.update(flight);
    }

    public boolean bookSeats(String flightNumber, int seatsBooked) throws SQLException {
        if (seatsBooked <= 0) {
            throw new IllegalArgumentException("Number of seats must be positive");
        }

        Flight flight = getFlightByNumber(flightNumber);
        if (flight == null) {
            return false;
        }

        if (flight.getAvailableSeats() < seatsBooked) {
            return false;
        }

        flight.setAvailableSeats(flight.getAvailableSeats() - seatsBooked);
        return flightDAO.update(flight);
    }

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

    public boolean hasAvailableSeats(String flightNumber) throws SQLException {
        Flight flight = getFlightByNumber(flightNumber);
        return flight != null && flight.getAvailableSeats() > 0;
    }

    public int getAvailableSeatCount(String flightNumber) throws SQLException {
        Flight flight = getFlightByNumber(flightNumber);
        return (flight != null) ? flight.getAvailableSeats() : 0;
    }

    public List<Flight> getAllFlights() throws SQLException {
        return flightDAO.findAll();
    }

    public List<Airport> getAllAirports() throws SQLException {
        return airportDAO.findAll();
    }

    public Airport getAirportByCode(String code) throws SQLException {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        return airportDAO.findById(code.toUpperCase());
    }
}
