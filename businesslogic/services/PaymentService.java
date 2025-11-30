package businesslogic.services;

import businesslogic.entities.Payment;
import businesslogic.entities.Reservation;
import businesslogic.entities.Customer;
import businesslogic.entities.Flight;
import businesslogic.entities.Seat;
import businesslogic.entities.User;
import businesslogic.entities.enums.PaymentMethod;
import businesslogic.entities.enums.PaymentStatus;
import businesslogic.entities.enums.ReservationStatus;
import businesslogic.entities.enums.SeatClass;
import datalayer.dao.PaymentDAO;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Simulates payment processing (no real transactions).
 * Handles card validation, PayPal, and bank transfer payments.
 */
public class PaymentService {
    
    private PaymentDAO paymentDAO;
    
    public PaymentService(PaymentDAO paymentDAO) {
        this.paymentDAO = paymentDAO;
    }
    
    /**
     * Process card payment. Cards ending in 0000 are declined for testing.
     */
    public Payment processPayment(double amount, PaymentMethod paymentMethod,
                                   String cardNumber, String cardHolderName,
                                   String expiryDate, String cvv) throws SQLException {
        
        validatePaymentDetails(amount, paymentMethod, cardNumber, cardHolderName, expiryDate, cvv);
        
        String transactionId = generateTransactionId();
        
        Payment payment = new Payment(
            0,
            amount,
            LocalDateTime.now(),
            paymentMethod,
            transactionId,
            PaymentStatus.PENDING
        );
        
        boolean paymentAuthorized = simulatePaymentAuthorization(cardNumber, amount);
        
        if (paymentAuthorized) {
            Payment savedPayment = paymentDAO.save(payment);
            savedPayment.setStatus(PaymentStatus.COMPLETED);
            paymentDAO.update(savedPayment);
            return savedPayment;
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            Payment savedPayment = paymentDAO.save(payment);
            throw new IllegalStateException("Payment authorization failed. Please check your card details and try again.");
        }
    }
    
    public Payment processPayPalPayment(double amount, String paypalEmail) throws SQLException {
        if (paypalEmail == null || paypalEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("PayPal email is required.");
        }
        
        if (!isValidEmail(paypalEmail)) {
            throw new IllegalArgumentException("Invalid PayPal email address.");
        }
        
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }
        
        String transactionId = generateTransactionId();
        
        Payment payment = new Payment(
            0,
            amount,
            LocalDateTime.now(),
            PaymentMethod.PAYPAL,
            transactionId,
            PaymentStatus.COMPLETED
        );
        
