package datalayer.dao;

import businesslogic.entities.Payment;

import java.sql.SQLException;

public interface PaymentDAO extends BaseDAO<Payment, Integer> {
    
    /**
     * Find payment by transaction ID.
     * @param transactionId Transaction ID
     * @return Payment or null if not found
     * @throws SQLException if database error occurs
     */
    Payment findByTransactionId(String transactionId) throws SQLException;
}
