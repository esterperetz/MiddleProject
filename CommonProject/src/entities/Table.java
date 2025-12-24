package entities;

import java.io.Serializable;

public class Table implements Serializable {
	private static final long serialVersionUID = 1L;

    private int tableNumber;
    private int numberOfSeats;
    private Boolean isOccupied;

    public Table(int tableNumber, int numberOfSeats,Boolean isOccupied) {
        this.tableNumber = tableNumber;
        this.numberOfSeats = numberOfSeats;
        this.isOccupied = isOccupied;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }
    
    public Boolean getIsOccupied() {
    	return isOccupied;
    }
    
    public void setIsOccupied(Boolean occupation) {
    	this.isOccupied = occupation;
    }
    @Override
    public String toString() {
        return "Table [tableNumber=" + tableNumber + ", numberOfSeats=" + numberOfSeats + "]";
    }
}