package datalayer.impl;

import businesslogic.entities.User;
import businesslogic.entities.Customer;
import businesslogic.entities.FlightAgent;
import businesslogic.entities.SystemAdmin;
import businesslogic.entities.enums.MembershipStatus;
import businesslogic.entities.enums.SystemAdminPermission;
import businesslogic.entities.enums.UserRole;
import datalayer.dao.UserDAO;
import datalayer.database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    private static final String INSERT_SQL =
            "INSERT INTO users (" +
                    "username, password_hash, email, role, " +
                    "first_name, last_name, phone, address, date_of_birth, membership_status, " +
                    "employee_id, hire_date, department, " +
                    "admin_level" +
            ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String SELECT_BY_ID_SQL =
            "SELECT * FROM users WHERE user_id = ?";

    private static final String SELECT_BY_USERNAME_SQL =
            "SELECT * FROM users WHERE username = ?";

    private static final String SELECT_ALL_SQL =
            "SELECT * FROM users";

    private static final String SELECT_BY_ROLE_SQL =
            "SELECT * FROM users WHERE role = ?";

    private static final String UPDATE_SQL =
            "UPDATE users SET " +
                    "username = ?, password_hash = ?, email = ?, role = ?, " +
                    "first_name = ?, last_name = ?, phone = ?, address = ?, " +
                    "date_of_birth = ?, membership_status = ?, " +
                    "employee_id = ?, hire_date = ?, department = ?, " +
                    "admin_level = ? " +
            "WHERE user_id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM users WHERE user_id = ?";

    // System admin permissions table
    private static final String INSERT_PERMISSION_SQL =
            "INSERT INTO system_admin_permissions (user_id, permission) VALUES (?, ?)";

    private static final String DELETE_PERMISSIONS_BY_USER_SQL =
            "DELETE FROM system_admin_permissions WHERE user_id = ?";

    private static final String SELECT_PERMISSIONS_BY_USER_SQL =
            "SELECT permission FROM system_admin_permissions WHERE user_id = ?";

    @Override
    public User save(User user) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(
                INSERT_SQL, Statement.RETURN_GENERATED_KEYS
        )) {
            setCommonParams(stmt, user);
            setSubclassParams(stmt, user);

            int affected = stmt.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Saving user failed, no rows affected.");
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setUserId(keys.getInt(1));
                }
            }
        }

        // After user row is inserted, handle system admin permissions
        if (user instanceof SystemAdmin) {
            saveSystemAdminPermissions((SystemAdmin) user);
        }

        return user;
    }

    @Override
    public User findById(Integer id) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        }

        return null;
    }

    @Override
    public User findByUsername(String username) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USERNAME_SQL)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        }

        return null;
    }

    @Override
    public List<User> findAll() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<User> users = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {

            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        }

        return users;
    }

    @Override
    public boolean update(User user) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            setCommonParams(stmt, user);
            setSubclassParams(stmt, user);

            // WHERE user_id = ?
            stmt.setInt(15, user.getUserId());

            int affected = stmt.executeUpdate();

            // Update system admin permissions too (overwrite)
            if (user instanceof SystemAdmin) {
                overwriteSystemAdminPermissions((SystemAdmin) user);
            }

            return affected > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        // Permissions will be removed automatically by ON DELETE CASCADE
        try (PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);

            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    @Override
    public List<Customer> findAllCustomers() throws SQLException {
        return findByRole(UserRole.CUSTOMER, Customer.class);
    }

    @Override
    public List<FlightAgent> findAllFlightAgents() throws SQLException {
        return findByRole(UserRole.FLIGHT_AGENT, FlightAgent.class);
    }

    @Override
    public List<SystemAdmin> findAllSystemAdmins() throws SQLException {
        return findByRole(UserRole.SYSTEM_ADMIN, SystemAdmin.class);
    }

    // ===== Helpers =====

    // Params 1–4: common to all users
    private void setCommonParams(PreparedStatement stmt, User user) throws SQLException {
        // 1: username
        stmt.setString(1, user.getUsername());

        // 2: password_hash  (mapped from User.password)
        stmt.setString(2, user.getPassword());

        // 3: email
        stmt.setString(3, user.getEmail());

        // 4: role
        UserRole role = user.getRole();
        stmt.setString(4, role != null ? role.name() : null);
    }

    // Params 5–14: subclass-specific
    private void setSubclassParams(PreparedStatement stmt, User user) throws SQLException {
        // Default all subclass fields to NULL
        for (int i = 5; i <= 14; i++) {
            stmt.setObject(i, null);
        }

        if (user instanceof Customer) {
            Customer c = (Customer) user;

            // 5: first_name
            stmt.setString(5, c.getFirstName());
            // 6: last_name
            stmt.setString(6, c.getLastName());
            // 7: phone
            stmt.setString(7, c.getPhone());
            // 8: address
            stmt.setString(8, c.getAddress());

            // 9: date_of_birth
            LocalDate dob = c.getDateOfBirth();
            if (dob != null) {
                stmt.setDate(9, Date.valueOf(dob));
            } else {
                stmt.setNull(9, Types.DATE);
            }

            // 10: membership_status
            MembershipStatus ms = c.getMembershipStatus();
            if (ms != null) {
                stmt.setString(10, ms.name());
            } else {
                stmt.setNull(10, Types.VARCHAR);
            }

        } else if (user instanceof FlightAgent) {
            FlightAgent fa = (FlightAgent) user;

            // 11: employee_id
            stmt.setString(11, fa.getEmployeeId());

            // 12: hire_date
            LocalDate hd = fa.getHireDate();
            if (hd != null) {
                stmt.setDate(12, Date.valueOf(hd));
            } else {
                stmt.setNull(12, Types.DATE);
            }

            // 13: department
            stmt.setString(13, fa.getDepartment());

        } else if (user instanceof SystemAdmin) {
            SystemAdmin sa = (SystemAdmin) user;

            // 14: admin_level
            stmt.setInt(14, sa.getAdminLevel());
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        String roleStr = rs.getString("role");
        UserRole role = roleStr != null ? UserRole.valueOf(roleStr) : null;

        User user;
        if (role == UserRole.CUSTOMER) {
            user = new Customer();
        } else if (role == UserRole.FLIGHT_AGENT) {
            user = new FlightAgent();
        } else if (role == UserRole.SYSTEM_ADMIN) {
            user = new SystemAdmin();
        } else {
            // default if somehow null/unknown
            user = new Customer();
        }

        // Common fields
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password_hash"));
        user.setEmail(rs.getString("email"));
        user.setRole(role);

        // Customer fields
        if (user instanceof Customer) {
            Customer c = (Customer) user;

            c.setFirstName(rs.getString("first_name"));
            c.setLastName(rs.getString("last_name"));
            c.setPhone(rs.getString("phone"));
            c.setAddress(rs.getString("address"));

            Date dob = rs.getDate("date_of_birth");
            if (dob != null) {
                c.setDateOfBirth(dob.toLocalDate());
            }

            String msStr = rs.getString("membership_status");
            if (msStr != null) {
                c.setMembershipStatus(MembershipStatus.valueOf(msStr));
            }
        }

        // FlightAgent fields
        if (user instanceof FlightAgent) {
            FlightAgent fa = (FlightAgent) user;

            fa.setEmployeeId(rs.getString("employee_id"));

            Date hd = rs.getDate("hire_date");
            if (hd != null) {
                fa.setHireDate(hd.toLocalDate());
            }

            fa.setDepartment(rs.getString("department"));
        }

        // SystemAdmin fields + permissions
        if (user instanceof SystemAdmin) {
            SystemAdmin sa = (SystemAdmin) user;

            sa.setAdminLevel(rs.getInt("admin_level"));
            sa.setPermissions(loadSystemAdminPermissions(sa.getUserId()));
        }

        return user;
    }

    private <T extends User> List<T> findByRole(UserRole role, Class<T> clazz) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        List<T> result = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ROLE_SQL)) {
            stmt.setString(1, role.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User u = mapRowToUser(rs);
                    if (clazz.isInstance(u)) {
                        result.add(clazz.cast(u));
                    }
                }
            }
        }

        return result;
    }

    // ===== SystemAdmin permissions helpers =====

    private void saveSystemAdminPermissions(SystemAdmin sa) throws SQLException {
        // For a fresh save, there should be no existing rows, but this makes it safe to reuse.
        overwriteSystemAdminPermissions(sa);
    }

    private void overwriteSystemAdminPermissions(SystemAdmin sa) throws SQLException {
        // Delete existing
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement deleteStmt =
                     conn.prepareStatement(DELETE_PERMISSIONS_BY_USER_SQL)) {
            deleteStmt.setInt(1, sa.getUserId());
            deleteStmt.executeUpdate();
        }

        // Insert current permissions
        if (sa.getPermissions() == null || sa.getPermissions().isEmpty()) {
            return;
        }

        try (PreparedStatement insertStmt =
                     conn.prepareStatement(INSERT_PERMISSION_SQL)) {

            for (SystemAdminPermission perm : sa.getPermissions()) {
                insertStmt.setInt(1, sa.getUserId());
                insertStmt.setString(2, perm.name());
                insertStmt.addBatch();
            }

            insertStmt.executeBatch();
        }
    }

    private EnumSet<SystemAdminPermission> loadSystemAdminPermissions(int userId) throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        EnumSet<SystemAdminPermission> result =
                EnumSet.noneOf(SystemAdminPermission.class);

        try (PreparedStatement stmt =
                     conn.prepareStatement(SELECT_PERMISSIONS_BY_USER_SQL)) {
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String permStr = rs.getString("permission");
                    if (permStr != null) {
                        SystemAdminPermission perm =
                                SystemAdminPermission.valueOf(permStr);
                        result.add(perm);
                    }
                }
            }
        }

        return result;
    }
}
