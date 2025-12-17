package Entities;

import java.io.Serializable;

public class Subscriber implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private String firstName;
	private String lastName;
	private String username;
	private String phoneNumber;
//	private static int subscriberCode;
	private String email;


	public Subscriber(String firstName, String lastName, String username, String phoneNumber, String email) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.username = username;
		this.phoneNumber = phoneNumber;
		this.email = email;		
	}
	

	// Getters and Setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	@Override
	public String toString() {
		return "Subscriber [id=" + id + ", username=" + username + ", phone=" + phoneNumber + "]";
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Subscriber other = (Subscriber) obj;

		return this.id == other.getId();
	}
}