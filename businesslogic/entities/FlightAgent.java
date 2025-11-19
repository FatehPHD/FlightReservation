package businesslogic.entities;

import java.time.LocalDate;

import businesslogic.entities.enums.UserRole;

public class FlightAgent extends User {

    private String employeeId;
    private LocalDate hireDate;
    private String department;

    public FlightAgent() {
    }

    public FlightAgent(int userId,
                       String username,
                       String password,
                       String email,
                       UserRole role,
                       String employeeId,
                       LocalDate hireDate,
                       String department) {

        super(userId, username, password, email, role);
        this.employeeId = employeeId;
        this.hireDate = hireDate;
        this.department = department;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "FlightAgent{" +
               "userId=" + getUserId() +
               ", username='" + getUsername() + '\'' +
               ", employeeId='" + employeeId + '\'' +
               ", department='" + department + '\'' +
               '}';
    }
}
