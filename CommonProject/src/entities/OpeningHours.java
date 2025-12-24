package entities;

import java.io.Serializable;
import java.sql.Time;
import java.util.Date;

public class OpeningHours implements Serializable {
	private int id;
	private Integer dayOfWeek; // 1-7 (Sun-Sat)
	private Date specialDate; // Specific date override
	private Time openTime;
	private Time closeTime;
	private boolean isClosed;

	public OpeningHours(int id, Integer dayOfWeek, Date specialDate, Time openTime, Time closeTime, boolean isClosed) {
		this.id = id;
		this.dayOfWeek = dayOfWeek;
		this.specialDate = specialDate;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.isClosed = isClosed;
	}

	// Getters and Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Integer getDayOfWeek() {
		return dayOfWeek;
	}

	public Date getSpecialDate() {
		return specialDate;
	}

	public Time getOpenTime() {
		return openTime;
	}

	public Time getCloseTime() {
		return closeTime;
	}

	public boolean isClosed() {
		return isClosed;
	}
}