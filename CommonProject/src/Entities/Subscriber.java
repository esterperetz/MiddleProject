package Entities;

import java.io.Serializable;

public class Subscriber implements Serializable {
	private static final long serialVersionUID = 1L;

	private int subscriber_id;
	private String subscriber_name;
	private String phone_number;
	private String email;
	
	public Subscriber(int subscriber_id, String subscriber_name, String phone_number, String email) {
		this.subscriber_id = subscriber_id;
		this.subscriber_name = subscriber_name;
		this.phone_number = phone_number;
		this.email = email;
	}

	public int getSubscriber_id() {
		return subscriber_id;
	}


	public void setSubscriber_id(int subscriber_id) {
		this.subscriber_id = subscriber_id;
	}


	public String getSubscriber_name() {
		return subscriber_name;
	}


	public void setSubscriber_name(String subscriber_name) {
		this.subscriber_name = subscriber_name;
	}


	public String getPhone_number() {
		return phone_number;
	}


	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


   
	
}