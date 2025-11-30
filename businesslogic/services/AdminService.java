package businesslogic.services;

import businesslogic.entities.Aircraft;
import businesslogic.entities.Airline;
import businesslogic.entities.Airport;
import businesslogic.entities.Customer;
import businesslogic.entities.Flight;
import businesslogic.entities.FlightAgent;
import businesslogic.entities.Payment;
import businesslogic.entities.Reservation;
import businesslogic.entities.Route;
import businesslogic.entities.Seat;
import businesslogic.entities.SystemAdmin;
import businesslogic.entities.User;
import businesslogic.entities.enums.FlightStatus;
import datalayer.dao.AircraftDAO;
import datalayer.dao.AirlineDAO;
import datalayer.dao.AirportDAO;
import datalayer.dao.FlightDAO;
import datalayer.dao.PaymentDAO;
import datalayer.dao.ReservationDAO;
import datalayer.dao.RouteDAO;
import datalayer.dao.SeatDAO;
import datalayer.dao.UserDAO;
import datalayer.database.DatabaseConnection;
import datalayer.database.TransactionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles all admin operations: CRUD for flights, aircraft, airlines, airports, routes.
 * Implements cascading deletes to maintain referential integrity.
 */
public class AdminService {

    private final FlightDAO flightDAO;
    private final AircraftDAO aircraftDAO;
    private final AirlineDAO airlineDAO;
    private final AirportDAO airportDAO;
    private final RouteDAO routeDAO;
    private final UserDAO userDAO;
    private final SeatDAO seatDAO;
    private final ReservationDAO reservationDAO;
    private final PaymentDAO paymentDAO;

    public AdminService(FlightDAO flightDAO,
                        AircraftDAO aircraftDAO,
                        AirlineDAO airlineDAO,
                        AirportDAO airportDAO,
                        RouteDAO routeDAO,
                        UserDAO userDAO,
                        SeatDAO seatDAO,
                        ReservationDAO reservationDAO) {
        this(flightDAO, aircraftDAO, airlineDAO, airportDAO, routeDAO, userDAO, seatDAO, reservationDAO, null);
    }

    public AdminService(FlightDAO flightDAO,
                        AircraftDAO aircraftDAO,
                        AirlineDAO airlineDAO,
                        AirportDAO airportDAO,
                        RouteDAO routeDAO,
                        UserDAO userDAO,
                        SeatDAO seatDAO,
                        ReservationDAO reservationDAO,
                        PaymentDAO paymentDAO) {
        this.flightDAO = flightDAO;
        this.aircraftDAO = aircraftDAO;
        this.airlineDAO = airlineDAO;
        this.airportDAO = airportDAO;
        this.routeDAO = routeDAO;
        this.userDAO = userDAO;
        this.seatDAO = seatDAO;
        this.reservationDAO = reservationDAO;
        this.paymentDAO = paymentDAO;
    }

    public Flight addFlight(Flight flight) throws SQLException {
        validateFlight(flight);

        // Validate flight number is unique
        Flight existing = getFlightByNumber(flight.getFlightNumber());
        if (existing != null) {
            throw new IllegalStateException("Flight number already exists");
        }

        return flightDAO.save(flight);
    }

    public boolean updateFlight(Flight flight) throws SQLException {
        validateFlight(flight);
        return flightDAO.update(flight);
    }

    public boolean removeFlight(String flightNumber) throws SQLException {
        Flight flight = getFlightByNumber(flightNumber);
        if (flight == null) {
            return false;
        }

        flight.setStatus(FlightStatus.CANCELLED);
        return flightDAO.update(flight);
    }

