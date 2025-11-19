package businesslogic.entities;

import java.time.LocalDate;

import businesslogic.entities.enums.MembershipStatus;
import businesslogic.entities.enums.UserRole;

public class Customer extends User {

    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private MembershipStatus membershipStatus;

    public Customer() {
    }

    public Customer(int userId,
                    String username,
                    String password,
                    String email,
                    UserRole role,
                    String firstName,
                    String lastName,
                    String phone,
                    String address,
                    LocalDate dateOfBirth,
                    MembershipStatus membershipStatus) {

        super(userId, username, password, email, role);
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.membershipStatus = membershipStatus;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public MembershipStatus getMembershipStatus() {
        return membershipStatus;
    }

    public void setMembershipStatus(MembershipStatus membershipStatus) {
        this.membershipStatus = membershipStatus;
    }

    @Override
    public String toString() {
        return "Customer{" +
               "userId=" + getUserId() +
               ", username='" + getUsername() + '\'' +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", membershipStatus=" + membershipStatus +
               '}';
    }
}
