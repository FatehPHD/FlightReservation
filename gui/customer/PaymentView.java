package gui.customer;

import gui.common.ViewManager;
import gui.common.ErrorDialog;
import gui.common.ConfirmDialog;
import businesslogic.entities.Flight;
import businesslogic.entities.Seat;
import businesslogic.entities.Payment;
import businesslogic.entities.Reservation;
import businesslogic.entities.User;
import businesslogic.entities.enums.PaymentMethod;
import businesslogic.entities.enums.SeatClass;
import businesslogic.services.PaymentService;
import businesslogic.services.ReservationService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Payment processing form (SIMULATED - no real transactions).
 * Collects payment details and processes booking payment.
 * Creates a reservation after successful payment simulation.
 * 
 * Location: gui/customer/PaymentView.java
 */
public class PaymentView extends JPanel {
    
    private ViewManager viewManager;
    private Flight flight;
    private List<Seat> selectedSeats;
    private PaymentService paymentService;
    private ReservationService reservationService;
    
    // Payment method selection
    private JComboBox<String> paymentMethodComboBox;
    private CardLayout paymentCardLayout;
    private JPanel paymentDetailsPanel;
    
    // Credit/Debit Card fields
    private JTextField cardNumberField;
    private JTextField cardHolderField;
    private JTextField expiryDateField;
    private JTextField cvvField;
    
    // PayPal fields
    private JTextField paypalEmailField;
    
    // Bank Transfer fields
    private JTextField bankAccountField;
    private JTextField routingNumberField;
    
    // Promo code
    private JTextField promoCodeField;
    private JButton applyPromoBtn;
    private double appliedDiscount = 0.0;
    
    // Price display
    private JLabel subtotalLabel;
    private JLabel discountLabel;
    private JLabel totalLabel;
    private double subtotal;
    private double total;
    
    // Processing indicator
    private JButton payBtn;
    private JLabel processingLabel;
    
    public PaymentView(ViewManager viewManager, Flight flight, List<Seat> selectedSeats) {
        this.viewManager = viewManager;
        this.flight = flight;
        this.selectedSeats = selectedSeats;
        this.paymentService = viewManager.getPaymentService();
        this.reservationService = viewManager.getReservationService();
        
        // Calculate initial price
        this.subtotal = paymentService.calculateTotalPrice(flight, selectedSeats);
        this.total = subtotal;
        
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // ==================== Title Panel ====================
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        
        JLabel title = new JLabel("Payment");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(title);
        
        JLabel subtitle = new JLabel("Complete your booking - Payment Simulation");
        subtitle.setFont(new Font("Arial", Font.ITALIC, 12));
        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(subtitle);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // ==================== Main Content Panel ====================
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        
        // Left side: Booking summary
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.4;
        gbc.weighty = 1.0;
        mainPanel.add(createBookingSummaryPanel(), gbc);
        
        // Right side: Payment form
        gbc.gridx = 1;
        gbc.weightx = 0.6;
        mainPanel.add(createPaymentFormPanel(), gbc);
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
        
        // ==================== Button Panel ====================
        add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    /**
     * Create booking summary panel showing flight and seat details.
     */
    private JPanel createBookingSummaryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Booking Summary",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)));
        
        // Flight Info
        JPanel flightPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        flightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        flightPanel.add(new JLabel("Flight:"));
        flightPanel.add(new JLabel(flight.getFlightNumber()));
        
        if (flight.getRoute() != null) {
            String origin = flight.getRoute().getOrigin() != null 
                ? flight.getRoute().getOrigin().getAirportCode() : "N/A";
            String destination = flight.getRoute().getDestination() != null
                ? flight.getRoute().getDestination().getAirportCode() : "N/A";
            
            flightPanel.add(new JLabel("Route:"));
            flightPanel.add(new JLabel(origin + " → " + destination));
        }
        
        if (flight.getDepartureTime() != null) {
            flightPanel.add(new JLabel("Departure:"));
            flightPanel.add(new JLabel(flight.getDepartureTime().format(
                DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))));
        }
        