    /**
     * Permanently delete flight with cascading deletes: tickets -> reservations -> seats -> flight
     */
    public boolean deleteFlight(String flightNumber) throws SQLException {
        Flight flight = getFlightByNumber(flightNumber);
        if (flight == null) {
            return false;
        }

        Connection conn = DatabaseConnection.getInstance().getConnection();
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

        TransactionManager.begin(conn);
        try {
            // Delete tickets for reservations on this flight
            List<Reservation> reservations = reservationDAO.findByFlightId(flightId);
            for (Reservation reservation : reservations) {
                try (PreparedStatement deleteTicketsStmt = conn.prepareStatement(
                        "DELETE FROM tickets WHERE reservation_id = ?")) {
                    deleteTicketsStmt.setInt(1, reservation.getReservationId());
                    deleteTicketsStmt.executeUpdate();
                }
            }

            // Delete reservations
            for (Reservation reservation : reservations) {
                reservationDAO.delete(reservation.getReservationId());
            }

            // Delete seats
            List<Seat> seats = seatDAO.findByFlightId(flightId);
            for (Seat seat : seats) {
                seatDAO.delete(seat.getSeatId());
            }

            // Delete flight
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

    public Flight getFlightByNumber(String flightNumber) throws SQLException {
        return flightDAO.findByFlightNumber(flightNumber);
    }

    public List<Flight> getAllFlights() throws SQLException {
        return flightDAO.findAll();
    }

    private void validateFlight(Flight flight) {
        if (flight == null) {
            throw new IllegalArgumentException("Flight is required.");
        }
        if (flight.getFlightNumber() == null || flight.getFlightNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Flight number is required.");
        }
        if (flight.getDepartureTime() == null) {
            throw new IllegalArgumentException("Departure time is required.");
        }
        if (flight.getArrivalTime() == null) {
            throw new IllegalArgumentException("Arrival time is required.");
        }
        if (flight.getAircraft() == null) {
            throw new IllegalArgumentException("Aircraft is required.");
        }
        if (flight.getRoute() == null) {
            throw new IllegalArgumentException("Route is required.");
        }
        if (flight.getPrice() < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
    }

    public Aircraft addAircraft(Aircraft aircraft) throws SQLException {
        validateAircraft(aircraft);
        return aircraftDAO.save(aircraft);
    }

    /**
     * Update aircraft and auto-update related flights:
     * ACTIVE -> restore flights to SCHEDULED
     * MAINTENANCE -> flights become DELAYED
     * INACTIVE -> flights become CANCELLED
     */
    public boolean updateAircraft(Aircraft aircraft) throws SQLException {
        if (aircraft == null || aircraft.getAircraftId() <= 0) {
            throw new IllegalArgumentException("Valid aircraft is required");
        }

        validateAircraft(aircraft);

        Aircraft oldAircraft = aircraftDAO.findById(aircraft.getAircraftId());
        if (oldAircraft == null) {
            return false;
        }

        String oldStatus = oldAircraft.getStatus();
        String newStatus = aircraft.getStatus();

        boolean success = aircraftDAO.update(aircraft);
        if (!success) {
            return false;
        }

        // Only if status actually changed
        if (newStatus != null && !newStatus.equals(oldStatus)) {
            updateRelatedFlights(aircraft.getAircraftId(), oldStatus, newStatus);
        }

        return true;
    }

    private void updateRelatedFlights(int aircraftId, String oldStatus, String newStatus) throws SQLException {
        List<Flight> flights = flightDAO.findByAircraftId(aircraftId);
        if (flights.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<Flight> upcomingFlights = flights.stream()
                .filter(f -> f.getDepartureTime() != null && f.getDepartureTime().isAfter(now))
                .collect(Collectors.toList());

        if (upcomingFlights.isEmpty()) {
            return;
        }

        List<Flight> flightsToUpdate = new ArrayList<>();
        FlightStatus targetStatus = null;

        if ("ACTIVE".equals(newStatus)) {
            flightsToUpdate = upcomingFlights.stream()
                    .filter(f -> f.getStatus() == FlightStatus.DELAYED ||
                                 f.getStatus() == FlightStatus.CANCELLED)
                    .collect(Collectors.toList());
            targetStatus = FlightStatus.SCHEDULED;
        } else if ("MAINTENANCE".equals(newStatus)) {
            flightsToUpdate = upcomingFlights.stream()
                    .filter(f -> f.getStatus() == FlightStatus.SCHEDULED ||
                                 f.getStatus() == FlightStatus.CANCELLED)
                    .collect(Collectors.toList());
            targetStatus = FlightStatus.DELAYED;
        } else if ("INACTIVE".equals(newStatus)) {
            flightsToUpdate = upcomingFlights.stream()
                    .filter(f -> f.getStatus() == FlightStatus.SCHEDULED ||
                                 f.getStatus() == FlightStatus.DELAYED)
                    .collect(Collectors.toList());
            targetStatus = FlightStatus.CANCELLED;
        }

        if (flightsToUpdate.isEmpty() || targetStatus == null) {
            return;
        }

        Connection conn = DatabaseConnection.getInstance().getConnection();
        TransactionManager.begin(conn);
        try {
            for (Flight flight : flightsToUpdate) {
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
     * Delete aircraft with cascading deletes: flights -> tickets -> reservations -> seats -> aircraft
     */
    public boolean removeAircraft(int aircraftId) throws SQLException {
        List<Flight> flights = flightDAO.findByAircraftId(aircraftId);

        if (flights.isEmpty()) {
            return aircraftDAO.delete(aircraftId);
        }

        Connection conn = DatabaseConnection.getInstance().getConnection();
        TransactionManager.begin(conn);

        try {
            for (Flight flight : flights) {
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
                    List<Reservation> reservations = reservationDAO.findByFlightId(flightId);
                    for (Reservation reservation : reservations) {
                        try (PreparedStatement deleteTicketsStmt = conn.prepareStatement(
                                "DELETE FROM tickets WHERE reservation_id = ?")) {
                            deleteTicketsStmt.setInt(1, reservation.getReservationId());
                            deleteTicketsStmt.executeUpdate();
                        }
                    }

                    for (Reservation reservation : reservations) {
                        reservationDAO.delete(reservation.getReservationId());
                    }

                    List<Seat> seats = seatDAO.findByFlightId(flightId);
                    for (Seat seat : seats) {
                        seatDAO.delete(seat.getSeatId());
                    }

                    flightDAO.delete(flightId);
                }
            }

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

    public List<Aircraft> getAllAircraft() throws SQLException {
        return aircraftDAO.findAll();
    }

    private void validateAircraft(Aircraft aircraft) {
        if (aircraft == null) {
            throw new IllegalArgumentException("Aircraft is required.");
        }
        if (aircraft.getModel() == null || aircraft.getModel().trim().isEmpty()) {
            throw new IllegalArgumentException("Aircraft model is required.");
        }
        if (aircraft.getManufacturer() == null || aircraft.getManufacturer().trim().isEmpty()) {
            throw new IllegalArgumentException("Aircraft manufacturer is required.");
        }
        if (aircraft.getTotalSeats() <= 0) {
            throw new IllegalArgumentException("Total seats must be greater than 0.");
        }
    }

    public Airline addAirline(Airline airline) throws SQLException {
        if (airline == null) {
            throw new IllegalArgumentException("Airline is required");
        }
        return airlineDAO.save(airline);
    }

    public boolean updateAirline(Airline airline) throws SQLException {
        if (airline == null || airline.getAirlineId() <= 0) {
            throw new IllegalArgumentException("Valid airline is required");
        }
        return airlineDAO.update(airline);
    }

    /**
     * Delete airline with cascading deletes: flights -> tickets -> reservations -> seats -> airline
     */
    public boolean removeAirline(int airlineId) throws SQLException {
        List<Flight> flights = flightDAO.findByAirlineId(airlineId);

        if (flights.isEmpty()) {
            return airlineDAO.delete(airlineId);
        }

        Connection conn = DatabaseConnection.getInstance().getConnection();
        TransactionManager.begin(conn);

        try {
            for (Flight flight : flights) {
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
                    List<Reservation> reservations = reservationDAO.findByFlightId(flightId);
                    for (Reservation reservation : reservations) {
                        try (PreparedStatement deleteTicketsStmt = conn.prepareStatement(
                                "DELETE FROM tickets WHERE reservation_id = ?")) {
                            deleteTicketsStmt.setInt(1, reservation.getReservationId());
                            deleteTicketsStmt.executeUpdate();
                        }
                    }

                    for (Reservation reservation : reservations) {
                        reservationDAO.delete(reservation.getReservationId());
                    }

                    List<Seat> seats = seatDAO.findByFlightId(flightId);
                    for (Seat seat : seats) {
                        seatDAO.delete(seat.getSeatId());
                    }

                    flightDAO.delete(flightId);
                }
            }

            boolean success = airlineDAO.delete(airlineId);
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

    public List<Airline> getAllAirlines() throws SQLException {
        return airlineDAO.findAll();
    }

    public Airport addAirport(Airport airport) throws SQLException {
        if (airport == null) {
            throw new IllegalArgumentException("Airport is required");
        }
        if (airport.getAirportCode() == null || airport.getAirportCode().isEmpty()) {
            throw new IllegalArgumentException("Airport code is required");
        }
        return airportDAO.save(airport);
    }

    public boolean updateAirport(Airport airport) throws SQLException {
        if (airport == null || airport.getAirportCode() == null) {
            throw new IllegalArgumentException("Valid airport is required");
        }
        return airportDAO.update(airport);
    }

    /**
     * Delete airport with cascading deletes: routes -> flights -> tickets -> reservations -> seats -> airport
     */
    public boolean removeAirport(String airportCode) throws SQLException {
        List<Route> routes = routeDAO.findByAirportCode(airportCode);

        if (routes.isEmpty()) {
            return airportDAO.delete(airportCode);
        }

        Connection conn = DatabaseConnection.getInstance().getConnection();
        TransactionManager.begin(conn);

        try {
            for (Route route : routes) {
                List<Flight> flights = flightDAO.findByRouteId(route.getRouteId());

                for (Flight flight : flights) {
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
                        List<Reservation> reservations = reservationDAO.findByFlightId(flightId);
                        for (Reservation reservation : reservations) {
                            try (PreparedStatement deleteTicketsStmt = conn.prepareStatement(
                                    "DELETE FROM tickets WHERE reservation_id = ?")) {
                                deleteTicketsStmt.setInt(1, reservation.getReservationId());
                                deleteTicketsStmt.executeUpdate();
                            }
                        }

                        for (Reservation reservation : reservations) {
                            reservationDAO.delete(reservation.getReservationId());
                        }

                        List<Seat> seats = seatDAO.findByFlightId(flightId);
                        for (Seat seat : seats) {
                            seatDAO.delete(seat.getSeatId());
                        }

                        flightDAO.delete(flightId);
                    }
                }

                routeDAO.delete(route.getRouteId());
            }

            boolean success = airportDAO.delete(airportCode);
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

    public List<Airport> getAllAirports() throws SQLException {
        return airportDAO.findAll();
    }

    public Route addRoute(Route route) throws SQLException {
        if (route == null) {
            throw new IllegalArgumentException("Route is required");
        }
        if (route.getOrigin() == null || route.getDestination() == null) {
            throw new IllegalArgumentException("Route origin and destination are required");
        }
        if (route.getOrigin().getAirportCode().equals(route.getDestination().getAirportCode())) {
            throw new IllegalArgumentException("Origin and destination airports must be different");
        }

        // Prevent duplicate routes
        List<Route> allRoutes = routeDAO.findAll();
        for (Route existingRoute : allRoutes) {
            if (existingRoute.getOrigin() != null && existingRoute.getDestination() != null &&
                existingRoute.getOrigin().getAirportCode().equals(route.getOrigin().getAirportCode()) &&
                existingRoute.getDestination().getAirportCode().equals(route.getDestination().getAirportCode())) {
                throw new IllegalStateException("Route already exists: " +
                        route.getOrigin().getAirportCode() + " → " +
                        route.getDestination().getAirportCode());
            }
        }

        return routeDAO.save(route);
    }

    public boolean updateRoute(Route route) throws SQLException {
        if (route == null || route.getRouteId() <= 0) {
            throw new IllegalArgumentException("Valid route is required");
        }
        if (route.getOrigin() == null || route.getDestination() == null) {
            throw new IllegalArgumentException("Route origin and destination are required");
        }
        if (route.getOrigin().getAirportCode().equals(route.getDestination().getAirportCode())) {
            throw new IllegalArgumentException("Origin and destination airports must be different");
        }
        return routeDAO.update(route);
    }

    /**
     * Remove a route with cascading deletes of flights and their dependencies.
     */
    public boolean removeRoute(int routeId) throws SQLException {
        List<Flight> flights = flightDAO.findByRouteId(routeId);

        if (flights.isEmpty()) {
            return routeDAO.delete(routeId);
        }

        Connection conn = DatabaseConnection.getInstance().getConnection();
        TransactionManager.begin(conn);

        try {
            for (Flight flight : flights) {
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
                    List<Reservation> reservations = reservationDAO.findByFlightId(flightId);
                    for (Reservation reservation : reservations) {
                        try (PreparedStatement deleteTicketsStmt = conn.prepareStatement(
                                "DELETE FROM tickets WHERE reservation_id = ?")) {
                            deleteTicketsStmt.setInt(1, reservation.getReservationId());
                            deleteTicketsStmt.executeUpdate();
                        }
                    }

                    for (Reservation reservation : reservations) {
                        reservationDAO.delete(reservation.getReservationId());
                    }

                    List<Seat> seats = seatDAO.findByFlightId(flightId);
                    for (Seat seat : seats) {
                        seatDAO.delete(seat.getSeatId());
                    }

                    flightDAO.delete(flightId);
                }
            }

            boolean success = routeDAO.delete(routeId);
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

    public List<Route> getAllRoutes() throws SQLException {
        return routeDAO.findAll();
    }

    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
    }

    public List<Customer> getAllCustomers() throws SQLException {
        return userDAO.findAllCustomers();
    }

    public List<FlightAgent> getAllFlightAgents() throws SQLException {
        return userDAO.findAllFlightAgents();
    }

    public List<SystemAdmin> getAllSystemAdmins() throws SQLException {
        return userDAO.findAllSystemAdmins();
    }

    /**
     * Alias from second file: same as getAllSystemAdmins().
     */
    public List<SystemAdmin> getAllAdmins() throws SQLException {
        return getAllSystemAdmins();
    }

    public boolean deleteUser(int userId) throws SQLException {
        return userDAO.delete(userId);
    }

    /**
     * Verify if a user is a system admin.
     */
    public SystemAdmin getAdminById(int userId) throws SQLException {
        User user = userDAO.findById(userId);
        if (user instanceof SystemAdmin) {
            return (SystemAdmin) user;
        }
        return null;
    }

    public List<Reservation> getAllReservations() throws SQLException {
        return reservationDAO.findAll();
    }

    public List<Reservation> getReservationsByCustomer(int customerId) throws SQLException {
        return reservationDAO.findByCustomerId(customerId);
    }

    public List<Reservation> getReservationsByFlight(int flightId) throws SQLException {
        return reservationDAO.findByFlightId(flightId);
    }

    public List<Payment> getAllPayments() throws SQLException {
        if (paymentDAO == null) {
            throw new IllegalStateException("PaymentDAO is not configured for this AdminService");
        }
        return paymentDAO.findAll();
    }


    /**
     * Helper to get flight_id by flight number.
     * (Not heavily used here, but preserved from original version.)
     */
    private Integer getFlightIdByNumber(String flightNumber) throws SQLException {
        if (flightNumber == null || flightNumber.isEmpty()) {
            return null;
        }

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
}
