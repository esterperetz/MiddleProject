package entities;

import java.io.Serializable;

public class Customer implements Serializable {
	private static final long serialVersionUID = 1L;
	

	private int subscriberCode;
	private String name;
	private String phoneNumber;
	private String email;
	private CustomerType type;
	private Integer customerId; 
	

	public Customer(Integer customerId, Integer subscriberCode, String name, String phoneNumber, String email, CustomerType type) {
		this.customerId = customerId;
		this.subscriberCode = subscriberCode;
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.type = type;
	}

	public int getSubscriberCode() {
		return subscriberCode;
	}

	public void setSubscriberCode(int subscriberCode) {
		this.subscriberCode = subscriberCode;
	}
	
	public int getCustomerId() {
		return customerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public CustomerType getType() {
		return type;
	}

	public void setType(CustomerType type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "Customer [subscriberCode=" + subscriberCode + ", name=" + name + ", phoneNumber="
				+ phoneNumber + ", email=" + email + ", type=" + type + "]";
	}

	public void setCustomerId(int customerId) {
		// TODO Auto-generated method stub
		this.customerId = customerId;
	}
}