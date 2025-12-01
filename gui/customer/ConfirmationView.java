package gui.customer;

import gui.common.ViewManager;
import businesslogic.entities.Reservation;
import businesslogic.entities.Seat;
import businesslogic.entities.Flight;
import businesslogic.entities.Payment;
import businesslogic.entities.Customer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.print.PrinterException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Booking confirmation screen.
 * Shows booking details and confirmation number after successful payment.
 * Provides options to print confirmation or return to dashboard.
 * 
 * Location: gui/customer/ConfirmationView.java
 */
public class ConfirmationView extends JPanel {
    
    private ViewManager viewManager;
    private Reservation reservation;
    
    public ConfirmationView(ViewManager viewManager, Reservation reservation) {
        this.viewManager = viewManager;
        this.reservation = reservation;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        setBackground(Color.WHITE);
        
        // ==================== Success Header ====================
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // ==================== Main Content ====================
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;
        
        // Left: Booking details
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        mainPanel.add(createBookingDetailsPanel(), gbc);
        
        // Right: Payment details
        gbc.gridx = 1;
        mainPanel.add(createPaymentDetailsPanel(), gbc);
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
        
        // ==================== Button Panel ====================
        add(createButtonPanel(), BorderLayout.SOUTH);
    }
    
    /**
     * Create success header with confirmation number.
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(240, 255, 240)); // Light green background
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(46, 139, 87), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel("Booking Confirmed!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(46, 139, 87));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        
        panel.add(Box.createVerticalStrut(10));
        
        // Confirmation number
        JLabel confLabel = new JLabel("Confirmation Number:");
        confLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        confLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(confLabel);
        
        String confirmationNumber = generateConfirmationNumber();
        JLabel confNumber = new JLabel(confirmationNumber);
        confNumber.setFont(new Font("Monospaced", Font.BOLD, 24));
        confNumber.setForeground(new Color(0, 100, 0));
        confNumber.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(confNumber);
        
        panel.add(Box.createVerticalStrut(10));
        
        JLabel emailNote = new JLabel("A confirmation email has been sent to your registered email address. (Simulated)");
        emailNote.setFont(new Font("Arial", Font.ITALIC, 12));
        emailNote.setForeground(Color.GRAY);
        emailNote.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(emailNote);
        
        return panel;
    }
    
    /**
     * Create booking details panel.
     */
    private JPanel createBookingDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Booking Details",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)));
        
        Flight flight = reservation.getFlight();
        Customer customer = reservation.getCustomer();
        List<Seat> seats = reservation.getSeats();
        
