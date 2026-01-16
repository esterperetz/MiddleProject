package server.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
				Order order = (Order) req.getPayload();
				checkAvailability(order.getDateOfPlacingOrder(), order.getNumberOfGuests());
				break;
			case GET_USER_ORDERS:
				handleGetSubscriberApprovedOrders(req, client);
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
			case UPDATE_CHECKOUT:
				handelUpdateCheckOut(req, client);
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
		Customer customer = new Customer();
		customer.setCustomerId(0);
		if ((int) req.getId() != 0) {
			customer = customerDao.getCustomerBySubscriberCode((int) req.getId());
			if (customer == null) {
				client.sendToClient(new Response(ResourceType.TABLE, ActionType.GET, Response.ResponseStatus.ERROR,
						"ERROR CHECKOUT :CANOT find coustomerId by subscriber code ", null));
				return;
			}
		}

		Order order = orderdao.getOrderByConfirmationCodeSeated((int) req.getPayload(), customer.getCustomerId());
		if (order == null)
			client.sendToClient(new Response(req.getResource(), ActionType.GET_BY_CODE, Response.ResponseStatus.ERROR,
					"Error: order have not found.", null));
		else {
			client.sendToClient(new Response(req.getResource(), ActionType.GET_BY_CODE, Response.ResponseStatus.SUCCESS,
					"Check out successfully.", order));
			return;
		}

	}

	private void handleGetAll(Request req, ConnectionToClient client) throws SQLException, IOException {
		List<Map<String, Object>> orders = orderdao.getAllOrdersWithCustomers();
		client.sendToClient(
				new Response(req.getResource(), ActionType.GET_ALL, Response.ResponseStatus.SUCCESS, null, orders));
	}

	private void handleGetSubscriberApprovedOrders(Request req, ConnectionToClient client)
			throws SQLException, IOException {
		int subscriberCode = (int) req.getId();

		List<Order> orders = orderdao.getAllOrdersApproved(subscriberCode);
		if (orders != null) {
			client.sendToClient(new Response(req.getResource(), ActionType.GET_USER_ORDERS,
					Response.ResponseStatus.SUCCESS, null, orders));
			return;
		}
		client.sendToClient(new Response(req.getResource(), ActionType.GET_USER_ORDERS, Response.ResponseStatus.ERROR,
				"Error: No orders for subscriber ", null));

	}

	private void handleGetAllBySubscriberId(Request req, ConnectionToClient client) throws SQLException, IOException {
		if (req.getId() == null) {
			client.sendToClient(new Response(req.getResource(), ActionType.GET_ALL_BY_SUBSCRIBER_ID,
					Response.ResponseStatus.ERROR, "Error: ID missing.", null));
			return;
		}
		Customer cusId = customerDao.getCustomerBySubscriberCode(req.getId());
		List<Order> subOrders = orderdao.getOrdersByCustomerId(cusId.getCustomerId());
		if (subOrders != null) {
			client.sendToClient(new Response(req.getResource(), ActionType.GET_ALL_BY_SUBSCRIBER_ID,
					Response.ResponseStatus.SUCCESS, null, subOrders));
			return;
		}
		client.sendToClient(new Response(req.getResource(), ActionType.GET_ALL_BY_SUBSCRIBER_ID,
				Response.ResponseStatus.ERROR, "Can not find any history", null));

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

		if (!isSpaceAvailable(order.getOrderDate(), guests)) {
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
			} else {
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
				System.out.println(finalCustomer.toString());
				System.out.println(order.toString());
				EmailService.sendConfirmation(finalCustomer, order);
				System.out.println(EmailService.getContent());
				System.out.println("FROM SERVER CUS ID: " + finalCustomer.getCustomerId());
				order.setCustomer(finalCustomer);
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

	private boolean handleCheckAvailability(Request req, ConnectionToClient client) throws IOException {
		try {
			Order requestedOrder = (Order) req.getPayload();
			java.util.Date orderDate = requestedOrder.getOrderDate();
			int guests = requestedOrder.getNumberOfGuests();

			if (tabledao.countSuitableTables(guests) == 0) {
				client.sendToClient(new Response(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY,
						Response.ResponseStatus.ERROR, "No table exists for " + guests + " guests.", null));
				return false;
			}
			boolean isAvailable = isSpaceAvailable(orderDate, guests);

			List<TimeSlotStatus> timeSlots = checkAvailability(orderDate, guests);
			if (isAvailable) {
				client.sendToClient(new Response(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY,
						Response.ResponseStatus.SUCCESS, "Table is available.", timeSlots));
				return true;
			} else {
				client.sendToClient(new Response(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY,
						Response.ResponseStatus.ERROR, "The restaurant is full at this time.", timeSlots));
				return false;
			}

		} catch (SQLException e) {
			client.sendToClient(new Response(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY,
					Response.ResponseStatus.DATABASE_ERROR, "Database error checking availability.", null));
			return false;
		}
	}

	public List<TimeSlotStatus> checkAvailability(Date date, int guests) throws SQLException, IOException {

		List<TimeSlotStatus> results = new ArrayList<>();

		List<String> allSlots = getAvailabilityOptions(date);
		if (allSlots == null)
			return new ArrayList<>();

		LocalDate localDate = new java.sql.Date(date.getTime()).toLocalDate();
		for (String slotStr : allSlots) {

			LocalTime timeSlot = LocalTime.parse(slotStr);
			LocalDateTime ldt = LocalDateTime.of(localDate, timeSlot);
			java.sql.Timestamp specificTimeToCheck = java.sql.Timestamp.valueOf(ldt);

			boolean isFull = !isSpaceAvailable(specificTimeToCheck, guests);

			results.add(new TimeSlotStatus(slotStr, isFull));
		}

		return results;
	}

	private List<String> getAvailabilityOptions(Date dateOrder) throws SQLException, IOException {

		LocalDate date = new java.sql.Date(dateOrder.getTime()).toLocalDate();
		LocalDate today = LocalDate.now();

		if (date.isBefore(today)) {
			return new ArrayList<>();
		}
		OpeningHours dayHours = businessHourDao.getHoursForDate(dateOrder);
		List<String> options = new ArrayList<>();
		if (dayHours == null || dayHours.isClosed()) {
			System.out.println("Restaurant is closed.");
			return null;
		}

		LocalTime currentTime = dayHours.getOpenTime().toLocalTime();
		LocalTime lastSeatingTime = dayHours.getCloseTime().toLocalTime().minusHours(2);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		LocalTime nowTime = LocalTime.now();

		while (!currentTime.isAfter(lastSeatingTime)) {

			boolean isPastTimeToday = date.equals(today) && currentTime.isBefore(nowTime);

			if (!isPastTimeToday) {
				String timeStr = currentTime.format(formatter);
				options.add(timeStr);
			}
			currentTime = currentTime.plusMinutes(30);
		}

		return options;
	}

	private boolean isSpaceAvailable(java.util.Date date, int guests) throws SQLException {
		int totalTablesForMe = tabledao.countSuitableTables(guests);
		int activeOrdersForMe = orderdao.countActiveOrdersInTimeRange(date, guests);

		if (totalTablesForMe - activeOrdersForMe <= 0) {
			return false;
		}

		List<Integer> allSizes = tabledao.getAllTableCapacities();
		if (allSizes != null) {
			for (int size : allSizes) {
				if (size >= guests)
					continue;

				int totalAtThisSize = tabledao.countSuitableTables(size);

				int activeAtThisSize = orderdao.countActiveOrdersInTimeRange(date, size);

				int demand = activeAtThisSize + 1;

				if (totalAtThisSize < demand) {
					return false;
				}
			}
		}
		if (guests != 0) {
			List<Integer> table_list = tabledao.getAllTableCapacities();
			List<Integer> orders = orderdao.getActiveOrdersInTimeRange(date);
			List<Integer> futerOrder = new ArrayList<>();
			orders.add(guests);
			if (!isValidOrder(table_list, orders, futerOrder)) {
				return false;
			}
			orders.remove(orders.size() - 1);
		}

		return true;

	}

	private boolean isValidOrder(List<Integer> table_list, List<Integer> orders, List<Integer> futerOrder) {
		if (table_list.size() < orders.size()) {
			System.out.println("The size of lists are different!!");
			return false;
		}

		table_list.sort(Comparator.reverseOrder());// 2
		orders.sort(Comparator.reverseOrder());// 2
		int size = 0;
		for (int i = 0; i < orders.size(); i++) {
			if (!(table_list.get(size) >= orders.get(i))) {
				return false;
			}

			size++;// 1//2
			if (size >= table_list.size()) {
				break;
			}
		}

		return true;
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

	private void handelUpdateCheckOut(Request req, ConnectionToClient client) throws SQLException, IOException {
		Order order = (Order) req.getPayload();
		boolean updateOrder = orderdao.updateOrderCheckOut(order.getOrderNumber(), order.getTotalPrice(),
				Order.OrderStatus.PAID);
		if (!updateOrder) {
			client.sendToClient(new Response(req.getResource(), ActionType.UPDATE, Response.ResponseStatus.ERROR,
					"Error: CANOT update order.", null));
			/// need to get email from customer table

			return;
		}
		boolean updateTable = tabledao.updateTableStatus(order.getTableNumber(), 0);
		if (!updateTable) {
			client.sendToClient(new Response(req.getResource(), ActionType.UPDATE, Response.ResponseStatus.ERROR,
					"Error: CANOT update table.", null));
			/// need to get email from customer table
			return;
		}
		client.sendToClient(new Response(req.getResource(), ActionType.UPDATE, Response.ResponseStatus.SUCCESS,
				"Order updated.", order));
		sendOrdersToAllClients();

	}

	private void handleDelete(Request req, ConnectionToClient client) throws SQLException, IOException {
		if (req.getId() == null)
			return;

		Order order = orderdao.getOrder(req.getId());
		if (order != null && order.getOrderStatus() == Order.OrderStatus.SEATED) {
			if (order.getTableNumber() != null) {
				tabledao.updateTableStatus(order.getTableNumber(), 0);
			}
		}

		if (orderdao.updateOrderStatus(req.getId(), OrderStatus.CANCELLED)) {
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

						tabledao.updateTableStatus(tableNum, 1);

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
					tabledao.updateTableStatus(order.getTableNumber(), 0);
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
			Customer tempCustomer = new Customer(order.getCustomer().getCustomerId(), order.getCustomer().getName(),
					order.getCustomer().getPhoneNumber(), order.getCustomer().getEmail());
			System.out.println(order.getCustomer().getEmail());

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
		List<Map<String, Object>> orders = orderdao.getAllOrdersWithCustomers();
		Router.sendToAllClients(
				new Response(ResourceType.ORDER, ActionType.GET_ALL, Response.ResponseStatus.SUCCESS, null, orders));
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