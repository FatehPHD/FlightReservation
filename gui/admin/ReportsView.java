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
        JLabel title = new JLabel("System Reports & Analytics");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(title);
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
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton refreshBtn = new JButton("Refresh Reports");
        refreshBtn.setPreferredSize(new Dimension(150, 35));
        refreshBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        refreshBtn.addActionListener(e -> generateReports());
        
        JButton backBtn = new JButton("Back");
        backBtn.setPreferredSize(new Dimension(120, 35));
        backBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        backBtn.addActionListener(e -> {
            viewManager.showView("ADMIN_DASHBOARD", 
                new AdminDashboardView(viewManager));
        });
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(backBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Generate and display all reports using AdminService.
     */
    private void generateReports() {
        try {
            StringBuilder report = new StringBuilder();
            report.append("═══════════════════════════════════════════════════════════════\n");
            report.append("           FLIGHT RESERVATION SYSTEM - REPORTS\n");
            report.append("═══════════════════════════════════════════════════════════════\n\n");
            
            // Load all data through AdminService
            List<Flight> flights = adminService.getAllFlights();
            List<Reservation> reservations = adminService.getAllReservations();
            List<Payment> payments = adminService.getAllPayments();
            List<Customer> customers = adminService.getAllCustomers();
            List<Route> routes = adminService.getAllRoutes();
            List<Aircraft> aircraft = adminService.getAllAircraft();
            List<Airline> airlines = adminService.getAllAirlines();
            List<Airport> airports = adminService.getAllAirports();
            
            // 1. Operational Statistics
            report.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            report.append("OPERATIONAL STATISTICS\n");
            report.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            report.append(String.format("Total Flights:        %d\n", flights.size()));
            report.append(String.format("Total Routes:         %d\n", routes.size()));
            report.append(String.format("Total Aircraft:       %d\n", aircraft.size()));
            report.append(String.format("Total Airlines:       %d\n", airlines.size()));
            report.append(String.format("Total Airports:       %d\n", airports.size()));
            report.append(String.format("Total Customers:      %d\n", customers.size()));
            report.append(String.format("Total Reservations:   %d\n", reservations.size()));
            report.append(String.format("Total Payments:       %d\n\n", payments.size()));
            
            // 2. Flight Performance
            report.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            report.append("FLIGHT PERFORMANCE\n");
            report.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            Map<FlightStatus, Long> flightStatusCounts = flights.stream()
                .collect(Collectors.groupingBy(Flight::getStatus, Collectors.counting()));
            
            long scheduled = flightStatusCounts.getOrDefault(FlightStatus.SCHEDULED, 0L);
            long delayed = flightStatusCounts.getOrDefault(FlightStatus.DELAYED, 0L);
            long cancelled = flightStatusCounts.getOrDefault(FlightStatus.CANCELLED, 0L);
            long departed = flightStatusCounts.getOrDefault(FlightStatus.DEPARTED, 0L);
            long arrived = flightStatusCounts.getOrDefault(FlightStatus.ARRIVED, 0L);
            
            report.append(String.format("Scheduled:            %d (%.1f%%)\n", 
                scheduled, flights.isEmpty() ? 0 : (scheduled * 100.0 / flights.size())));
            report.append(String.format("Delayed:              %d (%.1f%%)\n", 
                delayed, flights.isEmpty() ? 0 : (delayed * 100.0 / flights.size())));
            report.append(String.format("Cancelled:            %d (%.1f%%)\n", 
                cancelled, flights.isEmpty() ? 0 : (cancelled * 100.0 / flights.size())));
            report.append(String.format("Departed:             %d (%.1f%%)\n", 
                departed, flights.isEmpty() ? 0 : (departed * 100.0 / flights.size())));
            report.append(String.format("Arrived:              %d (%.1f%%)\n\n", 
                arrived, flights.isEmpty() ? 0 : (arrived * 100.0 / flights.size())));
            
            // 3. Revenue Summary
            report.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            report.append("REVENUE SUMMARY\n");
            report.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
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
            
            report.append(String.format("Total Revenue:        $%.2f\n", totalRevenue));
            report.append(String.format("Pending Payments:     $%.2f\n", pendingRevenue));
            report.append(String.format("Refunded Amount:      $%.2f\n", refundedAmount));
            report.append(String.format("Total Payments:       %d\n\n", payments.size()));
            
            report.append("Revenue by Payment Method:\n");
            revenueByMethod.forEach((method, amount) -> 
                report.append(String.format("  %-20s $%.2f\n", method + ":", amount)));
            report.append("\n");
            
            // 4. Booking Trends
            report.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            report.append("BOOKING TRENDS\n");
            report.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            Map<ReservationStatus, Long> reservationStatusCounts = reservations.stream()
                .collect(Collectors.groupingBy(Reservation::getStatus, Collectors.counting()));
            
            long pendingRes = reservationStatusCounts.getOrDefault(ReservationStatus.PENDING, 0L);
            long confirmedRes = reservationStatusCounts.getOrDefault(ReservationStatus.CONFIRMED, 0L);
            long cancelledRes = reservationStatusCounts.getOrDefault(ReservationStatus.CANCELLED, 0L);
            long completedRes = reservationStatusCounts.getOrDefault(ReservationStatus.COMPLETED, 0L);
            
            report.append(String.format("Pending:              %d (%.1f%%)\n", 
                pendingRes, reservations.isEmpty() ? 0 : (pendingRes * 100.0 / reservations.size())));
            report.append(String.format("Confirmed:            %d (%.1f%%)\n", 
                confirmedRes, reservations.isEmpty() ? 0 : (confirmedRes * 100.0 / reservations.size())));
            report.append(String.format("Cancelled:            %d (%.1f%%)\n", 
                cancelledRes, reservations.isEmpty() ? 0 : (cancelledRes * 100.0 / reservations.size())));
            report.append(String.format("Completed:            %d (%.1f%%)\n\n", 
                completedRes, reservations.isEmpty() ? 0 : (completedRes * 100.0 / reservations.size())));
            
            // 5. Route Utilization
            report.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            report.append("ROUTE UTILIZATION (Top 10 Most Used Routes)\n");
            report.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            Map<String, Long> routeUsage = reservations.stream()
                .filter(r -> r.getFlight() != null && r.getFlight().getRoute() != null)
                .collect(Collectors.groupingBy(
                    r -> {
                        Route route = r.getFlight().getRoute();
                        String origin = route.getOrigin() != null ? route.getOrigin().getAirportCode() : "N/A";
                        String dest = route.getDestination() != null ? route.getDestination().getAirportCode() : "N/A";
                        return origin + " → " + dest;
                    },
                    Collectors.counting()
                ));
            
            routeUsage.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> 
                    report.append(String.format("  %-30s %d bookings\n", entry.getKey() + ":", entry.getValue())));
            report.append("\n");
            
            // 6. Customer Activity
            report.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            report.append("CUSTOMER ACTIVITY\n");
            report.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            Map<MembershipStatus, Long> membershipCounts = customers.stream()
                .filter(c -> c.getMembershipStatus() != null)
                .collect(Collectors.groupingBy(
                    Customer::getMembershipStatus,
                    Collectors.counting()
                ));
            
            long regular = membershipCounts.getOrDefault(MembershipStatus.REGULAR, 0L);
            long silver = membershipCounts.getOrDefault(MembershipStatus.SILVER, 0L);
            long gold = membershipCounts.getOrDefault(MembershipStatus.GOLD, 0L);
            long platinum = membershipCounts.getOrDefault(MembershipStatus.PLATINUM, 0L);
            
            report.append(String.format("Regular Members:      %d\n", regular));
            report.append(String.format("Silver Members:       %d\n", silver));
            report.append(String.format("Gold Members:         %d\n", gold));
            report.append(String.format("Platinum Members:     %d\n\n", platinum));
            
            // 7. Aircraft Status
            report.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            report.append("AIRCRAFT STATUS\n");
            report.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            long activeAircraft = aircraft.stream()
                .filter(a -> "ACTIVE".equals(a.getStatus()))
                .count();
            long inactiveAircraft = aircraft.stream()
                .filter(a -> "INACTIVE".equals(a.getStatus()))
                .count();
            long maintenanceAircraft = aircraft.stream()
                .filter(a -> "MAINTENANCE".equals(a.getStatus()))
                .count();
            
            report.append(String.format("Active:               %d (%.1f%%)\n", 
                activeAircraft, aircraft.isEmpty() ? 0 : (activeAircraft * 100.0 / aircraft.size())));
            report.append(String.format("Inactive:             %d (%.1f%%)\n", 
                inactiveAircraft, aircraft.isEmpty() ? 0 : (inactiveAircraft * 100.0 / aircraft.size())));
            report.append(String.format("In Maintenance:       %d (%.1f%%)\n\n", 
                maintenanceAircraft, aircraft.isEmpty() ? 0 : (maintenanceAircraft * 100.0 / aircraft.size())));
            
            // 8. Payment Status Breakdown
            report.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            report.append("PAYMENT STATUS BREAKDOWN\n");
            report.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            
            Map<PaymentStatus, Long> paymentStatusCounts = payments.stream()
                .collect(Collectors.groupingBy(Payment::getStatus, Collectors.counting()));
            
            paymentStatusCounts.forEach((status, count) -> 
                report.append(String.format("%-20s %d (%.1f%%)\n", 
                    status + ":", count, 
                    payments.isEmpty() ? 0 : (count * 100.0 / payments.size()))));
            report.append("\n");
            
            report.append("═══════════════════════════════════════════════════════════════\n");
            report.append(String.format("Report Generated: %s\n", LocalDateTime.now().toString()));
            report.append("═══════════════════════════════════════════════════════════════\n");
            
            reportTextArea.setText(report.toString());
            reportTextArea.setCaretPosition(0); // Scroll to top
            
        } catch (SQLException e) {
            ErrorDialog.show(this, "Error generating reports: " + e.getMessage(), e);
        }
    }
}
