package Entities;

import java.io.Serializable;
import java.util.Date;
//ssssssssssssssss
public class Order implements Serializable {
	
	private int order_number; 
	private Date order_date; 
	private int number_of_guests ;
	private int confirmation_code  ;
	private int subscriber_id ;
	private Date date_of_placing_order;
	public Order(int order_number, Date order_date, int number_of_guests, int confirmation_code, int subscriber_id,
			Date date_of_placing_order) {
		super();
		this.order_number = order_number;
		this.order_date = order_date;
		this.number_of_guests = number_of_guests;
		this.confirmation_code = confirmation_code;
		this.subscriber_id = subscriber_id;
		this.date_of_placing_order = date_of_placing_order;
	}
	public int getOrder_number() {
		return order_number;
	}
	public void setOrder_number(int order_number) {
		this.order_number = order_number;
	}
	public Date getOrder_date() {
		return order_date;
	}
	public void setOrder_date(Date order_date) {
		this.order_date = order_date;
	}
	public int getNumber_of_guests() {
		return number_of_guests;
	}
	public void setNumber_of_guests(int number_of_guests) {
		this.number_of_guests = number_of_guests;
	}
	public int getConfirmation_code() {
		return confirmation_code;
	}
	public void setConfirmation_code(int confirmation_code) {
		this.confirmation_code = confirmation_code;
	}
	public int getSubscriber_id() {
		return subscriber_id;
	}
	public void setSubscriber_id(int subscriber_id) {
		this.subscriber_id = subscriber_id;
	}
	public Date getDate_of_placing_order() {
		return date_of_placing_order;
	}
	public void setDate_of_placing_order(Date date_of_placing_order) {
		this.date_of_placing_order = date_of_placing_order;
	}

}
