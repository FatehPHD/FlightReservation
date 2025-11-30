package businesslogic.services;

import businesslogic.entities.Reservation;
import businesslogic.entities.Customer;
import businesslogic.entities.User;
import businesslogic.entities.Flight;
import businesslogic.entities.Seat;
import businesslogic.entities.Payment;
import businesslogic.entities.enums.ReservationStatus;
import businesslogic.entities.enums.MembershipStatus;
import datalayer.dao.ReservationDAO;
import datalayer.dao.SeatDAO;
import datalayer.database.DatabaseConnection;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles reservation creation, cancellation, and seat management.
 */
public class ReservationService {
    
    private final ReservationDAO reservationDAO;
    private final SeatDAO seatDAO;
    private final FlightService flightService;

    public ReservationService(ReservationDAO reservationDAO,
                              SeatDAO seatDAO,
                              FlightService flightService) {
        this.reservationDAO = reservationDAO;
        this.seatDAO = seatDAO;
        this.flightService = flightService;
    }

    private Reservation createReservationInternal(Customer customer,
                                                  Flight flight,
                                                  List<Seat> selectedSeats,
                                                  Payment payment) throws SQLException {
        if (customer == null || flight == null || selectedSeats == null || selectedSeats.isEmpty()) {
            throw new IllegalArgumentException("Customer, flight, and seats are required.");
        }

        for (Seat seat : selectedSeats) {
            Seat dbSeat = seatDAO.findById(seat.getSeatId());
            if (dbSeat == null) {
                throw new IllegalStateException("Seat " + seat.getSeatNumber() + " not found.");
            }
            if (!dbSeat.isAvailable()) {
                throw new IllegalStateException("Seat " + seat.getSeatNumber()
                        + " is no longer available. Please select different seats.");
            }
        }

        if (flight.getAvailableSeats() < selectedSeats.size()) {
            throw new IllegalStateException("Not enough seats available on flight.");
        }

        double totalPrice = calculateTotalPrice(flight, selectedSeats);
        ReservationStatus status =
                (payment != null) ? ReservationStatus.CONFIRMED : ReservationStatus.PENDING;

        Reservation reservation = new Reservation(
                0,
                LocalDateTime.now(),
                status,
                totalPrice,
                customer,
                flight,
                payment,
                new ArrayList<>(selectedSeats)
        );

        Reservation saved = reservationDAO.save(reservation);

        for (Seat seat : selectedSeats) {
            seat.setAvailable(false);
            seatDAO.update(seat);
        }

        try {
            flightService.bookSeats(flight.getFlightNumber(), selectedSeats.size());
        } catch (SQLException e) {
            System.err.println("Warning: Could not update flight available seats: " + e.getMessage());
        }

        return saved;
    }

    public Reservation createReservation(Customer customer,
                                         Flight flight,
                                         List<Seat> selectedSeats,
                                         Payment payment) throws SQLException {
        return createReservationInternal(customer, flight, selectedSeats, payment);
    }

    public Reservation createReservation(Customer customer,
                                         Flight flight,
                                         List<Seat> selectedSeats) throws SQLException {
        return createReservationInternal(customer, flight, selectedSeats, null);
    }

    /**
     * Create reservation for any user type. Converts non-Customer users to Customer with defaults.
     */
    public Reservation createReservationForUser(User user,
                                                Flight flight,
                                                List<Seat> selectedSeats,
                                                Payment payment) throws SQLException {
        if (user == null || flight == null || selectedSeats == null || selectedSeats.isEmpty()) {
            throw new IllegalArgumentException("User, flight, and seats are required.");
        }

        Customer customer;
        if (user instanceof Customer) {
            customer = (Customer) user;
        } else {
            customer = new Customer();
            customer.setUserId(user.getUserId());
            customer.setUsername(user.getUsername());
            customer.setPassword(user.getPassword());
            customer.setEmail(user.getEmail());
            customer.setRole(user.getRole());
            customer.setFirstName(user.getUsername());
            customer.setMembershipStatus(MembershipStatus.REGULAR);
        }

        return createReservationInternal(customer, flight, selectedSeats, payment);
    }

    public Reservation createReservationForUser(User user,
                                                Flight flight,
                                                List<Seat> selectedSeats) throws SQLException {
        return createReservationForUser(user, flight, selectedSeats, null);
    }

