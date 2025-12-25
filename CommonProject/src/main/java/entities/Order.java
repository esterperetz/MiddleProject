package entities;

import java.io.Serializable;
import java.util.Date;

public class Order implements Serializable {
	private static final long serialVersionUID = 1L;


	// Enum for Order Status
	public enum OrderStatus {
		APPROVED, SEATED, PAID, CANCELLED
	}

	private int orderNumber;
	private Date orderDate;
	private int numberOfGuests;
	private int confirmationCode;
	private Integer subscriberId;
	private Integer tableNumber;
	private Date dateOfPlacingOrder;
	private String clientName;
	private String clientEmail;
	private String clientPhone; 
	private Date arrivalTime;
	private Date leavingTime;
	private double totalPrice;
	private OrderStatus orderStatus;

	public Order(int orderNumber, Date orderDate, int numberOfGuests, int confirmationCode, Integer subscriberId,
			Integer tableNumber, Date dateOfPlacingOrder, String clientName, String clientEmail, String clientPhone, 
			Date arrivalTime, Date leavingTime, double totalPrice, OrderStatus orderStatus) {
		
		this.orderNumber = orderNumber;
		this.orderDate = orderDate;
		this.numberOfGuests = numberOfGuests;
		this.confirmationCode = confirmationCode;
		this.subscriberId = subscriberId;
		this.tableNumber = tableNumber;
		this.dateOfPlacingOrder = dateOfPlacingOrder;
		this.clientName = clientName;
		this.clientEmail = clientEmail;
		this.clientPhone = clientPhone;
		this.arrivalTime = arrivalTime;
		this.leavingTime = leavingTime;
		this.totalPrice = totalPrice;
		this.orderStatus = orderStatus;
		
	}

	// Getters and Setters
		public int getOrderNumber() {
			return orderNumber;
		}

		public void setOrderNumber(int orderNumber) {
			this.orderNumber = orderNumber;
		}

		public Date getOrderDate() {
			return orderDate;
		}

		public void setOrderDate(Date orderDate) {
			this.orderDate = orderDate;
		}

		public int getNumberOfGuests() {
			return numberOfGuests;
		}

		public void setNumberOfGuests(int numberOfGuests) {
			this.numberOfGuests = numberOfGuests;
		}

		public int getConfirmationCode() {
			return confirmationCode;
		}

		public void setConfirmationCode(int confirmationCode) {
			this.confirmationCode = confirmationCode;
		}

		public Integer getSubscriberId() {
			return subscriberId;
		}

		public void setSubscriberId(Integer subscriberId) {
			this.subscriberId = subscriberId;
		}

		public Integer getTableNumber() {
			return tableNumber;
		}

		public void setTableNumber(Integer tableNumber) {
			this.tableNumber = tableNumber;
		}

		public Date getDateOfPlacingOrder() {
			return dateOfPlacingOrder;
		}

		public void setDateOfPlacingOrder(Date dateOfPlacingOrder) {
			this.dateOfPlacingOrder = dateOfPlacingOrder;
		}

		public double getTotalPrice() {
			return totalPrice;
		}

		public void setTotalPrice(double totalPrice) {
			this.totalPrice = totalPrice;
		}

		public String getClientName() {
			return clientName;
		}

		public void setClientName(String clientName) {
			this.clientName = clientName;
		}

		public String getClientEmail() {
			return clientEmail;
		}

		public void setClientEmail(String clientEmail) {
			this.clientEmail = clientEmail;
		}

		public String getClientPhone() {
			return clientPhone;
		}

		public void setClientPhone(String clientPhone) {
			this.clientPhone = clientPhone;
		}

		public Date getArrivalTime() {
			return arrivalTime;
		}

		public void setArrivalTime(Date arrivalTime) {
			this.arrivalTime = arrivalTime;
		}
	    
	    public Date getLeavingTime() {
			return leavingTime;
		}

		public void setLeavingTime(Date leavingTime) {
			this.leavingTime = leavingTime;
		}

		public OrderStatus getOrderStatus() {
			return orderStatus;
		}

		public void setOrderStatus(OrderStatus orderStatus) {
			this.orderStatus = orderStatus;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null || getClass() != obj.getClass())
				return false;
			Order other = (Order) obj;
			return orderNumber == other.orderNumber;
		}
	}