package Entities;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Order implements Serializable {

	public enum OrderStatus {
		APPROVED, SEATED, PAID, CANCELLED
	}

	private int order_number;
	private Date order_date;
	private int number_of_guests;
	private int confirmation_code;
	private Integer subscriber_id;
	private Date date_of_placing_order;
	private String client_name;
	private String client_email; 
	private String client_Phone; 
	private Date ArrivalTime;
	private double total_price;
	private OrderStatus order_status;
	private Integer table_number;
	private Date leaveTime;

	public Order(int order_number, Date order_date, int number_of_guests, int confirmation_code, Integer subscriber_id,
	        Date date_of_placing_order, String client_name, String client_email, String client_Phone, Date ArrivalTime, 
	        double total_price, OrderStatus order_status) {
	    
	    if (client_email == null || client_Phone == null) {
	        throw new IllegalArgumentException("Email and Phone cannot be null.");
	    }

	    this.order_number = order_number;
	    this.order_date = order_date;
	    this.number_of_guests = number_of_guests;
	    this.confirmation_code = confirmation_code;
	    this.subscriber_id = subscriber_id;
	    this.date_of_placing_order = date_of_placing_order;
	    this.client_name = client_name;   
	    this.client_email = client_email;
	    this.client_Phone = client_Phone;
	    this.ArrivalTime = ArrivalTime;
	    this.total_price = total_price;
	    this.order_status = (order_status != null) ? order_status : OrderStatus.APPROVED;
	}

	public int getOrder_number() { return order_number; }
	public void setOrder_number(int order_number) { this.order_number = order_number; }

	public Date getOrder_date() { return order_date; }
	public void setOrder_date(Date order_date) { this.order_date = order_date; }

	public int getNumber_of_guests() { return number_of_guests; }
	public void setNumber_of_guests(int number_of_guests) { this.number_of_guests = number_of_guests; }

	public int getConfirmation_code() { return confirmation_code; }
	public void setConfirmation_code(int confirmation_code) { this.confirmation_code = confirmation_code; }

	public Integer getSubscriber_id() { return subscriber_id; }
	public void setSubscriber_id(Integer subscriber_id) { this.subscriber_id = subscriber_id; }

	public Date getDate_of_placing_order() { return date_of_placing_order; }
	public void setDate_of_placing_order(Date date_of_placing_order) { this.date_of_placing_order = date_of_placing_order; }

	public double getTotal_price() { return total_price; }
	public void setTotal_price(double total_price) { this.total_price = total_price; }

	public String getClient_name() { return client_name; }
	public void setClient_name(String client_name) { this.client_name = client_name; }

	public String getClient_email() { return client_email; }
	public void setClient_email(String client_email) { this.client_email = client_email; }

	public String getClient_Phone() { return client_Phone; }
	public void setClient_Phone(String client_Phone) { this.client_Phone = client_Phone; }

	public Date getArrivalTime() { return ArrivalTime; }
	public void setArrivalTime(Date arrivalTime) { ArrivalTime = arrivalTime; }

	public OrderStatus getOrder_status() { return order_status; }
	public void setOrder_status(OrderStatus order_status) { this.order_status = order_status; }

	public Integer getTable_number() { return table_number; }
	public void setTable_number(Integer table_number) { this.table_number = table_number; }

	public Date getLeaveTime() { return leaveTime; }
	public void setLeaveTime(Date leaveTime) { this.leaveTime = leaveTime; }

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		Order other = (Order) obj;
		return order_number == other.order_number;
	}
}