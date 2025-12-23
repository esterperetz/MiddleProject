package entities;

import java.io.Serializable;

public class Subscriber implements Serializable {
	private static final long serialVersionUID = 1L;

	private int subscriberId;
	private String subscriberName;
	private String phoneNumber;
	private String email;
	
	public Subscriber(int subscriberId, String subscriberName, String phoneNumber, String email) {
		this.subscriberId = subscriberId;
		this.subscriberName = subscriberName;
		this.phoneNumber = phoneNumber;
		this.email = email;
	}

	public int getSubscriberId() {
		return subscriberId;
	}

	public void setSubscriberId(int subscriberId) {
		this.subscriberId = subscriberId;
	}

	public String getSubscriberName() {
		return subscriberName;
	}

	public void setSubscriberName(String subscriberName) {
		this.subscriberName = subscriberName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}