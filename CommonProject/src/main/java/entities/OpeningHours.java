package entities;

import java.io.Serializable;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

public class OpeningHours implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private Date dateOfWeek; // 1-7 (Sun-Sat)
	private Date specialDate; // Specific date override
	private Time openTime;
	private Time closeTime;
	private boolean isClosed;
	private Integer dayOfWeek;
	private String description;
	
	public OpeningHours(Date dateOfWeek, Date specialDate, Time openTime, Time closeTime) {
		
		this.dateOfWeek = dateOfWeek;
		this.specialDate = specialDate;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.dayOfWeek = getDayOfWeek(dateOfWeek);
		

	}

	public OpeningHours(int id,Integer dayOfWeek, Date specialDate, Time openTime, Time closeTime, boolean isClosed) {
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
	
	public void setDescription(String data) {
		this.description = data;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getDateOfWeek() {
		return dateOfWeek;
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
	
	public int getDayOfWeek(Date dateOfWeek) {
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(dateOfWeek);
	    
	    // ב-Calendar, הקבוע SUNDAY הוא 1 באופן אוטומטי
	    int dayNumber = cal.get(Calendar.DAY_OF_WEEK);
	    
	    return dayNumber; 
	    // תוצאה: ראשון=1, שני=2 ... שבת=7
	}

	public Time getCloseTime() {
		return closeTime;
	}

	public boolean isClosed() {
		return isClosed;
	}
}