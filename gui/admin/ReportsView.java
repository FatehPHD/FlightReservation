package gui.admin;

import gui.common.ViewManager;
import gui.common.ErrorDialog;
import businesslogic.services.AdminService;
import businesslogic.entities.*;
import businesslogic.entities.enums.FlightStatus;
import businesslogic.entities.enums.ReservationStatus;
import businesslogic.entities.enums.PaymentStatus;
import businesslogic.entities.enums.PaymentMethod;
import businesslogic.entities.enums.MembershipStatus;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * System reports and statistics.
 * Displays analytics including flight performance, revenue, booking trends, etc.
 * All data is retrieved through AdminService - no direct database access.
 */
public class ReportsView extends JPanel {
    
    private ViewManager viewManager;
    private AdminService adminService;
    private JTextArea reportTextArea;
    
    public ReportsView(ViewManager viewManager) {
        this.viewManager = viewManager;
        this.adminService = viewManager.getAdminService();
        initComponents();
        generateReports();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Title
        JLabel title = new JLabel("System Reports & Analytics");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(10));
        
        // Buttons panel (centered)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        
        JButton refreshBtn = new JButton("Refresh Reports");
        refreshBtn.setPreferredSize(new Dimension(150, 35));
        refreshBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        refreshBtn.addActionListener(e -> generateReports());
        
        JButton backBtn = new JButton("Back");
        backBtn.setPreferredSize(new Dimension(100, 35));
        backBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        backBtn.addActionListener(e -> {
            viewManager.showView("ADMIN_DASHBOARD", 
                new AdminDashboardView(viewManager));
        });
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(backBtn);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        titlePanel.add(buttonPanel);
        add(titlePanel, BorderLayout.NORTH);
        