        if (flight.getArrivalTime() != null) {
            flightPanel.add(new JLabel("Arrival:"));
            flightPanel.add(new JLabel(flight.getArrivalTime().format(
                DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))));
        }
        
        panel.add(flightPanel);
        
        // Separator
        panel.add(new JSeparator());
        
        // Selected Seats
        JPanel seatsPanel = new JPanel();
        seatsPanel.setLayout(new BoxLayout(seatsPanel, BoxLayout.Y_AXIS));
        seatsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel seatsTitle = new JLabel("Selected Seats (" + selectedSeats.size() + "):");
        seatsTitle.setFont(new Font("Arial", Font.BOLD, 12));
        seatsPanel.add(seatsTitle);
        seatsPanel.add(Box.createVerticalStrut(5));
        
        double basePrice = flight.getPrice();
        for (Seat seat : selectedSeats) {
            double seatPrice = calculateSeatPrice(basePrice, seat.getSeatClass());
            String seatInfo = String.format("  %s (%s) - $%.2f", 
                seat.getSeatNumber(),
                seat.getSeatClass() != null ? seat.getSeatClass().toString() : "Economy",
                seatPrice);
            seatsPanel.add(new JLabel(seatInfo));
        }
        
        panel.add(seatsPanel);
        
        // Separator
        panel.add(new JSeparator());
        
        // Price Summary
        JPanel pricePanel = new JPanel(new GridLayout(0, 2, 5, 5));
        pricePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        pricePanel.add(new JLabel("Subtotal:"));
        subtotalLabel = new JLabel(String.format("$%.2f", subtotal));
        pricePanel.add(subtotalLabel);
        
        pricePanel.add(new JLabel("Discount:"));
        discountLabel = new JLabel("-$0.00");
        discountLabel.setForeground(new Color(0, 128, 0));
        pricePanel.add(discountLabel);
        
        JLabel totalText = new JLabel("Total:");
        totalText.setFont(new Font("Arial", Font.BOLD, 14));
        pricePanel.add(totalText);
        
        totalLabel = new JLabel(String.format("$%.2f", total));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalLabel.setForeground(new Color(0, 100, 0));
        pricePanel.add(totalLabel);
        
        panel.add(pricePanel);
        
        // Promo Code
        JPanel promoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        promoPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        promoPanel.add(new JLabel("Promo Code:"));
        promoCodeField = new JTextField(10);
        promoPanel.add(promoCodeField);
        
        applyPromoBtn = new JButton("Apply");
        applyPromoBtn.addActionListener(e -> applyPromoCode());
        promoPanel.add(applyPromoBtn);
        
        panel.add(promoPanel);
        
        // Add glue to push everything to top
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    /**
     * Create payment form panel with different payment methods.
     */
    private JPanel createPaymentFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Payment Details",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)));
        
        // Payment method selector
        JPanel methodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        methodPanel.add(new JLabel("Payment Method:"));
        
        String[] paymentMethods = {"Credit Card", "Debit Card", "PayPal", "Bank Transfer"};
        paymentMethodComboBox = new JComboBox<>(paymentMethods);
        paymentMethodComboBox.setPreferredSize(new Dimension(150, 25));
        paymentMethodComboBox.addActionListener(e -> switchPaymentMethod());
        methodPanel.add(paymentMethodComboBox);
        
        panel.add(methodPanel, BorderLayout.NORTH);
        
        // Payment details with CardLayout for different methods
        paymentCardLayout = new CardLayout();
        paymentDetailsPanel = new JPanel(paymentCardLayout);
        
        // Credit/Debit Card panel - same panel for both (they have the same fields)
        JPanel cardPanel = createCardPaymentPanel();
        paymentDetailsPanel.add(cardPanel, "Card");
        
        // PayPal panel
        paymentDetailsPanel.add(createPayPalPanel(), "PayPal");
        
        // Bank Transfer panel
        paymentDetailsPanel.add(createBankTransferPanel(), "Bank Transfer");
        
        panel.add(paymentDetailsPanel, BorderLayout.CENTER);
        
        // Security notice
        JPanel securityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel securityLabel = new JLabel("🔒 This is a simulated payment - no real transaction will occur.");
        securityLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        securityLabel.setForeground(Color.GRAY);
        securityPanel.add(securityLabel);
        panel.add(securityPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create credit/debit card payment panel.
     */
    private JPanel createCardPaymentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Card Number
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Card Number:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 200;
        cardNumberField = new JTextField(20);
        cardNumberField.setToolTipText("Enter 16-digit card number (spaces optional)");
        panel.add(cardNumberField, gbc);
        
        // Cardholder Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.ipadx = 0;
        panel.add(new JLabel("Cardholder Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 200;
        cardHolderField = new JTextField(20);
        cardHolderField.setToolTipText("Name as it appears on card");
        panel.add(cardHolderField, gbc);
        
        // Expiry Date and CVV on same row
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.ipadx = 0;
        panel.add(new JLabel("Expiry Date (MM/YY):"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.ipadx = 0;
        JPanel expiryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        expiryDateField = new JTextField(7);
        expiryDateField.setToolTipText("MM/YY");
        expiryPanel.add(expiryDateField);
        
        expiryPanel.add(Box.createHorizontalStrut(20));
        expiryPanel.add(new JLabel("CVV:"));
        expiryPanel.add(Box.createHorizontalStrut(5));
        cvvField = new JTextField(5);
        cvvField.setToolTipText("3 or 4 digit security code");
        expiryPanel.add(cvvField);
        panel.add(expiryPanel, gbc);
        
        // Test card info
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JLabel testInfo = new JLabel("<html><i>Test cards: Use any valid card number format. " +
            "Cards ending in 0000 will be declined.</i></html>");
        testInfo.setFont(new Font("Arial", Font.PLAIN, 10));
        testInfo.setForeground(Color.GRAY);
        panel.add(testInfo, gbc);
        
        return panel;
    }
    
    /**
     * Create PayPal payment panel.
     */
    private JPanel createPayPalPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // PayPal Email
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("PayPal Email:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 200;
        paypalEmailField = new JTextField(25);
        paypalEmailField.setToolTipText("Enter your PayPal email address");
        panel.add(paypalEmailField, gbc);
        
        // Info text
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.ipadx = 0;
        JLabel infoLabel = new JLabel("<html><i>You will be redirected to PayPal to complete payment. (Simulated)</i></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        infoLabel.setForeground(Color.GRAY);
        panel.add(infoLabel, gbc);
        
        return panel;
    }
    
    /**
     * Create bank transfer payment panel.
     */
    private JPanel createBankTransferPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Bank Account Number
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Account Number:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 200;
        bankAccountField = new JTextField(20);
        bankAccountField.setToolTipText("Enter your bank account number");
        panel.add(bankAccountField, gbc);
        
        // Routing Number
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.ipadx = 0;
        panel.add(new JLabel("Routing Number:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 200;
        routingNumberField = new JTextField(20);
        routingNumberField.setToolTipText("Enter your bank routing number");
        panel.add(routingNumberField, gbc);
        
        // Info text
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.ipadx = 0;
        JLabel infoLabel = new JLabel("<html><i>Bank transfers may take 3-5 business days to process. (Simulated)</i></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        infoLabel.setForeground(Color.GRAY);
        panel.add(infoLabel, gbc);
        
        return panel;
    }
    
    /**
     * Create button panel with Pay and Cancel buttons.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Back button
        JButton backBtn = new JButton("Back to Seat Selection");
        backBtn.setPreferredSize(new Dimension(180, 40));
        backBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        backBtn.addActionListener(e -> {
            viewManager.showView("SEAT_SELECTION", 
                new SeatSelectionView(viewManager, flight));
        });
        panel.add(backBtn);
        
        // Processing label (initially hidden)
        processingLabel = new JLabel("Processing payment...");
        processingLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        processingLabel.setForeground(Color.BLUE);
        processingLabel.setVisible(false);
        panel.add(processingLabel);
        
        // Pay button
        payBtn = new JButton("Pay " + String.format("$%.2f", total));
        payBtn.setPreferredSize(new Dimension(180, 40));
        payBtn.setFont(new Font("Arial", Font.BOLD, 14));
        payBtn.setBackground(new Color(46, 139, 87));
        payBtn.setForeground(Color.WHITE);
        payBtn.setOpaque(true);
        payBtn.setBorderPainted(false);
        payBtn.setFocusPainted(false);
        payBtn.addActionListener(e -> processPayment());
        panel.add(payBtn);
        
        return panel;
    }
    
    /**
     * Switch payment method panel based on selection.
     */
    private void switchPaymentMethod() {
        String selected = (String) paymentMethodComboBox.getSelectedItem();
        
        // Credit Card and Debit Card use the same "Card" panel
        if ("Credit Card".equals(selected) || "Debit Card".equals(selected)) {
            paymentCardLayout.show(paymentDetailsPanel, "Card");
        } else {
            paymentCardLayout.show(paymentDetailsPanel, selected);
        }
    }
    
    /**
     * Apply promo code and update prices.
     */
    private void applyPromoCode() {
        String code = promoCodeField.getText().trim();
        
        if (code.isEmpty()) {
            ErrorDialog.show(this, "Please enter a promo code.");
            return;
        }
        
        double discount = paymentService.validatePromoCode(code);
        
        if (discount > 0) {
            appliedDiscount = discount;
            updatePriceDisplay();
            
            promoCodeField.setEnabled(false);
            applyPromoBtn.setEnabled(false);
            applyPromoBtn.setText("Applied ✓");
            
            JOptionPane.showMessageDialog(this,
                String.format("Promo code applied! You saved %.0f%%", discount),
                "Promo Code Applied", JOptionPane.INFORMATION_MESSAGE);
        } else {
            ErrorDialog.show(this, "Invalid promo code. Please try again.");
            promoCodeField.selectAll();
        }
    }
    
    /**
     * Update price display with discount.
     */
    private void updatePriceDisplay() {
        double discountAmount = subtotal * (appliedDiscount / 100);
        total = subtotal - discountAmount;
        
        subtotalLabel.setText(String.format("$%.2f", subtotal));
        discountLabel.setText(String.format("-$%.2f (%.0f%%)", discountAmount, appliedDiscount));
        totalLabel.setText(String.format("$%.2f", total));
        
        payBtn.setText("Pay " + String.format("$%.2f", total));
    }
    
    /**
     * Calculate seat price based on class.
     */
    private double calculateSeatPrice(double basePrice, SeatClass seatClass) {
        if (seatClass == null) {
            return basePrice;
        }
        
        switch (seatClass) {
            case BUSINESS:
                return basePrice * 1.5;
            case FIRST:
                return basePrice * 2.5;
            case ECONOMY:
            default:
                return basePrice;
        }
    }
    
    /**
     * Process the payment.
     */
    private void processPayment() {
        // Confirm payment
        if (!ConfirmDialog.showPayment(this, total, "$")) {
            return;
        }
        
        // Get current user
        User currentUser = viewManager.getCurrentUser();
        if (currentUser == null) {
            ErrorDialog.show(this, "You must be logged in to make a payment.");
            return;
        }
        
        // Disable pay button and show processing
        payBtn.setEnabled(false);
        processingLabel.setVisible(true);
        
        // Process payment in background thread
        SwingWorker<Payment, Void> worker = new SwingWorker<Payment, Void>() {
            private Exception error;
            
            @Override
            protected Payment doInBackground() throws Exception {
                try {
                    String selectedMethod = (String) paymentMethodComboBox.getSelectedItem();
                    Payment payment;
                    
                    switch (selectedMethod) {
                        case "Credit Card":
                            payment = paymentService.processPayment(
                                total,
                                PaymentMethod.CREDIT_CARD,
                                cardNumberField.getText(),
                                cardHolderField.getText(),
                                expiryDateField.getText(),
                                cvvField.getText()
                            );
                            break;
                            
                        case "Debit Card":
                            payment = paymentService.processPayment(
                                total,
                                PaymentMethod.DEBIT_CARD,
                                cardNumberField.getText(),
                                cardHolderField.getText(),
                                expiryDateField.getText(),
                                cvvField.getText()
                            );
                            break;
                            
                        case "PayPal":
                            payment = paymentService.processPayPalPayment(
                                total,
                                paypalEmailField.getText()
                            );
                            break;
                            
                        case "Bank Transfer":
                            payment = paymentService.processBankTransfer(
                                total,
                                bankAccountField.getText(),
                                routingNumberField.getText()
                            );
                            break;
                            
                        default:
                            throw new IllegalStateException("Unknown payment method: " + selectedMethod);
                    }
                    
                    return payment;
                    
                } catch (Exception e) {
                    error = e;
                    return null;
                }
            }
            
            @Override
            protected void done() {
                processingLabel.setVisible(false);
                payBtn.setEnabled(true);
                
                if (error != null) {
                    if (error instanceof IllegalArgumentException) {
                        ErrorDialog.showValidation(PaymentView.this, error.getMessage());
                    } else if (error instanceof IllegalStateException) {
                        ErrorDialog.showPaymentFailed(PaymentView.this, error.getMessage());
                    } else {
                        ErrorDialog.show(PaymentView.this, 
                            "Payment failed: " + error.getMessage(), (Exception) error);
                    }
                    return;
                }
                
                try {
                    Payment payment = get();
                    if (payment != null) {
                        // Create reservation with the payment
                        createReservation(payment);
                    }
                } catch (Exception e) {
                    ErrorDialog.show(PaymentView.this, "Error processing payment result.", e);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Create reservation after successful payment.
     */
    private void createReservation(Payment payment) {
        try {
            User currentUser = viewManager.getCurrentUser();
            
            // Create the reservation
            Reservation reservation = reservationService.createReservationForUser(
                currentUser, flight, selectedSeats, payment);
            
            // Navigate to confirmation
            viewManager.showView("CONFIRMATION", 
                new ConfirmationView(viewManager, reservation));
            
        } catch (SQLException e) {
            ErrorDialog.show(this, "Payment successful but failed to create reservation: " + e.getMessage(), e);
        } catch (IllegalArgumentException | IllegalStateException e) {
            ErrorDialog.show(this, "Payment successful but failed to create reservation: " + e.getMessage());
        }
    }
}