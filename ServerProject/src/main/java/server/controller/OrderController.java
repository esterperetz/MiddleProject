package server.controller;

import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import DAO.OrderDAO;
import DAO.TableDAO;
import DAO.BusinessHourDAO;
import DAO.CustomerDAO;
import entities.*;
import entities.Order.OrderStatus;
import ocsf.server.ConnectionToClient;
import java.io.IOException;

public class OrderController {
	private final OrderDAO orderdao = new OrderDAO();
	private final TableDAO tabledao = new TableDAO();
	private final CustomerDAO customerDao = new CustomerDAO();
	private final BusinessHourDAO businessHourDao = new BusinessHourDAO();
	private final Object tableLock = new Object();

	public OrderController() {

//		Executors.newSingleThreadScheduledExecutor().schedule(() -> {
//			try {
//				test();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}, 10, TimeUnit.SECONDS);
	}

	public void test() throws IOException {
		Order order = new Order();
		order.setDateOfPlacingOrder(java.sql.Date.valueOf(LocalDate.now()));
		order.getArrivalTime();
		order.setNumberOfGuests(5);
		Request req = new Request(ResourceType.ORDER, ActionType.GET_AVAILABLE_TIME, null, order);
		handle(req, null, null);
	}

	public void handle(Request req, ConnectionToClient client, List<ConnectionToClient> clients) throws IOException {
		if (req.getResource() != ResourceType.ORDER) {
			client.sendToClient(new Response(req.getResource(), ActionType.GET_ALL, Response.ResponseStatus.ERROR,
					"Error: Incorrect resource type requested. Expected ORDER.", null));
			return;
		}

		try {
			switch (req.getAction()) {
			case GET_ALL:
				handleGetAll(req, client);
				break;
			case GET_ALL_BY_SUBSCRIBER_ID:
				handleGetAllBySubscriberId(req, client);
				break;
			case GET_BY_ID:
				handleGetById(req, client);
				break;
			case GET_AVAILABLE_TIME:
//				getAvailabilityOptions();
				checkAvailability(((Order) req.getPayload()).getDateOfPlacingOrder(),
						((Order) req.getPayload()).getNumberOfGuests());
				break;
			case GET_BY_CODE:
				handleGetByCode(req, client);
				break;
			case CREATE:
				handleCreate(req, client);
				break;
			case UPDATE:
				handleUpdate(req, client);
				break;
			case DELETE:
				handleDelete(req, client);
				break;
			case CHECK_AVAILABILITY:
				handleCheckAvailability(req, client);
				break;
			case IDENTIFY_AT_TERMINAL:
				handleIdentifyAtTerminal(req, client);
				break;
			case PAY_BILL:
				handlePayBill(req, client);
				break;
			case SEND_EMAIL:
				handleSendEmail(req, client);
				break;
			case RESEND_CONFIRMATION:
				handleResendConfirmation(req, client);
				break;
			default:
				client.sendToClient(new Response(null, null, Response.ResponseStatus.ERROR,
						"Unsupported action: " + req.getAction(), null));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			client.sendToClient("Database error: " + e.getMessage());
		}
	}

	private void handleGetByCode(Request req, ConnectionToClient client) throws SQLException, IOException {
		if (req.getPayload() == null) {
			client.sendToClient(new Response(req.getResource(), ActionType.GET_BY_CODE, Response.ResponseStatus.ERROR,
					"Error: ID missing.", null));
			return;
		}

		Order order = orderdao.getOrderByConfirmationCode((int) req.getPayload());
		if (order == null)
			client.sendToClient(new Response(req.getResource(), ActionType.GET_BY_CODE, Response.ResponseStatus.ERROR,
					"Error: Code have not found.", null));
		else {
			client.sendToClient(new Response(req.getResource(), ActionType.GET_BY_CODE, Response.ResponseStatus.SUCCESS,
					"Error: ID missing.", order));
			return;
		}

	}

	private void handleGetAll(Request req, ConnectionToClient client) throws SQLException, IOException {
		List<Map<String, Object>> orders = orderdao.getAllOrdersWithCustomers();
		client.sendToClient(
				new Response(req.getResource(), ActionType.GET_ALL, Response.ResponseStatus.SUCCESS, null, orders));
	}
	//
	// private void handleGetAll(Request req, ConnectionToClient client) throws
	// SQLException, IOException {
	// List<Order> orders = orderdao.getAllOrders();
	// client.sendToClient(
	// new Response(req.getResource(), ActionType.GET_ALL,
	// Response.ResponseStatus.SUCCESS, null, orders));
	// }

	private void handleGetAllBySubscriberId(Request req, ConnectionToClient client) throws SQLException, IOException {
		if (req.getId() == null) {
			client.sendToClient(new Response(req.getResource(), ActionType.GET_ALL_BY_SUBSCRIBER_ID,
					Response.ResponseStatus.ERROR, "Error: ID missing.", null));
			return;
		}
		Customer cusId = customerDao.getCustomerBySubscriberCode(req.getId());
		List<Order> subOrders = orderdao.getOrdersByCustomerId(cusId.getCustomerId());
		client.sendToClient(new Response(req.getResource(), ActionType.GET_ALL_BY_SUBSCRIBER_ID,
				Response.ResponseStatus.SUCCESS, null, subOrders));
	}

	private void handleGetById(Request req, ConnectionToClient client) throws SQLException, IOException {
		if (req.getId() == null) {
			client.sendToClient(new Response(req.getResource(), ActionType.GET_BY_ID, Response.ResponseStatus.ERROR,
					"Error: ID missing.", null));
			return;
		}
		Order order = orderdao.getOrder(req.getId());
		client.sendToClient(
				new Response(req.getResource(), ActionType.GET_BY_ID, Response.ResponseStatus.SUCCESS, null, order));
	}

	private boolean handleCreate(Request req, ConnectionToClient client) throws IOException, SQLException {
		Object payload = req.getPayload();
	    Order order = null;
	    Customer guestData = null;

	    if (payload instanceof Map) {
	        @SuppressWarnings("unchecked")
	        Map<String, Object> data = (Map<String, Object>) payload;
	        order = (Order) data.get("order");
	        guestData = (Customer) data.get("guest");
	    } else if (payload instanceof Order) {
	        order = (Order) payload;
	        guestData = order.getCustomer(); 
	    } 

	 

	    int guests = order.getNumberOfGuests();
	    int minGuestsThreshold = (guests > 5) ? 6 : 1;
	    int totalTables = tabledao.countSuitableTables(guests);
	    int conflictingOrders = orderdao.countActiveOrdersInTimeRange(order.getOrderDate(), minGuestsThreshold);

	    if (totalTables - conflictingOrders <= 0) {
	        List<TimeSlotStatus> alternatives = checkAvailability(order.getOrderDate(), guests);
	        client.sendToClient(new Response(ResourceType.ORDER, ActionType.CREATE, Response.ResponseStatus.ERROR,
	                "The restaurant is full at this time.", alternatives));
	        return false;
	    }

	    Customer finalCustomer = null;
	    Integer subCode = order.getCustomer().getSubscriberCode();
	    
	    if ((subCode == null || subCode == 0) && guestData != null) {
	        subCode = guestData.getSubscriberCode();
	    }

	    try {
	        if (subCode != null && subCode > 0) {
	            finalCustomer = customerDao.getCustomerBySubscriberCode(subCode);
	            
	            if (finalCustomer == null) {
	                client.sendToClient(new Response(ResourceType.ORDER, ActionType.CREATE, 
	                        Response.ResponseStatus.ERROR, "Invalid Subscriber Code.", null));
	                return false;
	            }
	        } 
	        else {
	        	Customer dataToUse;

	        	if (guestData != null) {
	        	    dataToUse = guestData;
	        	} else {
	        	    dataToUse = order.getCustomer();
	        	}
	            finalCustomer = customerDao.getCustomerByEmail(dataToUse.getEmail());

	            if (finalCustomer == null) {
	                dataToUse.setType(CustomerType.REGULAR);
	                customerDao.createCustomer(dataToUse); 
	                
	                finalCustomer = customerDao.getCustomerByEmail(dataToUse.getEmail());
	            }
	        }

	        if (finalCustomer == null || finalCustomer.getCustomerId() == null) {
	            throw new SQLException("Failed to resolve customer ID.");
	        }

	        order.getCustomer().setCustomerId(finalCustomer.getCustomerId());
	        
	        order.getCustomer().setName(finalCustomer.getName());
	        order.getCustomer().setPhoneNumber(finalCustomer.getPhoneNumber());
	        order.getCustomer().setEmail(finalCustomer.getEmail());

	        order.setConfirmationCode(generateUniqueConfirmationCode());
	        order.setOrderStatus(Order.OrderStatus.APPROVED);

	        boolean success = orderdao.createOrder(order);

	        if (success) {
	            client.sendToClient(new Response(ResourceType.ORDER, ActionType.CREATE, Response.ResponseStatus.SUCCESS,
	                    "Order created successfully!", order));
	            return true;
	        } else {
	            client.sendToClient(new Response(ResourceType.ORDER, ActionType.CREATE,
	                    Response.ResponseStatus.DATABASE_ERROR, "Failed to save order in database.", null));
	            return false;
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        client.sendToClient(new Response(ResourceType.ORDER, ActionType.CREATE,
	                Response.ResponseStatus.DATABASE_ERROR, "DB Error: " + e.getMessage(), null));
	        return false;
	    }
	}
	private void handleUpdate(Request req, ConnectionToClient client) throws SQLException, IOException {
		Order updatedOrder = (Order) req.getPayload();
		if (orderdao.updateOrder(updatedOrder)) {
			/// need to get email from customer table
			Customer customer = customerDao.getCustomerByCustomerId(updatedOrder.getCustomer().getCustomerId());
			if (customer != null)
				EmailService.sendConfirmation(customer, updatedOrder);
			System.out.println(EmailService.getContent());
			client.sendToClient(new Response(req.getResource(), ActionType.UPDATE, Response.ResponseStatus.SUCCESS,
					"Order updated.", updatedOrder));
			sendOrdersToAllClients();
		}
	}

	private void handleDelete(Request req, ConnectionToClient client) throws SQLException, IOException {
		if (req.getId() == null)
			return;

		// Safety check is a table needs to be released.
		Order order = orderdao.getOrder(req.getId());
		if (order != null && order.getOrderStatus() == Order.OrderStatus.SEATED) {
			if (order.getTableNumber() != null) {
				tabledao.updateTableStatus(order.getTableNumber(), false);
			}
		}

		if (orderdao.deleteOrder(req.getId())) {
			/// need to get email from customer table

			Customer customer = customerDao.getCustomerByCustomerId(order.getCustomer().getCustomerId());
			if (customer != null)
				EmailService.sendCancelation(customer, order);
			System.out.println(EmailService.getContent());
			client.sendToClient(new Response(req.getResource(), ActionType.DELETE, Response.ResponseStatus.SUCCESS,
					"Order deleted.", order));
			sendOrdersToAllClients();
		}
	}

	private boolean handleCheckAvailability(Request req, ConnectionToClient client) throws IOException {
		try {
			Order requestedOrder = (Order) req.getPayload();
			java.util.Date orderDate = requestedOrder.getOrderDate();
			int guests = requestedOrder.getNumberOfGuests();

			// 1. חישוב סף אורחים
			int minGuestsThreshold = (guests > 5) ? 6 : 1;

			// 2. בדיקה ספציפית: האם יש מקום להזמנה הנוכחית?
			int totalSuitableTables = tabledao.countSuitableTables(guests);
			if (totalSuitableTables == 0) {
				client.sendToClient(new Response(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY,
						Response.ResponseStatus.ERROR, "No table exists for " + guests + " guests.", null));
				return false;
			}

			int conflictingOrders = orderdao.countActiveOrdersInTimeRange(orderDate, minGuestsThreshold);
			int available = totalSuitableTables - conflictingOrders;

			List<TimeSlotStatus> timeSlots = checkAvailability(orderDate, guests);

			if (available > 0) {
				client.sendToClient(new Response(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY,
						Response.ResponseStatus.SUCCESS, "Table is available.", timeSlots));
				return true;
			} else {
				client.sendToClient(new Response(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY,
						Response.ResponseStatus.ERROR, "The restaurant is full at this time.", timeSlots));
				return false;
			}

		} catch (SQLException e) {
//			e.printStackTrace();
			client.sendToClient(new Response(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY,
					Response.ResponseStatus.DATABASE_ERROR, "Database error checking availability.", null));
			return false;
		}
	}

	private List<String> getAvailabilityOptions(Date dateOrder) throws SQLException, IOException {

		LocalDate date = new java.sql.Date(dateOrder.getTime()).toLocalDate();
		int dayOfWeek = date.getDayOfWeek().getValue();

		OpeningHours dayHours = businessHourDao.getHoursForDate(dayOfWeek);

		List<String> options = new ArrayList<>();

		if (dayHours == null || dayHours.isClosed()) {
			System.out.println("Restaurant is closed.");
//	        client.sendToClient(new Message(MessageType.SHOW_AVAILABILITY, options));
			return null;
		}

		LocalTime currentTime = dayHours.getOpenTime().toLocalTime();
		LocalTime closeTime = dayHours.getCloseTime().toLocalTime();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		LocalTime lastSeatingTime = dayHours.getCloseTime().toLocalTime().minusHours(2);
		while (!currentTime.isAfter(lastSeatingTime)) {

			LocalDateTime ldt = LocalDateTime.of(date, currentTime);
			Timestamp checkTime = Timestamp.valueOf(ldt);

			String timeStr = currentTime.format(formatter);

			options.add(timeStr);

			currentTime = currentTime.plusMinutes(30);
		}
		System.out.println(options);
		return options;
//	    client.sendToClient(new Response(ResourceType.ORDER, ActionType.GET_AVAILABLE_TIME,
//				Response.ResponseStatus.SUCCESS, "list of time", options));
	}

	public List<TimeSlotStatus> checkAvailability(Date date, int guests) throws SQLException, IOException {

		List<TimeSlotStatus> results = new ArrayList<>();

		int minGuestsThreshold = (guests > 5) ? 6 : 1;

		int totalTables = tabledao.countSuitableTables(guests);
		List<String> allSlots = getAvailabilityOptions(date);

		if (allSlots == null)
			return new ArrayList<>();

		LocalDate localDate = new java.sql.Date(date.getTime()).toLocalDate();

		for (String slotStr : allSlots) {

			LocalTime timeSlot = LocalTime.parse(slotStr);
			LocalDateTime ldt = LocalDateTime.of(localDate, timeSlot);
			java.sql.Timestamp specificTimeToCheck = java.sql.Timestamp.valueOf(ldt);

			int conflictingOrders = orderdao.countActiveOrdersInTimeRange(specificTimeToCheck, minGuestsThreshold);

			int available = totalTables - conflictingOrders;
			boolean isFull = (available <= 0); // אם 0 או פחות -> מלא

			results.add(new TimeSlotStatus(slotStr, isFull));
		}

		System.out.println(results);
		return results;
	}

	/**
	 * Handles customer arrival at the terminal. Checks 15-min rule and assigns a
	 * physical table.
	 */
	private void handleIdentifyAtTerminal(Request req, ConnectionToClient client) throws SQLException, IOException {
		if (req.getId() == null)
			return;
		Order order = orderdao.getOrderByConfirmationCode(req.getId());

		if (order != null && order.getOrderStatus() == Order.OrderStatus.APPROVED) {
			long diffInMinutes = (new Date().getTime() - order.getOrderDate().getTime()) / 60000;

			if (diffInMinutes > 15) { // if 15 minute violate
				order.setOrderStatus(Order.OrderStatus.CANCELLED);
				orderdao.updateOrder(order);
				client.sendToClient(new Response(ResourceType.ORDER, ActionType.IDENTIFY_AT_TERMINAL,
						Response.ResponseStatus.ERROR, "Expired", null));
			} else { // less than 15 minutes
				synchronized (tableLock) {
					Integer tableNum = tabledao.findAvailableTable(order.getNumberOfGuests());
					if (tableNum != null) {
						order.setOrderStatus(Order.OrderStatus.SEATED);
						order.setArrivalTime(new Date());
						order.setTableNumber(tableNum); // Assign table to order
						orderdao.updateOrder(order);

						tabledao.updateTableStatus(tableNum, true);

						client.sendToClient(new Response(ResourceType.ORDER, ActionType.IDENTIFY_AT_TERMINAL,
								Response.ResponseStatus.SUCCESS, "Table assigned: " + tableNum,
								order.getOrderNumber()));
					} else {
						client.sendToClient(new Response(ResourceType.ORDER, ActionType.IDENTIFY_AT_TERMINAL,
								Response.ResponseStatus.ERROR, "No table ready yet", null));
					}
				}
			}
			sendOrdersToAllClients();
		}
	}

	/**
	 * Calculates final bill with subscriber discounts and frees the physical table.
	 */
	private void handlePayBill(Request req, ConnectionToClient client) throws SQLException, IOException {
		if (req.getId() == null)
			return;
		Order order = orderdao.getOrder(req.getId());

		if (order != null && order.getOrderStatus() == Order.OrderStatus.SEATED) {
			double amount = order.getTotalPrice();

			// Check if customer is SUBSCRIBER for 10% discount
			if (order.getCustomer().getCustomerId() != null) {
				// Use correct DAO method to fetch customer by ID
				Customer c = customerDao.getCustomerByCustomerId(order.getCustomer().getCustomerId());
				if (c != null && c.getType() == CustomerType.SUBSCRIBER) {
					amount *= 0.9;
				}
			}

			order.setTotalPrice(amount);
			order.setOrderStatus(Order.OrderStatus.PAID);
			order.setLeavingTime(new Date());

			if (orderdao.updateOrder(order)) {
				// release table in DB
				if (order.getTableNumber() != null) {
					tabledao.updateTableStatus(order.getTableNumber(), false);
				}
				client.sendToClient(new Response(ResourceType.ORDER, ActionType.PAY_BILL,
						Response.ResponseStatus.SUCCESS, "Paid", amount));
				sendOrdersToAllClients();
			}
		}
	}

	private void handleSendEmail(Request req, ConnectionToClient client) {
		try {
			if (req.getPayload() instanceof Order) {
				Order order = (Order) req.getPayload();
				/// need to get email from customer table
				// EmailService.sendConfirmation(order.getClientEmail(), order);
				Router.sendToAllClients(new Response(ResourceType.ORDER, ActionType.SEND_EMAIL,
						Response.ResponseStatus.SUCCESS, "Email has been sent!", EmailService.getContent()));
			} else {
				Router.sendToAllClients(new Response(ResourceType.ORDER, ActionType.SEND_EMAIL,
						Response.ResponseStatus.ERROR, null, null));
			}

		} catch (Exception e) {
			System.out.println("From handle send email.");
		}

	}

	private void handleResendConfirmation(Request req, ConnectionToClient client) throws SQLException, IOException {
		String contact = (String) req.getPayload(); // Expect email or phone string
		if (contact == null || contact.isEmpty()) {
			client.sendToClient(new Response(ResourceType.ORDER, ActionType.RESEND_CONFIRMATION,
					Response.ResponseStatus.ERROR, "Missing contact details.", null));
			return;
		}

		Order order = orderdao.getOrderByContact(contact);
		if (order != null) {
			// Generate NEW UNIQUE confirmation code
			order.setConfirmationCode(generateUniqueConfirmationCode());

			// Update in Database
			orderdao.updateOrder(order);

			// Construct a temporary Customer object for EmailService
			// We can use the data fetched by the join in OrderDAO
			Customer tempCustomer = new Customer(order.getCustomer().getCustomerId(), order.getCustomer().getName(), order.getCustomer().getPhoneNumber(),
					order.getCustomer().getEmail());

			// Send Email with NEW code
			EmailService.sendConfirmation(tempCustomer, order);

			client.sendToClient(new Response(ResourceType.ORDER, ActionType.RESEND_CONFIRMATION,
					Response.ResponseStatus.SUCCESS, "New confirmation code generated and sent to email.", order));
		} else {
			client.sendToClient(new Response(ResourceType.ORDER, ActionType.RESEND_CONFIRMATION,
					Response.ResponseStatus.ERROR, "No upcoming approved order found for this contact.", null));
		}
	}

	private void sendOrdersToAllClients() {
		try {
			List<Order> orders = orderdao.getAllOrders();
			Router.sendToAllClients(new Response(ResourceType.ORDER, ActionType.GET_ALL,
					Response.ResponseStatus.SUCCESS, null, orders));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generates a unique 4-digit confirmation code by ensuring it doesn't collide
	 * with any currently APPROVED order.
	 */
	private int generateUniqueConfirmationCode() throws SQLException {
		int newCode;
		Order existingOrder;
		do {
			newCode = 1000 + (int) (Math.random() * 9000);
			existingOrder = orderdao.getOrderByConfirmationCode(newCode);
		} while (existingOrder != null);
		return newCode;
	}

}