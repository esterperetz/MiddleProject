package entities;

import java.io.Serializable;

public class TimeSlotStatus implements Serializable {

	private static final long serialVersionUID = 1L;
	private String time; // למשל "19:00"
	private boolean isFull; // true = רשימת המתנה, false = פנוי להזמנה

	public TimeSlotStatus(String time, boolean isFull) {
		this.time = time;
		this.isFull = isFull;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public boolean isFull() {
		return isFull;
	}

	public void setFull(boolean isFull) {
		this.isFull = isFull;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return "TimeSlotStatus [time=" + time + ", isFull=" + isFull + "]";
	}
	
	



}
