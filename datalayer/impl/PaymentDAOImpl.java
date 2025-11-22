// File: datalayer/impl/PaymentDAOImpl.java
package datalayer.impl;

import businesslogic.entities.Payment;
import businesslogic.entities.enums.PaymentMethod;
import businesslogic.entities.enums.PaymentStatus;
import datalayer.dao.PaymentDAO;
import datalayer.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAOImpl implements PaymentDAO {

    private final DatabaseConnection db;

    public PaymentDAOImpl() {
        this.db = DatabaseConnection.getInstance();
    }

    // ----- Helper: map ResultSet row to Payment -----
    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment payment = new Payment();

        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setAmount(rs.getDouble("amount"));

        Timestamp ts = rs.getTimestamp("payment_date");
        if (ts != null) {
            payment.setPaymentDate(ts.toLocalDateTime());
        }

        String methodStr = rs.getString("payment_method");
        if (methodStr != null) {
            payment.setPaymentMethod(PaymentMethod.valueOf(methodStr));
        }

        payment.setTransactionId(rs.getString("transaction_id"));

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            payment.setStatus(PaymentStatus.valueOf(statusStr));
        }

        return payment;
    }

    // ----- CRUD from BaseDAO -----

    @Override
    public Payment save(Payment payment) throws SQLException {
        String sql = "INSERT INTO payments " +
                "(amount, payment_date, payment_method, transaction_id, status) " +
                "VALUES (?, ?, ?, ?, ?)";

        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDouble(1, payment.getAmount());
            LocalDateTime paymentDate = payment.getPaymentDate();
            ps.setTimestamp(2, paymentDate != null ? Timestamp.valueOf(paymentDate) : null);
            ps.setString(3, payment.getPaymentMethod() != null
                    ? payment.getPaymentMethod().name()
                    : null);
            ps.setString(4, payment.getTransactionId());
            ps.setString(5, payment.getStatus() != null
                    ? payment.getStatus().name()
                    : null);

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Inserting payment failed, no rows affected.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    payment.setPaymentId(id);
                }
            }
        }

        return payment;
    }

    @Override
    public Payment findById(Integer id) throws SQLException {
        String sql = "SELECT payment_id, amount, payment_date, payment_method, transaction_id, status " +
                     "FROM payments WHERE payment_id = ?";

        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }

    @Override
    public List<Payment> findAll() throws SQLException {
        String sql = "SELECT payment_id, amount, payment_date, payment_method, transaction_id, status " +
                     "FROM payments";

        List<Payment> result = new ArrayList<>();

        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }

        return result;
    }

    @Override
    public boolean update(Payment payment) throws SQLException {
        String sql = "UPDATE payments SET " +
                     "amount = ?, payment_date = ?, payment_method = ?, " +
                     "transaction_id = ?, status = ? " +
                     "WHERE payment_id = ?";

        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, payment.getAmount());
            LocalDateTime paymentDate = payment.getPaymentDate();
            ps.setTimestamp(2, paymentDate != null ? Timestamp.valueOf(paymentDate) : null);
            ps.setString(3, payment.getPaymentMethod() != null
                    ? payment.getPaymentMethod().name()
                    : null);
            ps.setString(4, payment.getTransactionId());
            ps.setString(5, payment.getStatus() != null
                    ? payment.getStatus().name()
                    : null);
            ps.setInt(6, payment.getPaymentId());

            int affected = ps.executeUpdate();
            return affected > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM payments WHERE payment_id = ?";

        Connection conn = db.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            return affected > 0;
        }
    }
}
