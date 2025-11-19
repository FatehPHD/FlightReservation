package businesslogic.entities;

import java.time.LocalDate;
import java.util.List;

public class Promotion {

    private int promotionId;
    private String title;
    private String description;
    private double discountPercent;
    private LocalDate validFrom;
    private LocalDate validTo;

    private List<Route> applicableRoutes;

    public Promotion(int promotionId, String title, String description,
                     double discountPercent, LocalDate validFrom,
                     LocalDate validTo, List<Route> applicableRoutes) {
        this.promotionId = promotionId;
        this.title = title;
        this.description = description;
        this.discountPercent = discountPercent;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.applicableRoutes = applicableRoutes;
    }

    public int getPromotionId() {
        return promotionId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public List<Route> getApplicableRoutes() {
        return applicableRoutes;
    }
}
