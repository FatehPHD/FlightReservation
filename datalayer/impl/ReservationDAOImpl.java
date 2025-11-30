package datalayer.impl;

import businesslogic.entities.Reservation;
import businesslogic.entities.Customer;
import businesslogic.entities.User;
import businesslogic.entities.Flight;
import businesslogic.entities.Payment;
import businesslogic.entities.Seat;
import businesslogic.entities.enums.ReservationStatus;
import datalayer.dao.ReservationDAO;
import datalayer.dao.UserDAO;
import datalayer.dao.FlightDAO;
import datalayer.dao.PaymentDAO;
import datalayer.dao.SeatDAO;
import datalayer.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAOImpl implements ReservationDAO {

    private static final String INSERT_SQL =
            "INSERT INTO reservations (booking_date, status, total_price, customer_id, flight_id, payment_id) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT * FROM reservations WHERE reservation_id = ?";

    private static final String SELECT_BY_CUSTOMER_ID_SQL =
            "SELECT * FROM reservations WHERE customer_id = ?";

    private static final String SELECT_BY_FLIGHT_ID_SQL =
            "SELECT * FROM reservations WHERE flight_id = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT * FROM reservations";

    private static final String UPDATE_SQL =
            "UPDATE reservations SET booking_date = ?, status = ?, total_price = ?, " +
            "customer_id = ?, flight_id = ?, payment_id = ? WHERE reservation_id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM reservations WHERE reservation_id = ?";

    private final UserDAO userDAO;
    private final FlightDAO flightDAO;
    private final PaymentDAO paymentDAO;
    private final SeatDAO seatDAO;

    public ReservationDAOImpl() throws SQLException {
        this.userDAO = new UserDAOImpl();
        this.flightDAO = new FlightDAOImpl();
        this.paymentDAO = new PaymentDAOImpl();
        this.seatDAO = new SeatDAOImpl();
    }

    // -------------------------------------------------------------------------
    // Public CRUD methods
    // -------------------------------------------------------------------------

    @Override
    public Reservation save(Reservation reservation) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS
        )) {
            stmt.setTimestamp(1, Timestamp.valueOf(reservation.getBookingDate()));
            stmt.setString(2, reservation.getStatus().name());
            stmt.setDouble(3, reservation.getTotalPrice());
            stmt.setInt(4, reservation.getCustomer().getUserId());

            // Get flight_id from flight number
            Integer flightId = getFlightIdByNumber(reservation.getFlight().getFlightNumber());
            if (flightId == null) {
                throw new SQLException("Flight not found: " + reservation.getFlight().getFlightNumber());
            }
            stmt.setInt(5, flightId);

            if (reservation.getPayment() != null) {
                stmt.setInt(6, reservation.getPayment().getPaymentId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Saving reservation failed, no rows affected.");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int reservationId = keys.getInt(1);

                    // Create tickets for each selected seat (tickets are the junction table)
                    createTicketsForReservation(conn, reservationId, reservation);

                    // Return new Reservation with generated ID
                    return new Reservation(
                            reservationId,
                            reservation.getBookingDate(),
                            reservation.getStatus(),
                            reservation.getTotalPrice(),
                            reservation.getCustomer(),
                            reservation.getFlight(),
                            reservation.getPayment(),
                            reservation.getSeats()
                    );
                }
            }
        }

        return reservation;
    }

    @Override
    public Reservation findById(Integer id) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs, conn);
                }
            }
        }

        return null;
    }

    @Override
    public List<Reservation> findByCustomerId(Integer customerId) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Reservation> list = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_CUSTOMER_ID_SQL)) {
            stmt.setInt(1, customerId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs, conn));
                }
            }
        }

        return list;
    }

    @Override
    public List<Reservation> findByFlightId(Integer flightId) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Reservation> list = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_FLIGHT_ID_SQL)) {
            stmt.setInt(1, flightId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs, conn));
                }
            }
        }

        return list;
    }

    @Override
    public List<Reservation> findAll() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Reservation> list = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {

            while (rs.next()) {
                list.add(mapRow(rs, conn));
            }
        }

        return list;
    }

    @Override
    public boolean update(Reservation reservation) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            stmt.setTimestamp(1, Timestamp.valueOf(reservation.getBookingDate()));
            stmt.setString(2, reservation.getStatus().name());
            stmt.setDouble(3, reservation.getTotalPrice());
            stmt.setInt(4, reservation.getCustomer().getUserId());

            Integer flightId = getFlightIdByNumber(reservation.getFlight().getFlightNumber());
            if (flightId == null) {
                return false;
            }
            stmt.setInt(5, flightId);

            if (reservation.getPayment() != null) {
                stmt.setInt(6, reservation.getPayment().getPaymentId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.setInt(7, reservation.getReservationId());

            int affected = stmt.executeUpdate();

            // Seats and tickets can be managed separately if you add modification logic later
            return affected > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);
            int affected = stmt.executeUpdate();
            // Tickets and seat availability should be handled by ON DELETE CASCADE
            // and by your service layer when cancelling reservations
            return affected > 0;
        }
    }

    // -------------------------------------------------------------------------
    // Helpers for flight / tickets / seats
    // -------------------------------------------------------------------------

    private Integer getFlightIdByNumber(String flightNumber) throws SQLException {
        Flight flight = flightDAO.findByFlightNumber(flightNumber);
        if (flight == null) {
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

    /**
     * Build a reasonable passenger name for tickets using the Customer.
     */
    private String buildPassengerName(Customer customer) {
        if (customer == null) return "Unknown Passenger";

        String first = customer.getFirstName();
        String last = customer.getLastName();

        if (first != null && !first.isBlank() && last != null && !last.isBlank()) {
            return first + " " + last;
        }
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (last != null && !last.isBlank()) {
            return last;
        }
        return customer.getUsername() != null ? customer.getUsername() : "Unknown Passenger";
    }

    /**
     * Create one ticket per selected seat for a given reservation.
     */
    private void createTicketsForReservation(Connection conn,
                                             int reservationId,
                                             Reservation reservation) throws SQLException {
        if (reservation.getSeats() == null || reservation.getSeats().isEmpty()) {
            return;
        }

        String passengerName = buildPassengerName(reservation.getCustomer());

        String sql = "INSERT INTO tickets (issue_date, passenger_name, reservation_id, seat_id, barcode) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            for (Seat seat : reservation.getSeats()) {
                stmt.setTimestamp(1, now);
                stmt.setString(2, passengerName);
                stmt.setInt(3, reservationId);
                stmt.setInt(4, seat.getSeatId());
                stmt.setString(5, "TKT-" + reservationId + "-" + seat.getSeatId());
                stmt.addBatch();
            }

            stmt.executeBatch();
        }
    }

    /**
     * Load seats for a reservation via the tickets table.
     */
    private List<Seat> loadSeatsForReservation(Connection conn, int reservationId) throws SQLException {
        List<Seat> seats = new ArrayList<>();

        String sql = "SELECT seat_id FROM tickets WHERE reservation_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int seatId = rs.getInt("seat_id");
                    Seat seat = seatDAO.findById(seatId);
                    if (seat != null) {
                        seats.add(seat);
                    }
                }
            }
        }

        return seats;
    }

    // -------------------------------------------------------------------------
    // Row mapping
    // -------------------------------------------------------------------------

    private Reservation mapRow(ResultSet rs, Connection conn) throws SQLException {
        int reservationId = rs.getInt("reservation_id");
        Timestamp bookingDateTs = rs.getTimestamp("booking_date");
        LocalDateTime bookingDate = bookingDateTs != null ? bookingDateTs.toLocalDateTime() : null;

        String statusStr = rs.getString("status");
        ReservationStatus status = statusStr != null ? ReservationStatus.valueOf(statusStr) : null;

        double totalPrice = rs.getDouble("total_price");

        // Load related entities
        int customerId = rs.getInt("customer_id");
        User user = userDAO.findById(customerId);

        // Convert User to Customer (handles cases where admin/agent made booking)
        Customer customer;
        if (user instanceof Customer) {
            customer = (Customer) user;
        } else if (user != null) {
            customer = new Customer();
            customer.setUserId(user.getUserId());
            customer.setUsername(user.getUsername());
            customer.setPassword(user.getPassword());
            customer.setEmail(user.getEmail());
            customer.setRole(user.getRole());
            customer.setFirstName(user.getUsername());
            customer.setMembershipStatus(businesslogic.entities.enums.MembershipStatus.REGULAR);
        } else {
            throw new SQLException("User not found for customer_id: " + customerId);
        }

        int flightId = rs.getInt("flight_id");
        Flight flight = flightDAO.findById(flightId);

        Payment payment = null;
        int paymentId = rs.getInt("payment_id");
        if (!rs.wasNull() && paymentId > 0) {
            payment = paymentDAO.findById(paymentId);
        }

        // Load seats via tickets
        List<Seat> seats = loadSeatsForReservation(conn, reservationId);

        return new Reservation(
                reservationId,
                bookingDate,
                status,
                totalPrice,
                customer,
                flight,
                payment,
                seats
        );
    }
}
