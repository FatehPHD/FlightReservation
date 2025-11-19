package tests;

import businesslogic.entities.Airline;
import datalayer.dao.AirlineDAO;
import datalayer.impl.AirlineDAOImpl;

public class TestAirlineDAO {

    public static void main(String[] args) throws Exception {

        AirlineDAO dao = new AirlineDAOImpl();

        // Use a unique code each run to avoid UNIQUE constraint conflicts
        String uniqueCode = "TA" + (System.currentTimeMillis() % 100000);

        // 1. CREATE
        Airline a = new Airline(0, "Test Airline", uniqueCode, "Canada");
        a = dao.save(a); // save now returns Airline and can throw SQLException

        System.out.println("Saved airline with ID: " + a.getAirlineId() + " and code: " + uniqueCode);

        // If save failed, id will still be 0 (assuming 0 means "not set")
        if (a.getAirlineId() == 0) {
            System.out.println("Save seems to have failed (ID is 0). Stopping test.");
            return;
        }

        // 2. READ by ID
        Airline found = dao.findById(a.getAirlineId());
        if (found == null) {
            System.out.println("findById returned null. Stopping test.");
            return;
        }
        System.out.println("Found airline: " + found.getName() + " (" + found.getCode() + ")");

        // 3. READ all
        System.out.println("All airlines:");
        for (Airline air : dao.findAll()) {
            System.out.println(" - " + air.getName() + " (" + air.getCode() + ")");
        }

        // 4. UPDATE
        found.setName("Updated Airline");
        boolean updateOk = dao.update(found);

        System.out.println("Update result: " + updateOk);
        Airline updated = dao.findById(found.getAirlineId());
        if (updated != null) {
            System.out.println("After update, airline name is: " + updated.getName());
        } else {
            System.out.println("After update, airline not found.");
        }

        // 5. DELETE
        boolean deleteOk = dao.delete(found.getAirlineId());
        System.out.println("Delete result: " + deleteOk);

        Airline deleted = dao.findById(found.getAirlineId());
        if (deleted == null) {
            System.out.println("Confirmed: airline was deleted.");
        } else {
            System.out.println("Airline still exists after delete!");
        }
    }
}
