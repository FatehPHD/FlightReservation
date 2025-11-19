package businesslogic.entities;

import java.util.EnumSet;
import java.util.Set;

import businesslogic.entities.enums.SystemAdminPermission;
import businesslogic.entities.enums.UserRole;

public class SystemAdmin extends User {

    private int adminLevel;
    private EnumSet<SystemAdminPermission> permissions;

    public SystemAdmin() {
        // By default, no permissions
        this.permissions = EnumSet.noneOf(SystemAdminPermission.class);
    }

    public SystemAdmin(int userId,
                       String username,
                       String password,
                       String email,
                       UserRole role,
                       int adminLevel,
                       EnumSet<SystemAdminPermission> permissions) {

        super(userId, username, password, email, role);
        this.adminLevel = adminLevel;
        // Avoid null – always keep a valid EnumSet
        this.permissions = (permissions != null)
                ? EnumSet.copyOf(permissions)
                : EnumSet.noneOf(SystemAdminPermission.class);
    }

    // Optional convenience constructor (varargs)
    public SystemAdmin(int userId,
                       String username,
                       String password,
                       String email,
                       UserRole role,
                       int adminLevel,
                       SystemAdminPermission... permissions) {

        super(userId, username, password, email, role);
        this.adminLevel = adminLevel;
        this.permissions = EnumSet.noneOf(SystemAdminPermission.class);
        if (permissions != null) {
            for (SystemAdminPermission p : permissions) {
                this.permissions.add(p);
            }
        }
    }

    public int getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(int adminLevel) {
        this.adminLevel = adminLevel;
    }

    public EnumSet<SystemAdminPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<SystemAdminPermission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            this.permissions = EnumSet.noneOf(SystemAdminPermission.class);
        } else {
            this.permissions = EnumSet.copyOf(permissions);
        }
    }

    // Add a single permission
    public void addPermission(SystemAdminPermission permission) {
        if (this.permissions == null) {
            this.permissions = EnumSet.noneOf(SystemAdminPermission.class);
        }
        this.permissions.add(permission);
    }

    // Remove a single permission
    public void removePermission(SystemAdminPermission permission) {
        if (this.permissions != null) {
            this.permissions.remove(permission);
        }
    }

    // Does this admin logically have ALL permissions?
    public boolean hasAllPermissions() {
        return permissions != null && permissions.contains(SystemAdminPermission.ALL);
    }

    // Check a specific permission, respecting ALL
    public boolean hasPermission(SystemAdminPermission permission) {
        if (hasAllPermissions()) {
            return true;
        }
        return permissions != null && permissions.contains(permission);
    }

    @Override
    public String toString() {
        return "SystemAdmin{" +
               "userId=" + getUserId() +
               ", username='" + getUsername() + '\'' +
               ", adminLevel=" + adminLevel +
               ", permissions=" + permissions +
               '}';
    }
}
