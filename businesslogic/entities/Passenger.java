package businesslogic.entities;

import java.time.LocalDate;

public class Passenger {

    private int passengerId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String passportNumber;
    private String nationality;

    public Passenger() {
    }

    public Passenger(int passengerId,
                     String firstName,
                     String lastName,
                     LocalDate dateOfBirth,
                     String passportNumber,
                     String nationality) {

        this.passengerId = passengerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.passportNumber = passportNumber;
        this.nationality = nationality;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(int passengerId) {
        this.passengerId = passengerId;
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    @Override
    public String toString() {
        return "Passenger{" +
               "passengerId=" + passengerId +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", passportNumber='" + passportNumber + '\'' +
               ", nationality='" + nationality + '\'' +
               '}';
    }
}
