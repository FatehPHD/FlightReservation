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

    // Note: Schema doesn't have reservation_seats junction table
    // Seats are linked to reservations via tickets table or need to be managed separately
    // For now, seats will be loaded by querying seats for the flight
    // A proper implementation would require adding reservation_id to seats table or creating junction table

    private UserDAO userDAO;
    private FlightDAO flightDAO;
    private PaymentDAO paymentDAO;
    private SeatDAO seatDAO;

    public ReservationDAOImpl() throws SQLException {
        this.userDAO = new UserDAOImpl();
        this.flightDAO = new FlightDAOImpl();
        this.paymentDAO = new PaymentDAOImpl();
        this.seatDAO = new SeatDAOImpl();
    }

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
                    // Note: Seats are managed separately - they should be updated via SeatDAO
                    // to set is_available = false and link to reservation if seats table has reservation_id
                    // Create new Reservation with generated ID
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
            
            // Note: Seats are managed separately via SeatDAO
            // Update seat availability through SeatDAO if needed
            
            return affected > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);
            int affected = stmt.executeUpdate();
            // Note: Seats should be released (set is_available = true) via SeatDAO
            return affected > 0;
        }
    }

    private Integer getFlightIdByNumber(String flightNumber) throws SQLException {
        Flight flight = flightDAO.findByFlightNumber(flightNumber);
        if (flight == null) {
            return null;
        }
        // Get flight_id from database
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

    private List<Seat> loadSeatsForReservation(Connection conn, int reservationId) throws SQLException {
        // Note: Since schema doesn't have reservation_seats junction table,
        // we load seats by getting the flight_id from reservation and loading all seats for that flight
        // A proper implementation would require seats table to have reservation_id or a junction table
        List<Seat> seats = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT flight_id FROM reservations WHERE reservation_id = ?")) {
            stmt.setInt(1, reservationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int flightId = rs.getInt("flight_id");
                    // Load all seats for the flight (not ideal, but works with current schema)
                    seats = seatDAO.findByFlightId(flightId);
                }
            }
        }
        return seats;
    }

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
            // Convert non-Customer user to Customer for reservation purposes
            customer = new Customer();
            customer.setUserId(user.getUserId());
            customer.setUsername(user.getUsername());
            customer.setPassword(user.getPassword());
            customer.setEmail(user.getEmail());
            customer.setRole(user.getRole());
            customer.setFirstName(user.getUsername()); // Default to username
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
        
        // Load seats
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

