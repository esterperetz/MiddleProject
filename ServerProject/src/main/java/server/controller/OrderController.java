package server.controller;

import java.sql.SQLException;
import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Map;

import DAO.OrderDAO;
import DAO.TableDAO;
import DAO.BusinessHourDAO;
import DAO.CustomerDAO;
import entities.*;
import ocsf.server.ConnectionToClient;
import java.io.IOException;

public class OrderController {
	private final OrderDAO orderdao = new OrderDAO();
	private final TableDAO tabledao = new TableDAO();
	private final CustomerDAO customerDao = new CustomerDAO();
	private final BusinessHourDAO businessHourDao = new BusinessHourDAO();
	private final Object tableLock = new Object();

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

	@SuppressWarnings("unchecked")
	private void handleCreate(Request req, ConnectionToClient client) throws SQLException, IOException {
		Object payload = req.getPayload();
		Order order = null;
		Customer guest = null;

		// --- שלב 1: זיהוי סוג המידע (תמיכה לאחור) ---

		// בדיקה: האם זה ה-Flow החדש (Map שמכיל הזמנה + אולי אורח)?
		if (payload instanceof Map) {
			Map<String, Object> data = (Map<String, Object>) payload;
			order = (Order) data.get("order");
			guest = (Customer) data.get("guest");
		}
		// בדיקה: האם זה ה-Flow הישן (רק אובייקט Order)?
		else if (payload instanceof Order) {
			order = (Order) payload;
			// במקרה הזה guest נשאר null, והלוגיקה תמשיך לטיפול הרגיל ב-ID
		} else {
			client.sendToClient(new Response(req.getResource(), ActionType.CREATE,
					Response.ResponseStatus.ERROR, "Error: Invalid payload type.", null));
			return;
		}

		// --- שלב 2: טיפול בלקוח (Customer Resolution) ---

		// תרחיש א': הגיעו פרטי אורח (Guest) בתוך ה-Map
		if (guest != null) {
			// בדיקה אם המייל כבר קיים למניעת כפילויות
			Customer existing = customerDao.getSubscriberBySubscriberEmail(guest.getEmail());

			if (existing != null) {
				// הלקוח קיים -> משתמשים ב-ID שלו
				order.setCustomerId(existing.getCustomerId());
			} else {
				// לקוח חדש -> יוצרים אותו ב-DB
				boolean created = customerDao.createCustomer(guest);
				if (created) {
					// ה-DAO מעדכן את ה-ID באובייקט guest
					order.setCustomerId(guest.getCustomerId());
				} else {
					client.sendToClient(new Response(req.getResource(), ActionType.CREATE,
							Response.ResponseStatus.ERROR, "Error: Failed to create guest profile.", null));
					return;
				}
			}
		}
		// תרחיש ב': לא הגיע אורח (או שנשלח רק Order במקור, או שהמפ לא הכיל אורח)
		// זו הלוגיקה המקורית שלך בדיוק!
		else {
			Customer customer = null;

			// הגנה מפני Null ב-ID
			if (order.getCustomerId() != null) {
				customer = customerDao.getCustomerBySubscriberId(order.getCustomerId());
				if (customer == null) {
					customer = customerDao.getCustomerBySubscriberCode(order.getCustomerId());
				}
			}

			if (customer == null) {
				System.out.println("Customer not found for Order! ID received: " + order.getCustomerId());
				client.sendToClient(new Response(req.getResource(), ActionType.CREATE,
						Response.ResponseStatus.ERROR, "Error: Customer not found in system.", null));
				return;
			}

			// עדכון ה-ID הסופי (לביטחון)
			order.setCustomerId(customer.getCustomerId());
		}

		// --- שלב 3: יצירת ההזמנה (משותף לכולם) ---

		// Generate unique confirmation code
		order.setConfirmationCode(generateUniqueConfirmationCode());

		if (orderdao.createOrder(order)) {
			client.sendToClient(new Response(req.getResource(), ActionType.CREATE,
					Response.ResponseStatus.SUCCESS, "Order created.", order));
			sendOrdersToAllClients();
		} else {
			client.sendToClient(new Response(req.getResource(), ActionType.CREATE,
					Response.ResponseStatus.ERROR, "Database Error: Failed to create order.", null));
		}
	}

