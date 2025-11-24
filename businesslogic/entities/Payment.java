package businesslogic.entities;

import java.time.LocalDateTime;

import businesslogic.entities.enums.PaymentMethod;
import businesslogic.entities.enums.PaymentStatus;

public class Payment {

    private int paymentId;
    private double amount;
    private LocalDateTime paymentDate;
    private PaymentMethod paymentMethod;
    private String transactionId;
    private PaymentStatus status;

    public Payment(int paymentId, double amount, LocalDateTime paymentDate,
                   PaymentMethod paymentMethod, String transactionId, PaymentStatus status) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.status = status;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}
