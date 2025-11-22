package tests;

import businesslogic.entities.Payment;
import businesslogic.entities.enums.PaymentMethod;
import businesslogic.entities.enums.PaymentStatus;
import datalayer.dao.PaymentDAO;
import datalayer.impl.PaymentDAOImpl;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class TestPaymentDAO {

    public static void main(String[] args) {
        PaymentDAO dao = new PaymentDAOImpl();

        System.out.println("======================================");
        System.out.println("        PAYMENT DAO FULL TEST         ");
        System.out.println("======================================");

        try {
            // 1) CREATE / SAVE
            System.out.println("\n[1] CREATE & SAVE PAYMENT");
            Payment newPayment = new Payment(
                    0,                        // temp ID, will be replaced by DB
                    150.75,                   // amount
                    LocalDateTime.now(),      // paymentDate
                    PaymentMethod.CREDIT_CARD,
                    "TX-1001",
                    PaymentStatus.COMPLETED
            );

            Payment saved = dao.save(newPayment);
            System.out.println(" -> Saved payment to DB.");
            printPayment("Saved Payment", saved);

            int paymentId = saved.getPaymentId();
            System.out.println(" -> Generated payment_id from DB: " + paymentId);

            // 2) FIND BY ID
            System.out.println("\n[2] FIND BY ID");
            Payment found = dao.findById(paymentId);
            if (found != null) {
                System.out.println(" -> Payment found in DB.");
                printPayment("Found Payment", found);
            } else {
                System.out.println(" -> No payment found with id = " + paymentId);
            }

            // 3) UPDATE (change status)
            System.out.println("\n[3] UPDATE PAYMENT STATUS");
            System.out.println(" -> Changing status from COMPLETED to REFUNDED...");
            found.setStatus(PaymentStatus.REFUNDED);
            boolean updated = dao.update(found);
            System.out.println(" -> Update success? " + updated);

            Payment updatedPayment = dao.findById(paymentId);
            printPayment("Updated Payment", updatedPayment);

            // 4) FIND ALL
            System.out.println("\n[4] FIND ALL PAYMENTS");
            List<Payment> allPayments = dao.findAll();
            System.out.println(" -> Total payments in DB: " + allPayments.size());
            for (Payment p : allPayments) {
                printPayment("Payment Row", p);
            }

            // 5) DELETE
            System.out.println("\n[5] DELETE PAYMENT");
            boolean deleted = dao.delete(paymentId);
            System.out.println(" -> Delete success? " + deleted);

            Payment afterDelete = dao.findById(paymentId);
            System.out.println(" -> Trying to find deleted payment...");
            if (afterDelete == null) {
                System.out.println(" -> Confirmed: payment with id " + paymentId + " no longer exists.");
            } else {
                printPayment("Unexpected Payment Still In DB", afterDelete);
            }

            System.out.println("\n======================================");
            System.out.println("         PAYMENT DAO TEST DONE        ");
            System.out.println("======================================");

        } catch (SQLException e) {
            System.out.println("\n[ERROR] A SQL exception occurred:");
            e.printStackTrace();
        }
    }

    /**
     * Helper to print a payment in a consistent, readable format.
     */
    private static void printPayment(String label, Payment p) {
        if (p == null) {
            System.out.println(label + ": null");
            return;
        }

        System.out.println(label + ":");
        System.out.println("   ID            : " + p.getPaymentId());
        System.out.println("   Amount        : " + p.getAmount());
        System.out.println("   Date/Time     : " + p.getPaymentDate());
        System.out.println("   Method        : " + p.getPaymentMethod());
        System.out.println("   Transaction ID: " + p.getTransactionId());
        System.out.println("   Status        : " + p.getStatus());
    }
}