        // Reservation info
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 8));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        addInfoRow(infoPanel, "Reservation ID:", String.valueOf(reservation.getReservationId()));
        addInfoRow(infoPanel, "Booking Date:", reservation.getBookingDate() != null 
            ? reservation.getBookingDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
            : "N/A");
        addInfoRow(infoPanel, "Status:", reservation.getStatus() != null 
            ? reservation.getStatus().toString() : "CONFIRMED");
        
        panel.add(infoPanel);
        panel.add(new JSeparator());
        
        // Flight info
        JPanel flightPanel = new JPanel(new GridLayout(0, 2, 10, 8));
        flightPanel.setBackground(Color.WHITE);
        flightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel flightTitle = new JLabel("Flight Information");
        flightTitle.setFont(new Font("Arial", Font.BOLD, 12));
        flightPanel.add(flightTitle);
        flightPanel.add(new JLabel("")); // Spacer
        
        if (flight != null) {
            addInfoRow(flightPanel, "Flight Number:", flight.getFlightNumber());
            
            if (flight.getRoute() != null) {
                String origin = flight.getRoute().getOrigin() != null 
                    ? flight.getRoute().getOrigin().getAirportCode() + " - " +
                      flight.getRoute().getOrigin().getCity()
                    : "N/A";
                String destination = flight.getRoute().getDestination() != null 
                    ? flight.getRoute().getDestination().getAirportCode() + " - " +
                      flight.getRoute().getDestination().getCity()
                    : "N/A";
                
                addInfoRow(flightPanel, "From:", origin);
                addInfoRow(flightPanel, "To:", destination);
            }
            
            if (flight.getDepartureTime() != null) {
                addInfoRow(flightPanel, "Departure:", 
                    flight.getDepartureTime().format(DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy 'at' HH:mm")));
            }
            
            if (flight.getArrivalTime() != null) {
                addInfoRow(flightPanel, "Arrival:", 
                    flight.getArrivalTime().format(DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy 'at' HH:mm")));
            }
            
            if (flight.getAircraft() != null) {
                addInfoRow(flightPanel, "Aircraft:", flight.getAircraft().getModel());
            }
        }
        
        panel.add(flightPanel);
        panel.add(new JSeparator());
        
        // Passenger info
        JPanel passengerPanel = new JPanel(new GridLayout(0, 2, 10, 8));
        passengerPanel.setBackground(Color.WHITE);
        passengerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel passengerTitle = new JLabel("Passenger Information");
        passengerTitle.setFont(new Font("Arial", Font.BOLD, 12));
        passengerPanel.add(passengerTitle);
        passengerPanel.add(new JLabel("")); // Spacer
        
        if (customer != null) {
            String fullName = (customer.getFirstName() != null ? customer.getFirstName() : "") + " " +
                             (customer.getLastName() != null ? customer.getLastName() : "");
            addInfoRow(passengerPanel, "Name:", fullName.trim().isEmpty() ? customer.getUsername() : fullName.trim());
            addInfoRow(passengerPanel, "Email:", customer.getEmail() != null ? customer.getEmail() : "N/A");
        }
        
        panel.add(passengerPanel);
        panel.add(new JSeparator());
        
        // Seat info
        JPanel seatPanel = new JPanel();
        seatPanel.setLayout(new BoxLayout(seatPanel, BoxLayout.Y_AXIS));
        seatPanel.setBackground(Color.WHITE);
        seatPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel seatTitle = new JLabel("Seat(s) Assigned");
        seatTitle.setFont(new Font("Arial", Font.BOLD, 12));
        seatPanel.add(seatTitle);
        seatPanel.add(Box.createVerticalStrut(5));
        
        if (seats != null && !seats.isEmpty()) {
            for (Seat seat : seats) {
                String seatInfo = "  • Seat " + seat.getSeatNumber() + 
                    " (" + (seat.getSeatClass() != null ? seat.getSeatClass().toString() : "Economy") + ")";
                JLabel seatLabel = new JLabel(seatInfo);
                seatLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                seatPanel.add(seatLabel);
            }
        } else {
            seatPanel.add(new JLabel("  Seats will be assigned at check-in"));
        }
        
        panel.add(seatPanel);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    /**
     * Create payment details panel.
     */
    private JPanel createPaymentDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Payment Details",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)));
        
        Payment payment = reservation.getPayment();
        
        JPanel paymentPanel = new JPanel(new GridLayout(0, 2, 10, 8));
        paymentPanel.setBackground(Color.WHITE);
        paymentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        if (payment != null) {
            addInfoRow(paymentPanel, "Transaction ID:", 
                payment.getTransactionId() != null ? payment.getTransactionId() : "N/A");
            addInfoRow(paymentPanel, "Payment Date:", 
                payment.getPaymentDate() != null 
                    ? payment.getPaymentDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                    : "N/A");
            addInfoRow(paymentPanel, "Payment Method:", 
                payment.getPaymentMethod() != null ? formatPaymentMethod(payment.getPaymentMethod().toString()) : "N/A");
            addInfoRow(paymentPanel, "Payment Status:", 
                payment.getStatus() != null ? payment.getStatus().toString() : "N/A");
        }
        
        panel.add(paymentPanel);
        panel.add(new JSeparator());
        
        // Price summary
        JPanel pricePanel = new JPanel(new GridLayout(0, 2, 10, 8));
        pricePanel.setBackground(Color.WHITE);
        pricePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel priceTitle = new JLabel("Price Summary");
        priceTitle.setFont(new Font("Arial", Font.BOLD, 12));
        pricePanel.add(priceTitle);
        pricePanel.add(new JLabel("")); // Spacer
        
        addInfoRow(pricePanel, "Total Paid:", String.format("$%.2f", reservation.getTotalPrice()));
        
        if (payment != null && payment.getAmount() != reservation.getTotalPrice()) {
            addInfoRow(pricePanel, "Amount Charged:", String.format("$%.2f", payment.getAmount()));
        }
        
        panel.add(pricePanel);
        panel.add(new JSeparator());
        
        // Important notes
        JPanel notesPanel = new JPanel();
        notesPanel.setLayout(new BoxLayout(notesPanel, BoxLayout.Y_AXIS));
        notesPanel.setBackground(new Color(255, 255, 240)); // Light yellow
        notesPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 200, 100)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel notesTitle = new JLabel("Important Information");
        notesTitle.setFont(new Font("Arial", Font.BOLD, 12));
        notesPanel.add(notesTitle);
        notesPanel.add(Box.createVerticalStrut(5));
        
        String[] notes = {
            "• Please arrive at the airport at least 2 hours before departure.",
            "• Bring a valid government-issued photo ID.",
            "• Check-in online 24 hours before your flight.",
            "• Baggage allowance: 1 carry-on + 1 personal item.",
            "• This is a simulated booking - no real flight has been reserved."
        };
        
        for (String note : notes) {
            JLabel noteLabel = new JLabel(note);
            noteLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            notesPanel.add(noteLabel);
        }
        
        panel.add(notesPanel);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    /**
     * Create button panel.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Book Another Flight button
        JButton bookAnotherBtn = new JButton("Book Another Flight");
        bookAnotherBtn.setPreferredSize(new Dimension(180, 40));
        bookAnotherBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        bookAnotherBtn.addActionListener(e -> {
            viewManager.showView("FLIGHT_SEARCH", new FlightSearchView(viewManager));
        });
        panel.add(bookAnotherBtn);
        
        // Back to Dashboard button
        JButton dashboardBtn = new JButton("Back to Dashboard");
        dashboardBtn.setPreferredSize(new Dimension(180, 40));
        dashboardBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        dashboardBtn.addActionListener(e -> {
            viewManager.showView("CUSTOMER_DASHBOARD", new CustomerDashboardView(viewManager));
        });
        panel.add(dashboardBtn);
        
        return panel;
    }
    
    /**
     * Add an info row to a panel.
     */
    private void addInfoRow(JPanel panel, String label, String value) {
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(labelComp);
        
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(valueComp);
    }
    
    /**
     * Generate a confirmation number based on reservation ID.
     */
    private String generateConfirmationNumber() {
        // Format: ABC-123456
        int resId = reservation.getReservationId();
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        
        // Generate 3 letters based on hash
        int hash = (resId * 31 + 17) % (26 * 26 * 26);
        char c1 = letters.charAt(hash % 26);
        char c2 = letters.charAt((hash / 26) % 26);
        char c3 = letters.charAt((hash / 676) % 26);
        
        // Pad reservation ID to 6 digits
        String numPart = String.format("%06d", resId % 1000000);
        
        return "" + c1 + c2 + c3 + "-" + numPart;
    }
    
    /**
     * Format payment method for display.
     */
    private String formatPaymentMethod(String method) {
        if (method == null) return "N/A";
        
        switch (method) {
            case "CREDIT_CARD":
                return "Credit Card";
            case "DEBIT_CARD":
                return "Debit Card";
            case "PAYPAL":
                return "PayPal";
            case "BANK_TRANSFER":
                return "Bank Transfer";
            default:
                return method;
        }
    }
    
    /**
     * Print the confirmation (opens print dialog).
     */
    private void printConfirmation() {
        try {
            // Create a printable text version
            StringBuilder sb = new StringBuilder();
            sb.append("=== FLIGHT BOOKING CONFIRMATION ===\n\n");
            sb.append("Confirmation Number: ").append(generateConfirmationNumber()).append("\n");
            sb.append("Reservation ID: ").append(reservation.getReservationId()).append("\n");
            sb.append("Booking Date: ").append(reservation.getBookingDate() != null 
                ? reservation.getBookingDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                : "N/A").append("\n\n");
            
            Flight flight = reservation.getFlight();
            if (flight != null) {
                sb.append("--- Flight Information ---\n");
                sb.append("Flight: ").append(flight.getFlightNumber()).append("\n");
                if (flight.getRoute() != null) {
                    String origin = flight.getRoute().getOrigin() != null 
                        ? flight.getRoute().getOrigin().getAirportCode() : "N/A";
                    String destination = flight.getRoute().getDestination() != null 
                        ? flight.getRoute().getDestination().getAirportCode() : "N/A";
                    sb.append("Route: ").append(origin).append(" → ").append(destination).append("\n");
                }
                if (flight.getDepartureTime() != null) {
                    sb.append("Departure: ").append(flight.getDepartureTime()
                        .format(DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy HH:mm"))).append("\n");
                }
                sb.append("\n");
            }
            
            sb.append("--- Payment ---\n");
            sb.append("Total: $").append(String.format("%.2f", reservation.getTotalPrice())).append("\n\n");
            
            sb.append("Thank you for booking with us!\n");
            sb.append("(This is a simulated booking confirmation)");
            
            // Create a text area with the content and print it
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            
            try {
                textArea.print();
            } catch (PrinterException pe) {
                JOptionPane.showMessageDialog(this,
                    "Unable to print. Error: " + pe.getMessage(),
                    "Print Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error preparing print content: " + e.getMessage(),
                "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}