        return paymentDAO.save(payment);
    }
    
    public Payment processBankTransfer(double amount, String bankAccountNumber, 
                                        String routingNumber) throws SQLException {
        if (bankAccountNumber == null || bankAccountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Bank account number is required.");
        }
        
        if (routingNumber == null || routingNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Routing number is required.");
        }
        
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }
        
        String transactionId = generateTransactionId();
        
        Payment payment = new Payment(
            0,
            amount,
            LocalDateTime.now(),
            PaymentMethod.BANK_TRANSFER,
            transactionId,
            PaymentStatus.COMPLETED
        );
        
        return paymentDAO.save(payment);
    }
    
    public Payment refundPayment(int paymentId) throws SQLException {
        Payment payment = paymentDAO.findById(paymentId);
        
        if (payment == null) {
            throw new IllegalArgumentException("Payment not found: " + paymentId);
        }
        
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only completed payments can be refunded.");
        }
        
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentDAO.update(payment);
        
        return payment;
    }
    
    public Payment getPaymentById(int paymentId) throws SQLException {
        return paymentDAO.findById(paymentId);
    }
    
    public Payment getPaymentByTransactionId(String transactionId) throws SQLException {
        return paymentDAO.findByTransactionId(transactionId);
    }
    
    public List<Payment> getAllPayments() throws SQLException {
        return paymentDAO.findAll();
    }
    
    /**
     * Calculate total price with seat class multipliers (same as ReservationService).
     */
    public double calculateTotalPrice(Flight flight, List<Seat> selectedSeats) {
        if (flight == null || selectedSeats == null || selectedSeats.isEmpty()) {
            return 0.0;
        }
        
        double basePrice = flight.getPrice();
        double total = 0.0;
        
        for (Seat seat : selectedSeats) {
            double seatPrice = basePrice;
            
            // Apply seat class multiplier
            if (seat.getSeatClass() != null) {
                switch (seat.getSeatClass()) {
                    case BUSINESS:
                        seatPrice *= 1.5; // 50% premium
                        break;
                    case FIRST:
                        seatPrice *= 2.5; // 150% premium
                        break;
                    case ECONOMY:
                    default:
                        // Base price
                        break;
                }
            }
            
            total += seatPrice;
        }
        
        return total;
    }
    
    public double applyDiscount(double totalPrice, double discountPercent) {
        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("Discount percent must be between 0 and 100.");
        }
        
        return totalPrice * (1 - discountPercent / 100);
    }
    
    /**
     * Validate promo codes: SAVE10=10%, SAVE20=20%, HOLIDAY25=25%, VIP50=50%
     */
    public double validatePromoCode(String promoCode) {
        if (promoCode == null || promoCode.trim().isEmpty()) {
            return 0.0;
        }
        
        String code = promoCode.trim().toUpperCase();
        switch (code) {
            case "SAVE10":
                return 10.0;
            case "SAVE20":
                return 20.0;
            case "HOLIDAY25":
                return 25.0;
            case "VIP50":
                return 50.0;
            default:
                return 0.0;
        }
    }
    
    private void validatePaymentDetails(double amount, PaymentMethod paymentMethod,
                                         String cardNumber, String cardHolderName,
                                         String expiryDate, String cvv) {
        
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }
        
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method is required.");
        }
        
        // For card payments, validate card details
        if (paymentMethod == PaymentMethod.CREDIT_CARD || paymentMethod == PaymentMethod.DEBIT_CARD) {
            
            if (cardNumber == null || cardNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("Card number is required.");
            }
            
            // Remove spaces and dashes from card number
            String cleanCardNumber = cardNumber.replaceAll("[\\s-]", "");
            
            if (!isValidCardNumber(cleanCardNumber)) {
                throw new IllegalArgumentException("Invalid card number. Please enter a valid 13-19 digit card number.");
            }
            
            if (cardHolderName == null || cardHolderName.trim().isEmpty()) {
                throw new IllegalArgumentException("Cardholder name is required.");
            }
            
            if (expiryDate == null || expiryDate.trim().isEmpty()) {
                throw new IllegalArgumentException("Card expiry date is required.");
            }
            
            if (!isValidExpiryDate(expiryDate)) {
                throw new IllegalArgumentException("Invalid or expired card. Please check the expiry date (MM/YY format).");
            }
            
            if (cvv == null || cvv.trim().isEmpty()) {
                throw new IllegalArgumentException("CVV is required.");
            }
            
            if (!isValidCVV(cvv)) {
                throw new IllegalArgumentException("Invalid CVV. Please enter a 3 or 4 digit CVV.");
            }
        }
    }
    
    private boolean isValidCardNumber(String cardNumber) {
        return cardNumber.matches("\\d{13,19}");
    }
    
    private boolean isValidExpiryDate(String expiryDate) {
        if (!expiryDate.matches("\\d{2}/\\d{2}")) {
            return false;
        }
        
        try {
            String[] parts = expiryDate.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]) + 2000; // Convert YY to YYYY
            
            if (month < 1 || month > 12) {
                return false;
            }
            
            // Check if card is not expired
            LocalDateTime now = LocalDateTime.now();
            int currentYear = now.getYear();
            int currentMonth = now.getMonthValue();
            
            if (year < currentYear || (year == currentYear && month < currentMonth)) {
                return false; // Card is expired
            }
            
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean isValidCVV(String cvv) {
        return cvv.matches("\\d{3,4}");
    }
    
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
    
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + 
               "-" + System.currentTimeMillis();
    }
    
    /**
     * Test cards ending in 0000 are declined.
     */
    private boolean simulatePaymentAuthorization(String cardNumber, double amount) {
        if (cardNumber != null && cardNumber.endsWith("0000")) {
            return false;
        }
        
        if (amount > 10000) {
            return true;
        }
        
        return true;
    }
    
    @SuppressWarnings("unused")
    private void simulateProcessingDelay() {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        
        String cleanNumber = cardNumber.replaceAll("[\\s-]", "");
        String lastFour = cleanNumber.substring(cleanNumber.length() - 4);
        
        return "**** **** **** " + lastFour;
    }
    
    public static String getCardType(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "Unknown";
        }
        
        String cleanNumber = cardNumber.replaceAll("[\\s-]", "");
        
        if (cleanNumber.startsWith("4")) {
            return "Visa";
        } else if (cleanNumber.startsWith("5") || cleanNumber.startsWith("2")) {
            return "MasterCard";
        } else if (cleanNumber.startsWith("34") || cleanNumber.startsWith("37")) {
            return "American Express";
        } else if (cleanNumber.startsWith("6")) {
            return "Discover";
        } else {
            return "Card";
        }
    }
}