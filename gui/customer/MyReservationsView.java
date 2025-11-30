package gui.customer;

import gui.common.ViewManager;
import gui.common.ErrorDialog;
import businesslogic.entities.User;
import businesslogic.entities.Reservation;
import businesslogic.services.ReservationService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Displays user reservations. Works for all user types.
 * TODO: Add table view with cancel/modify functionality.
 */
public class MyReservationsView extends JPanel {
    
    private ViewManager viewManager;
    private User user;
    
    public MyReservationsView(ViewManager viewManager, User user) {
        this.viewManager = viewManager;
        this.user = user;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        JLabel title = new JLabel("My Reservations");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);
        
        ReservationService reservationService = viewManager.getReservationService();
        
        try {
            List<Reservation> reservations = reservationService.getUserReservations(user);
            
            if (reservations.isEmpty()) {
                JLabel noReservations = new JLabel("You have no reservations yet.");
                noReservations.setFont(new Font("Arial", Font.PLAIN, 16));
                noReservations.setHorizontalAlignment(SwingConstants.CENTER);
                noReservations.setBorder(BorderFactory.createEmptyBorder(50, 20, 20, 20));
                add(noReservations, BorderLayout.CENTER);
            } else {
                JLabel placeholder = new JLabel(
                    "<html><center>Found " + reservations.size() + " reservation(s).<br>" +
                    "Reservation table will be implemented here.</center></html>"
                );
                placeholder.setFont(new Font("Arial", Font.PLAIN, 14));
                placeholder.setHorizontalAlignment(SwingConstants.CENTER);
                placeholder.setBorder(BorderFactory.createEmptyBorder(50, 20, 20, 20));
                add(placeholder, BorderLayout.CENTER);
            }
            
            JPanel buttonPanel = new JPanel();
            JButton backBtn = new JButton("Back to Dashboard");
            backBtn.setPreferredSize(new Dimension(200, 35));
            backBtn.setFont(new Font("Arial", Font.PLAIN, 14));
            backBtn.addActionListener(e -> {
                viewManager.showView("CUSTOMER_DASHBOARD", 
                    new CustomerDashboardView(viewManager));
            });
            buttonPanel.add(backBtn);
            add(buttonPanel, BorderLayout.SOUTH);
            
        } catch (SQLException e) {
            ErrorDialog.show(this, "Error loading reservations: " + e.getMessage(), e);
        }
    }
}