	private void handleUpdate(Request req, ConnectionToClient client) throws SQLException, IOException {
		Order updatedOrder = (Order) req.getPayload();
		if (orderdao.updateOrder(updatedOrder)) {
			/// need to get email from customer table
			// EmailService.sendConfirmation(updatedOrder.getClientEmail(),updatedOrder);
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

			// EmailService.sendCancelation(order.getClientEmail(),order);
			System.out.println(EmailService.getContent());
			client.sendToClient(new Response(req.getResource(), ActionType.DELETE, Response.ResponseStatus.SUCCESS,
					"Order deleted.", order));
			sendOrdersToAllClients();
		}
	}

	private void handleCheckAvailability(Request req, ConnectionToClient client) throws SQLException, IOException {
		Order requestedOrder = (Order) req.getPayload();

		// 1. Booking time rules: At least 1 hour and no more than 1 month in advance
		long now = new Date().getTime();
		long requestedMillis = requestedOrder.getOrderDate().getTime();
		long diffInHours = (requestedMillis - now) / (1000 * 60 * 60);

		if (diffInHours < 1 || requestedMillis > (now + (31L * 24 * 60 * 60 * 1000))) {
			client.sendToClient(new Response(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY,
					Response.ResponseStatus.ERROR, "Order must be between 1 hour and 1 month in advance", null));
			return;
		}

		// 2. Business hours check: Priorities handle overrides for special dates
		OpeningHours hours = businessHourDao.getHoursForDate(requestedOrder.getOrderDate());
		if (hours == null || hours.isClosed()) {
			client.sendToClient(new Response(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY,
					Response.ResponseStatus.ERROR, "Restaurant is closed on this date", null));
			return;
		}

		// Time validation
		Time reqTime = new Time(requestedMillis);
		if (reqTime.before(hours.getOpenTime()) || reqTime.after(hours.getCloseTime())) {
			client.sendToClient(new Response(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY,
					Response.ResponseStatus.ERROR, "Requested time is outside operating hours", null));
			return;
		}

		// 3. Table capacity check
		int guests = requestedOrder.getNumberOfGuests();
		int totalSuitableTables = tabledao.countSuitableTables(guests);

		if (totalSuitableTables == 0) {
			client.sendToClient(new Response(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY,
					Response.ResponseStatus.ERROR, "No suitable tables for this party size", null));
			return;
		}

		int overlappingOrders = orderdao.countActiveOrdersInTimeRange(requestedOrder.getOrderDate(), guests);
		boolean available = (totalSuitableTables - overlappingOrders) > 0;

		client.sendToClient(new Response(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY,
				Response.ResponseStatus.SUCCESS, null, available));
	}

	/**
	 * Handles customer arrival at the terminal. Checks 15-min rule and assigns a
	 * physical table.
	 */
	private void handleIdentifyAtTerminal(Request req, ConnectionToClient client) throws SQLException, IOException {
		if (req.getId() == null)
			return;
		Order order = orderdao.getByConfirmationCode(req.getId());

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
			if (order.getCustomerId() != null) {
				// Use correct DAO method to fetch customer by ID
				Customer c = customerDao.getCustomerBySubscriberId(order.getCustomerId());
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
			Customer tempCustomer = new Customer(
					order.getCustomerId(),
					order.getClientName(),
					order.getClientPhone(),
					order.getClientEmail());

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
			existingOrder = orderdao.getByConfirmationCode(newCode);
		} while (existingOrder != null);
		return newCode;
	}
}