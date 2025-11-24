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

    private static final String INSERT_SQL =
            "INSERT INTO payments (amount, payment_date, payment_method, transaction_id, status) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT * FROM payments WHERE payment_id = ?";

    private static final String SELECT_BY_TRANSACTION_ID_SQL =
            "SELECT * FROM payments WHERE transaction_id = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT * FROM payments";

    private static final String UPDATE_SQL =
            "UPDATE payments SET amount = ?, payment_date = ?, payment_method = ?, " +
            "transaction_id = ?, status = ? WHERE payment_id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM payments WHERE payment_id = ?";

    @Override
    public Payment save(Payment payment) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS
        )) {
            stmt.setDouble(1, payment.getAmount());
            stmt.setTimestamp(2, Timestamp.valueOf(payment.getPaymentDate()));
            stmt.setString(3, payment.getPaymentMethod().name());
            stmt.setString(4, payment.getTransactionId());
            stmt.setString(5, payment.getStatus().name());

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Saving payment failed, no rows affected.");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    // Note: Payment constructor requires paymentId, but we need to create new instance
                    // Since Payment doesn't have setters for all fields, we'll need to work around this
                    // For now, we'll create a new Payment with the generated ID
                    int paymentId = keys.getInt(1);
                    return new Payment(
                        paymentId,
                        payment.getAmount(),
                        payment.getPaymentDate(),
                        payment.getPaymentMethod(),
                        payment.getTransactionId(),
                        payment.getStatus()
                    );
                }
            }
        }

        return payment;
    }

    @Override
    public Payment findById(Integer id) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }

    @Override
    public Payment findByTransactionId(String transactionId) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_TRANSACTION_ID_SQL)) {
            stmt.setString(1, transactionId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }

    @Override
    public List<Payment> findAll() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<Payment> list = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }

        return list;
    }

    @Override
    public boolean update(Payment payment) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            stmt.setDouble(1, payment.getAmount());
            stmt.setTimestamp(2, Timestamp.valueOf(payment.getPaymentDate()));
            stmt.setString(3, payment.getPaymentMethod().name());
            stmt.setString(4, payment.getTransactionId());
            stmt.setString(5, payment.getStatus().name());
            stmt.setInt(6, payment.getPaymentId());

            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);

            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        int paymentId = rs.getInt("payment_id");
        double amount = rs.getDouble("amount");
        Timestamp paymentDateTs = rs.getTimestamp("payment_date");
        LocalDateTime paymentDate = paymentDateTs != null ? paymentDateTs.toLocalDateTime() : null;
        
        String methodStr = rs.getString("payment_method");
        PaymentMethod method = methodStr != null ? PaymentMethod.valueOf(methodStr) : null;
        
        String transactionId = rs.getString("transaction_id");
        
        String statusStr = rs.getString("status");
        PaymentStatus status = statusStr != null ? PaymentStatus.valueOf(statusStr) : null;

        return new Payment(paymentId, amount, paymentDate, method, transactionId, status);
    }
}