        // Report display area
        reportTextArea = new JTextArea();
        reportTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportTextArea.setEditable(false);
        reportTextArea.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(reportTextArea);
        scrollPane.setPreferredSize(new Dimension(1000, 500));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Reports"));
        add(scrollPane, BorderLayout.CENTER);
        
    }
    
    private void generateReports() {
        try {
            StringBuilder report = new StringBuilder();
            report.append("FLIGHT RESERVATION SYSTEM - REPORTS\n");
            report.append("Generated: ").append(LocalDateTime.now()).append("\n\n");
            
            List<Flight> flights = adminService.getAllFlights();
            List<Reservation> reservations = adminService.getAllReservations();
            List<Payment> payments = adminService.getAllPayments();
            List<Customer> customers = adminService.getAllCustomers();
            List<Route> routes = adminService.getAllRoutes();
            List<Aircraft> aircraft = adminService.getAllAircraft();
            List<Airline> airlines = adminService.getAllAirlines();
            List<Airport> airports = adminService.getAllAirports();
            
            report.append("OPERATIONAL STATISTICS\n");
            report.append("----------------------\n");
            report.append("Total Flights:      ").append(flights.size()).append("\n");
            report.append("Total Routes:       ").append(routes.size()).append("\n");
            report.append("Total Aircraft:     ").append(aircraft.size()).append("\n");
            report.append("Total Airlines:     ").append(airlines.size()).append("\n");
            report.append("Total Airports:     ").append(airports.size()).append("\n");
            report.append("Total Customers:    ").append(customers.size()).append("\n");
            report.append("Total Reservations:  ").append(reservations.size()).append("\n");
            report.append("Total Payments:      ").append(payments.size()).append("\n\n");
            
            report.append("FLIGHT PERFORMANCE\n");
            report.append("------------------\n");
            Map<FlightStatus, Long> flightStatusCounts = flights.stream()
                .collect(Collectors.groupingBy(Flight::getStatus, Collectors.counting()));
            
            long scheduled = flightStatusCounts.getOrDefault(FlightStatus.SCHEDULED, 0L);
            long delayed = flightStatusCounts.getOrDefault(FlightStatus.DELAYED, 0L);
            long cancelled = flightStatusCounts.getOrDefault(FlightStatus.CANCELLED, 0L);
            long departed = flightStatusCounts.getOrDefault(FlightStatus.DEPARTED, 0L);
            long arrived = flightStatusCounts.getOrDefault(FlightStatus.ARRIVED, 0L);
            
            double totalFlights = flights.size();
            report.append("Scheduled:  ").append(scheduled).append(" (").append(formatPercent(scheduled, totalFlights)).append(")\n");
            report.append("Delayed:    ").append(delayed).append(" (").append(formatPercent(delayed, totalFlights)).append(")\n");
            report.append("Cancelled:  ").append(cancelled).append(" (").append(formatPercent(cancelled, totalFlights)).append(")\n");
            report.append("Departed:    ").append(departed).append(" (").append(formatPercent(departed, totalFlights)).append(")\n");
            report.append("Arrived:     ").append(arrived).append(" (").append(formatPercent(arrived, totalFlights)).append(")\n\n");
            
            report.append("REVENUE SUMMARY\n");
            report.append("---------------\n");
            
            double totalRevenue = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .mapToDouble(Payment::getAmount)
                .sum();
            
            double pendingRevenue = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .mapToDouble(Payment::getAmount)
                .sum();
            
            double refundedAmount = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.REFUNDED)
                .mapToDouble(Payment::getAmount)
                .sum();
            
            Map<PaymentMethod, Double> revenueByMethod = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .collect(Collectors.groupingBy(
                    Payment::getPaymentMethod,
                    Collectors.summingDouble(Payment::getAmount)
                ));
            
            report.append("Total Revenue:    $").append(String.format("%.2f", totalRevenue)).append("\n");
            report.append("Pending Payments: $").append(String.format("%.2f", pendingRevenue)).append("\n");
            report.append("Refunded:         $").append(String.format("%.2f", refundedAmount)).append("\n");
            report.append("Total Payments:   ").append(payments.size()).append("\n\n");
            
            if (!revenueByMethod.isEmpty()) {
                report.append("Revenue by Payment Method:\n");
                revenueByMethod.forEach((method, amount) -> 
                    report.append("  ").append(method).append(": $").append(String.format("%.2f", amount)).append("\n"));
                report.append("\n");
            }
            
            report.append("BOOKING TRENDS\n");
            report.append("--------------\n");
            
            Map<ReservationStatus, Long> reservationStatusCounts = reservations.stream()
                .collect(Collectors.groupingBy(Reservation::getStatus, Collectors.counting()));
            
            long pendingRes = reservationStatusCounts.getOrDefault(ReservationStatus.PENDING, 0L);
            long confirmedRes = reservationStatusCounts.getOrDefault(ReservationStatus.CONFIRMED, 0L);
            long cancelledRes = reservationStatusCounts.getOrDefault(ReservationStatus.CANCELLED, 0L);
            long completedRes = reservationStatusCounts.getOrDefault(ReservationStatus.COMPLETED, 0L);
            
            double totalReservations = reservations.size();
            report.append("Pending:   ").append(pendingRes).append(" (").append(formatPercent(pendingRes, totalReservations)).append(")\n");
            report.append("Confirmed: ").append(confirmedRes).append(" (").append(formatPercent(confirmedRes, totalReservations)).append(")\n");
            report.append("Cancelled: ").append(cancelledRes).append(" (").append(formatPercent(cancelledRes, totalReservations)).append(")\n");
            report.append("Completed: ").append(completedRes).append(" (").append(formatPercent(completedRes, totalReservations)).append(")\n\n");
            
            report.append("ROUTE UTILIZATION (Top 10)\n");
            report.append("--------------------------\n");
            
            Map<String, Long> routeUsage = reservations.stream()
                .filter(r -> r.getFlight() != null && r.getFlight().getRoute() != null)
                .collect(Collectors.groupingBy(
                    r -> {
                        Route route = r.getFlight().getRoute();
                        String origin = route.getOrigin() != null ? route.getOrigin().getAirportCode() : "N/A";
                        String dest = route.getDestination() != null ? route.getDestination().getAirportCode() : "N/A";
                        return origin + " -> " + dest;
                    },
                    Collectors.counting()
                ));
            
            routeUsage.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> 
                    report.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" bookings\n"));
            report.append("\n");
            
            report.append("CUSTOMER ACTIVITY\n");
            report.append("-----------------\n");
            
            Map<MembershipStatus, Long> membershipCounts = customers.stream()
                .filter(c -> c != null && c.getMembershipStatus() != null)
                .collect(Collectors.groupingBy(
                    Customer::getMembershipStatus,
                    Collectors.counting()
                ));
            
            report.append("Regular:   ").append(membershipCounts.getOrDefault(MembershipStatus.REGULAR, 0L)).append("\n");
            report.append("Silver:    ").append(membershipCounts.getOrDefault(MembershipStatus.SILVER, 0L)).append("\n");
            report.append("Gold:      ").append(membershipCounts.getOrDefault(MembershipStatus.GOLD, 0L)).append("\n");
            report.append("Platinum:  ").append(membershipCounts.getOrDefault(MembershipStatus.PLATINUM, 0L)).append("\n\n");
            
            report.append("AIRCRAFT STATUS\n");
            report.append("---------------\n");
            
            long activeAircraft = aircraft.stream()
                .filter(a -> "ACTIVE".equals(a.getStatus()))
                .count();
            long inactiveAircraft = aircraft.stream()
                .filter(a -> "INACTIVE".equals(a.getStatus()))
                .count();
            long maintenanceAircraft = aircraft.stream()
                .filter(a -> "MAINTENANCE".equals(a.getStatus()))
                .count();
            
            double totalAircraft = aircraft.size();
            report.append("Active:        ").append(activeAircraft).append(" (").append(formatPercent(activeAircraft, totalAircraft)).append(")\n");
            report.append("Inactive:      ").append(inactiveAircraft).append(" (").append(formatPercent(inactiveAircraft, totalAircraft)).append(")\n");
            report.append("Maintenance:   ").append(maintenanceAircraft).append(" (").append(formatPercent(maintenanceAircraft, totalAircraft)).append(")\n\n");
            
            report.append("PAYMENT STATUS\n");
            report.append("--------------\n");
            
            Map<PaymentStatus, Long> paymentStatusCounts = payments.stream()
                .collect(Collectors.groupingBy(Payment::getStatus, Collectors.counting()));
            
            double totalPayments = payments.size();
            paymentStatusCounts.forEach((status, count) -> 
                report.append(status).append(": ").append(count).append(" (").append(formatPercent(count, totalPayments)).append(")\n"));
            
            reportTextArea.setText(report.toString());
            reportTextArea.setCaretPosition(0);
            
        } catch (SQLException e) {
            ErrorDialog.show(this, "Error generating reports: " + e.getMessage(), e);
        }
    }
    
    private String formatPercent(long value, double total) {
        if (total == 0) return "0.0%";
        return String.format("%.1f%%", (value * 100.0 / total));
    }
}
