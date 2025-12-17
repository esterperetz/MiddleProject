package Entities;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Order implements Serializable {

	// Enum for Order Status
	public enum OrderStatus {
		APPROVED, SEATED, PAID, CANCELLED
	}

	private int order_number;
	private Date order_date;
	private int number_of_guests;
	private int confirmation_code;
	private Integer subscriber_id; // Integer to allow NULL
	private Date date_of_placing_order;
	private String identification_details; // Mandatory
	private String full_name;
	private double total_price;
	private OrderStatus status;

	public Order(int order_number, Date order_date, int number_of_guests, int confirmation_code, Integer subscriber_id,
			Date date_of_placing_order, String identification_details, String full_name, double total_price,
			OrderStatus status) {
		this.order_number = order_number;
		this.order_date = order_date;
		this.number_of_guests = number_of_guests;
		this.confirmation_code = confirmation_code;
		this.subscriber_id = subscriber_id;
		this.date_of_placing_order = date_of_placing_order;
		this.identification_details = identification_details;
		this.full_name = full_name;
		this.total_price = total_price;
		this.status = status;
	}

	// Constructor for creating NEW order
	public Order(Date order_date, int number_of_guests, int confirmation_code, Integer subscriber_id,
			Date date_of_placing_order, String identification_details, String full_name) {

		if (identification_details == null || identification_details.isEmpty()) {
			throw new IllegalArgumentException("Identification details cannot be null or empty.");
		}

		this.order_date = order_date;
		this.number_of_guests = number_of_guests;
		this.confirmation_code = confirmation_code;
		this.subscriber_id = subscriber_id;
		this.date_of_placing_order = date_of_placing_order;
		this.identification_details = identification_details;
		this.full_name = full_name;

		// Defaults
		this.total_price = 0.0;
		this.status = OrderStatus.APPROVED;
	}

	// Getters and Setters
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

	public Integer getSubscriber_id() {
		return subscriber_id;
	}

	public void setSubscriber_id(Integer subscriber_id) {
		this.subscriber_id = subscriber_id;
	}

	public Date getDate_of_placing_order() {
		return date_of_placing_order;
	}

	public void setDate_of_placing_order(Date date_of_placing_order) {
		this.date_of_placing_order = date_of_placing_order;
	}

	public String getIdentification_details() {
		return identification_details;
	}

	public void setIdentification_details(String identification_details) {
		this.identification_details = identification_details;
	}

	public String getFull_name() {
		return full_name;
	}

	public void setFull_name(String full_name) {
		this.full_name = full_name;
	}

	public double getTotal_price() {
		return total_price;
	}

	public void setTotal_price(double total_price) {
		this.total_price = total_price;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Order [order_number=" + order_number + ", order_date=" + order_date + ", identification="
				+ identification_details + ", status=" + status + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Order other = (Order) obj;
		return order_number == other.order_number;
	}
}