    public boolean confirmReservation(int reservationId, Payment payment) throws SQLException {
        Reservation reservation = reservationDAO.findById(reservationId);
        if (reservation == null) {
            return false;
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Only pending reservations can be confirmed.");
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setPayment(payment);

        return reservationDAO.update(reservation);
    }

    /**
     * Cancel reservation and release seats back to the flight.
     */
    public boolean cancelReservation(int reservationId) throws SQLException {
        Reservation reservation = reservationDAO.findById(reservationId);
        if (reservation == null) {
            return false;
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return true;
        }

        if (reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed reservation.");
        }

        List<Seat> seats = reservation.getSeats();
        if (seats != null) {
            for (Seat seat : seats) {
                seat.setAvailable(true);
                seatDAO.update(seat);
            }
        }

        if (reservation.getFlight() != null && seats != null) {
            try {
                flightService.releaseSeats(
                        reservation.getFlight().getFlightNumber(),
                        seats.size()
                );
            } catch (SQLException e) {
                System.err.println("Warning: Could not update flight available seats: " + e.getMessage());
            }
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationDAO.update(reservation);
    }

    public Reservation modifyReservation(int reservationId, List<Seat> newSeats) throws SQLException {
        Reservation oldReservation = reservationDAO.findById(reservationId);
        if (oldReservation == null) {
            return null;
        }

        if (oldReservation.getStatus() != ReservationStatus.PENDING &&
            oldReservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot modify cancelled or completed reservations.");
        }

        cancelReservation(reservationId);
        return createReservation(oldReservation.getCustomer(), oldReservation.getFlight(), newSeats);
    }

    public List<Reservation> getCustomerReservations(Customer customer) throws SQLException {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required.");
        }
        return reservationDAO.findByCustomerId(customer.getUserId());
    }

    public List<Reservation> getUserReservations(User user) throws SQLException {
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }
        return reservationDAO.findByCustomerId(user.getUserId());
    }

    public Reservation getReservationById(int reservationId) throws SQLException {
        return reservationDAO.findById(reservationId);
    }

    public List<Reservation> getReservationsByFlight(int flightId) throws SQLException {
        return reservationDAO.findByFlightId(flightId);
    }

    public List<Reservation> getAllReservations() throws SQLException {
        return reservationDAO.findAll();
    }

    public List<Seat> getAvailableSeatsForFlight(Flight flight) throws SQLException {
        if (flight == null) {
            throw new IllegalArgumentException("Flight is required.");
        }

        Integer flightId = getFlightIdByNumber(flight.getFlightNumber());
        if (flightId == null) {
            throw new SQLException("Flight not found: " + flight.getFlightNumber());
        }

        return seatDAO.findAvailableSeatsByFlightId(flightId);
    }

    public List<Seat> getAllSeatsForFlight(Flight flight) throws SQLException {
        if (flight == null) {
            throw new IllegalArgumentException("Flight is required.");
        }

        Integer flightId = getFlightIdByNumber(flight.getFlightNumber());
        if (flightId == null) {
            throw new SQLException("Flight not found: " + flight.getFlightNumber());
        }

        return seatDAO.findByFlightId(flightId);
    }

    private Integer getFlightIdByNumber(String flightNumber) throws SQLException {
        if (flightNumber == null || flightNumber.isEmpty()) {
            return null;
        }

        java.sql.Connection conn = DatabaseConnection.getInstance().getConnection();
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                "SELECT flight_id FROM flights WHERE flight_number = ?")) {
            stmt.setString(1, flightNumber);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("flight_id");
                }
            }
        }
        return null;
    }

    /**
     * Calculate total price with seat class multipliers:
     * BUSINESS = 1.5x, FIRST = 2.5x, ECONOMY = base price
     */
    public double calculateTotalPrice(Flight flight, List<Seat> seats) {
        if (flight == null || seats == null || seats.isEmpty()) {
            return 0.0;
        }

        double basePrice = flight.getPrice();
        double total = 0.0;

        for (Seat seat : seats) {
            double seatPrice = basePrice;

            if (seat.getSeatClass() != null) {
                switch (seat.getSeatClass()) {
                    case BUSINESS:
                        seatPrice *= 1.5;
                        break;
                    case FIRST:
                        seatPrice *= 2.5;
                        break;
                    case ECONOMY:
                    default:
                        break;
                }
            }

            total += seatPrice;
        }

        return total;
    }
}
