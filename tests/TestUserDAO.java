package tests;

import businesslogic.entities.Customer;
import businesslogic.entities.FlightAgent;
import businesslogic.entities.SystemAdmin;
import businesslogic.entities.User;
import businesslogic.entities.enums.MembershipStatus;
import businesslogic.entities.enums.SystemAdminPermission;
import businesslogic.entities.enums.UserRole;
import datalayer.dao.UserDAO;
import datalayer.impl.UserDAOImpl;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

public class TestUserDAO {

    public static void main(String[] args) {
        UserDAO userDAO = new UserDAOImpl();

        try {
            System.out.println("==== TEST: SAVE USERS ====");

            // ----- Create Customer -----
            Customer customer = new Customer();
            customer.setUsername("mike");
            customer.setPassword("password123");
            customer.setEmail("mike@example.com");
            customer.setRole(UserRole.CUSTOMER);
            customer.setFirstName("Michael");
            customer.setLastName("Tapang");
            customer.setPhone("555-1234");
            customer.setAddress("Calgary");
            customer.setDateOfBirth(LocalDate.of(2002, 1, 1));
            customer.setMembershipStatus(MembershipStatus.REGULAR);

            userDAO.save(customer);
            System.out.println("Saved customer with ID: " + customer.getUserId());

            // ----- Create FlightAgent -----
            FlightAgent agent = new FlightAgent();
            agent.setUsername("agent1");
            agent.setPassword("agentpass");
            agent.setEmail("agent1@example.com");
            agent.setRole(UserRole.FLIGHT_AGENT);
            agent.setEmployeeId("FA-001");
            agent.setHireDate(LocalDate.of(2024, 1, 15));
            agent.setDepartment("Sales");

            userDAO.save(agent);
            System.out.println("Saved flight agent with ID: " + agent.getUserId());

            // ----- Create SystemAdmin -----
            SystemAdmin admin = new SystemAdmin();
            admin.setUsername("admin1");
            admin.setPassword("adminpass");
            admin.setEmail("admin1@example.com");
            admin.setRole(UserRole.SYSTEM_ADMIN);
            admin.setAdminLevel(1);
            // ALL permission (means “has everything” in your logic)
            admin.setPermissions(EnumSet.of(SystemAdminPermission.ALL));

            userDAO.save(admin);
            System.out.println("Saved system admin with ID: " + admin.getUserId());

            // ================================
            System.out.println("\n==== TEST: FIND BY ID ====");
            User foundCustomer = userDAO.findById(customer.getUserId());
            User foundAgent = userDAO.findById(agent.getUserId());
            User foundAdmin = userDAO.findById(admin.getUserId());

            System.out.println("Found customer: " + foundCustomer);
            System.out.println("Found agent: " + foundAgent);
            System.out.println("Found admin: " + foundAdmin);

            // ================================
            System.out.println("\n==== TEST: FIND BY USERNAME ====");
            User byUsernameCustomer = userDAO.findByUsername("mike");
            User byUsernameAgent = userDAO.findByUsername("agent1");
            User byUsernameAdmin = userDAO.findByUsername("admin1");

            System.out.println("By username (mike): " + byUsernameCustomer);
            System.out.println("By username (agent1): " + byUsernameAgent);
            System.out.println("By username (admin1): " + byUsernameAdmin);

            // ================================
            System.out.println("\n==== TEST: UPDATE USERS ====");

            // Update customer
            Customer custToUpdate = (Customer) userDAO.findById(customer.getUserId());
            custToUpdate.setPhone("555-9999");
            custToUpdate.setMembershipStatus(MembershipStatus.GOLD);
            boolean custUpdated = userDAO.update(custToUpdate);
            System.out.println("Customer updated: " + custUpdated);

            // Update agent
            FlightAgent agentToUpdate = (FlightAgent) userDAO.findById(agent.getUserId());
            agentToUpdate.setDepartment("Customer Support");
            boolean agentUpdated = userDAO.update(agentToUpdate);
            System.out.println("Agent updated: " + agentUpdated);

            // Update admin
            SystemAdmin adminToUpdate = (SystemAdmin) userDAO.findById(admin.getUserId());
            adminToUpdate.setAdminLevel(2);
            adminToUpdate.setPermissions(
                    EnumSet.of(
                            SystemAdminPermission.MANAGE_USERS,
                            SystemAdminPermission.VIEW_REPORTS
                    )
            );
            boolean adminUpdated = userDAO.update(adminToUpdate);
            System.out.println("Admin updated: " + adminUpdated);

            // Check updates
            System.out.println("\nAfter updates:");
            System.out.println("Customer: " + userDAO.findById(customer.getUserId()));
            System.out.println("Agent: " + userDAO.findById(agent.getUserId()));
            System.out.println("Admin: " + userDAO.findById(admin.getUserId()));

            // ================================
            System.out.println("\n==== TEST: FIND ALL USERS ====");
            List<User> allUsers = userDAO.findAll();
            System.out.println("Total users in DB: " + allUsers.size());
            for (User u : allUsers) {
                System.out.println(u);
            }

            // ================================
            System.out.println("\n==== TEST: FIND BY ROLE HELPERS ====");
            List<Customer> customers = userDAO.findAllCustomers();
            List<FlightAgent> agents = userDAO.findAllFlightAgents();
            List<SystemAdmin> admins = userDAO.findAllSystemAdmins();

            System.out.println("Customers count: " + customers.size());
            for (Customer c : customers) {
                System.out.println("Customer: " + c);
            }

            System.out.println("Agents count: " + agents.size());
            for (FlightAgent fa : agents) {
                System.out.println("Agent: " + fa);
            }

            System.out.println("Admins count: " + admins.size());
            for (SystemAdmin sa : admins) {
                System.out.println("Admin: " + sa);
            }

            // ================================
            System.out.println("\n==== TEST: DELETE USERS ====");
            boolean custDeleted = userDAO.delete(customer.getUserId());
            boolean agentDeleted = userDAO.delete(agent.getUserId());
            boolean adminDeleted = userDAO.delete(admin.getUserId());

            System.out.println("Customer deleted: " + custDeleted);
            System.out.println("Agent deleted: " + agentDeleted);
            System.out.println("Admin deleted: " + adminDeleted);

            System.out.println("\nAfter delete, findById should return null:");
            System.out.println("Customer: " + userDAO.findById(customer.getUserId()));
            System.out.println("Agent: " + userDAO.findById(agent.getUserId()));
            System.out.println("Admin: " + userDAO.findById(admin.getUserId()));

            System.out.println("\n==== TESTS COMPLETED ====");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
