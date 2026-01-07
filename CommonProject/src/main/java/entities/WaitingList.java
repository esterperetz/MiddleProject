package entities;

import java.io.Serializable;
import java.util.Date;

public class WaitingList implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer waitingId;
	private Integer customerId;

	private int numberOfGuests;
	private Date enterTime;
	private int confirmationCode;
	private Customer customer;

	public WaitingList(Integer waitingId, Integer customerId,
			int numberOfGuests, Date enterTime, int confirmationCode,Customer customer) {
		this.waitingId = waitingId;
		this.customerId = customerId;
		this.numberOfGuests = numberOfGuests;
		this.enterTime = enterTime;
		this.confirmationCode = confirmationCode;
		this.customer = customer;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	// Getters and Setters
	public Integer getWaitingId() {
		return waitingId;
	}

	public void setWaitingId(Integer waitingId) {
		this.waitingId = waitingId;
	}

	public Integer getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Integer customerId) {
		this.customerId = customerId;
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
		return "WaitingList [guests=" + numberOfGuests + ", code=" + confirmationCode + "]";
	}

}