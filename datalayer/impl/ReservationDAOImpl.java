// File: datalayer/impl/ReservationDAOImpl.java
package datalayer.impl;

import businesslogic.entities.Customer;
import businesslogic.entities.Flight;
import businesslogic.entities.Payment;
import businesslogic.entities.Reservation;
import businesslogic.entities.enums.ReservationStatus;
import datalayer.dao.ReservationDAO;
import datalayer.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAOImpl implements ReservationDAO {

    private final DatabaseConnection db;

    public ReservationDAOImpl() {
        this.db = DatabaseConnection.getInstance();
    }

    // ---------- Helper: map a ResultSet row to a Reservation ----------
    private Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();

        reservation.setReservationId(rs.getInt("reservation_id"));

        Timestamp bookingTs = rs.getTimestamp("booking_date");
        if (bookingTs != null) {
            reservation.setBookingDate(bookingTs.toLocalDateTime());
        }

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            reservation.setStatus(ReservationStatus.valueOf(statusStr));
        }

        reservation.setTotalPrice(rs.getDouble("total_price"));

        // Minimal Customer object with only ID
        int customerId = rs.getInt("customer_id");
        if (!rs.wasNull()) {
            Customer customer = new Customer();
            customer.setUserId(customerId);
            reservation.setCustomer(customer);
        }

        // Minimal Flight object with only ID
        int flightId = rs.getInt("flight_id");
        if (!rs.wasNull()) {
            Flight flight = new Flight();
            flight.setFlightId(flightId);
            reservation.setFlight(flight);
        }

        // Optional Payment
        int paymentId = rs.getInt("payment_id");
        if (!rs.wasNull()) {
            Payment payment = new Payment();
            payment.setPaymentId(paymentId);
            reservation.setPayment(payment);
        }

        // Seats not loaded here (handled elsewhere if needed)

        return reservation;
    }

    // ---------- SAVE (from BaseDAO) ----------
    @Override
    public Reservation save(Reservation reservation) throws SQLException {
        String sql = "INSERT INTO reservations " +
                "(booking_date, status, total_price, customer_id, flight_id, payment_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        // Ensure booking date is set
        LocalDateTime bookingDate = reservation.getBookingDate();
        if (bookingDate == null) {
            bookingDate = LocalDateTime.now();
            reservation.setBookingDate(bookingDate);
        }

        Connection conn = db.getConnection(); // do NOT close this shared connection

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, Timestamp.valueOf(bookingDate));
            ps.setString(2, reservation.getStatus().name());
            ps.setDouble(3, reservation.getTotalPrice());

            // customer_id (NOT NULL)
            if (reservation.getCustomer() == null) {
                throw new SQLException("Reservation must have a Customer before saving.");
            }
            ps.setInt(4, reservation.getCustomer().getUserId());

            // flight_id (NOT NULL)
            if (reservation.getFlight() == null) {
                throw new SQLException("Reservation must have a Flight before saving.");
            }
            ps.setInt(5, reservation.getFlight().getFlightId());

            // payment_id (nullable)
            if (reservation.getPayment() != null) {
                ps.setInt(6, reservation.getPayment().getPaymentId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    reservation.setReservationId(generatedId);
                }
            }
        }

        return reservation;
    }

    // ---------- FIND BY ID (from BaseDAO) ----------
    @Override
    public Reservation findById(Integer reservationId) throws SQLException {
        if (reservationId == null) {
            return null;
        }

        String sql = "SELECT * FROM reservations WHERE reservation_id = ?";

        Connection conn = db.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reservationId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }

    // ---------- FIND ALL (from BaseDAO) ----------
    @Override
    public List<Reservation> findAll() throws SQLException {
        String sql = "SELECT * FROM reservations";

        List<Reservation> result = new ArrayList<>();

        Connection conn = db.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }

        return result;
    }

    // ---------- FIND BY CUSTOMER (extra) ----------
    @Override
    public List<Reservation> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE customer_id = ?";

        List<Reservation> result = new ArrayList<>();

        Connection conn = db.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }

        return result;
    }

    // ---------- FIND BY FLIGHT (extra) ----------
    @Override
    public List<Reservation> findByFlightId(int flightId) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE flight_id = ?";

        List<Reservation> result = new ArrayList<>();

        Connection conn = db.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, flightId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }

        return result;
    }

    // ---------- FIND BY STATUS (extra) ----------
    @Override
    public List<Reservation> findByStatus(ReservationStatus status) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE status = ?";

        List<Reservation> result = new ArrayList<>();

        Connection conn = db.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }

        return result;
    }

    // ---------- UPDATE (from BaseDAO, returns boolean) ----------
    @Override
    public boolean update(Reservation reservation) throws SQLException {
        String sql = "UPDATE reservations " +
                "SET booking_date = ?, status = ?, total_price = ?, " +
                "customer_id = ?, flight_id = ?, payment_id = ? " +
                "WHERE reservation_id = ?";

        if (reservation.getBookingDate() == null) {
            reservation.setBookingDate(LocalDateTime.now());
        }

        Connection conn = db.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(reservation.getBookingDate()));
            ps.setString(2, reservation.getStatus().name());
            ps.setDouble(3, reservation.getTotalPrice());

            if (reservation.getCustomer() == null) {
                throw new SQLException("Reservation must have a Customer for update.");
            }
            ps.setInt(4, reservation.getCustomer().getUserId());

            if (reservation.getFlight() == null) {
                throw new SQLException("Reservation must have a Flight for update.");
            }
            ps.setInt(5, reservation.getFlight().getFlightId());

            if (reservation.getPayment() != null) {
                ps.setInt(6, reservation.getPayment().getPaymentId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            ps.setInt(7, reservation.getReservationId());

            int updatedRows = ps.executeUpdate();
            return updatedRows > 0;
        }
    }

    // ---------- DELETE (from BaseDAO, returns boolean) ----------
    @Override
    public boolean delete(Integer reservationId) throws SQLException {
        if (reservationId == null) {
            return false;
        }

        String sql = "DELETE FROM reservations WHERE reservation_id = ?";

        Connection conn = db.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reservationId);
            int deletedRows = ps.executeUpdate();
            return deletedRows > 0;
        }
    }
}
