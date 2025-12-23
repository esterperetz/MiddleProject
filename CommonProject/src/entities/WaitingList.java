package entities;

import java.io.Serializable;
import java.util.Date;

public class WaitingList implements Serializable {
	private int waitingId;
	private Integer subscriberId;
	private String identificationDetails; //Phone or Email for non subscriber
	private String fullName;
	private int numberOfGuests;
	private Date enterTime;
	private int confirmationCode;

	public WaitingList(int waitingId, Integer subscriberId, String identificationDetails, String fullName,
			int numberOfGuests, Date enterTime, int confirmationCode) {
		this.waitingId = waitingId;
		this.subscriberId = subscriberId;
		this.identificationDetails = identificationDetails;
		this.fullName = fullName;
		this.numberOfGuests = numberOfGuests;
		this.enterTime = enterTime;
		this.confirmationCode = confirmationCode;
	}

	// Getters and Setters
	public int getWaitingId() {
		return waitingId;
	}

	public void setWaitingId(int waitingId) {
		this.waitingId = waitingId;
	}

	public Integer getSubscriberId() {
		return subscriberId;
	}

	public void setSubscriberId(Integer subscriberId) {
		this.subscriberId = subscriberId;
	}

	public String getIdentificationDetails() {
		return identificationDetails;
	}

	public void setIdentificationDetails(String identificationDetails) {
		this.identificationDetails = identificationDetails;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public int getNumberOfGuests() {
		return numberOfGuests;
	}

	public void setNumberOfGuests(int numberOfGuests) {
		this.numberOfGuests = numberOfGuests;
	}

	public Date getEnterTime() {
		return enterTime;
	}

	public void setEnterTime(Date enterTime) {
		this.enterTime = enterTime;
	}

	public int getConfirmationCode() {
		return confirmationCode;
	}

	public void setConfirmationCode(int confirmationCode) {
		this.confirmationCode = confirmationCode;
	}

	@Override
	public String toString() {
		return "WaitingList [name=" + fullName + ", guests=" + numberOfGuests + ", code=" + confirmationCode + "]";
	}
}