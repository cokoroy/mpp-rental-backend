package com.mpp.rental.model;

/**
 * Predefined business categories
 * Users can also enter custom categories (free text)
 */
public enum BusinessCategory {
    FOOD_AND_BEVERAGE("Food & Beverage"),
    CLOTHING_AND_FASHION("Clothing & Fashion"),
    ELECTRONICS("Electronics"),
    BOOKS_AND_STATIONERY("Books & Stationery"),
    ACCESSORIES("Accessories"),
    HANDICRAFTS("Handicrafts"),
    SERVICES("Services"),
    HEALTH_AND_BEAUTY("Health & Beauty"),
    SPORTS_AND_FITNESS("Sports & Fitness"),
    OTHERS("Others");

    private final String displayName;

    BusinessCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get all category names for dropdown
     */
    public static String[] getAllCategories() {
        BusinessCategory[] values = BusinessCategory.values();
        String[] categories = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            categories[i] = values[i].getDisplayName();
        }
        return categories;
    }
}
