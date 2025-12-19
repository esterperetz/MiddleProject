package Entities;

import java.io.Serializable;

public class Table implements Serializable {
	private int tableNumber;
	private int numberOfSeats;
	private boolean isOccupied;

	public Table(int tableNumber, int numberOfSeats) {
		this.tableNumber = tableNumber;
		this.numberOfSeats = numberOfSeats;
		this.isOccupied = false;
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

	public boolean isOccupied() {
		return isOccupied;
	}

	public void setOccupied(boolean occupied) {
		isOccupied = occupied;
	}

	@Override
	public String toString() {
		return "Table [tableNumber=" + tableNumber + ", numberOfSeats=" + numberOfSeats + ", isOccupied=" + isOccupied
				+ "]";
	}
}