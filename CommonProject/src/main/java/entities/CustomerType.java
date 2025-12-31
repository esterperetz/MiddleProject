package entities;

public enum CustomerType {
	
	    REGULAR("REGULAR"),    // לקוח מזדמן/רגיל
	    SUBSCRIBER("SUBSCRIBER");// לקוח רשום כמנוי
	    private String str;
	    
	    private CustomerType(String str) {
	    	this.str = str;
	    }
	    public String getString() {
	    	return str;
	    }
}
