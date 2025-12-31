package entities;

import java.io.Serializable;

public class Customer implements Serializable {
	private static final long serialVersionUID = 1L;

	private int customerId;
	private Integer subscriberCode;
	private String name;
	private String phoneNumber;
	private String email;
	private CustomerType type; 
	

	public Customer(int customerId,Integer subscriberCode, String name, String phoneNumber, String email, CustomerType type) {
		this.customerId = customerId;
		this.subscriberCode = subscriberCode;
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.type = type;
	}

	public int getCustomerId() {
		return subscriberCode;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}
	
	public Integer getSubscriberCode() {
		return subscriberCode;
	}

	public void setSubscriberCode(Integer subscriberCode) {
		this.subscriberCode = subscriberCode;
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
		return "Customer [customerId=" + customerId + ", name=" + name + ", phoneNumber="
				+ phoneNumber + ", email=" + email + ", type=" + type + "]";
	}
}