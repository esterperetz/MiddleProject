package entities;

/**
 * Enumeration representing the different types of customers in the system.
 * 
 * Distinguishes between a 'REGULAR' customer and a 'SUBSCRIBER' (who may be entitled to discounts).
 */
public enum CustomerType {

    REGULAR("REGULAR"),
    SUBSCRIBER("SUBSCRIBER");

    private String str;

    private CustomerType(String str) {
        this.str = str;
    }

    public String getString() {
        return str;
    }
}