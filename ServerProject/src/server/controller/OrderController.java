package server.controller;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import DAO.OrderDAO;
import DAO.TableDAO;
import entities.*;
import ocsf.server.ConnectionToClient;
import java.io.IOException;

public class OrderController {
	private final OrderDAO orderdao = new OrderDAO();
	private final TableDAO tabledao = new TableDAO();

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
		List<Order> orders = orderdao.getAllOrders();
		client.sendToClient(
				new Response(req.getResource(), ActionType.GET_ALL, Response.ResponseStatus.SUCCESS, null, orders));
	}

	private void handleGetAllBySubscriberId(Request req, ConnectionToClient client) throws SQLException, IOException {
		if (req.getId() == null) {
			client.sendToClient(new Response(req.getResource(), ActionType.GET_ALL_BY_SUBSCRIBER_ID,
					Response.ResponseStatus.ERROR, "Error: ID missing.", null));
			return;
		}
		List<Order> subOrders = orderdao.getOrdersBySubscriberId(req.getId());
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

	private void handleCreate(Request req, ConnectionToClient client) throws SQLException, IOException {
		Order o = (Order) req.getPayload();
		if (o.getClientEmail() == null || o.getClientPhone() == null) {
			client.sendToClient(new Response(req.getResource(), ActionType.CREATE, Response.ResponseStatus.ERROR,
					"Error: Identification mandatory.", null));
			return;
		}
		int generatedCode = 1000 + (int) (Math.random() * 9000);
		o.setConfirmationCode(generatedCode);
		if (orderdao.createOrder(o)) {
			client.sendToClient(new Response(req.getResource(), ActionType.CREATE, Response.ResponseStatus.SUCCESS,
					"Order created.", null));
			sendOrdersToAllClients();
		}
	}

	private void handleUpdate(Request req, ConnectionToClient client) throws SQLException, IOException {
		Order updatedOrder = (Order) req.getPayload();
		if (orderdao.updateOrder(updatedOrder)) {
			client.sendToClient(new Response(req.getResource(), ActionType.UPDATE, Response.ResponseStatus.SUCCESS,
					"Order updated.", null));
			sendOrdersToAllClients();
		}
	}

	private void handleDelete(Request req, ConnectionToClient client) throws SQLException, IOException {
		if (orderdao.deleteOrder(req.getId())) {
			client.sendToClient(new Response(req.getResource(), ActionType.DELETE, Response.ResponseStatus.SUCCESS,
					"Order deleted.", null));
			sendOrdersToAllClients();
		}
	}

	private void handleCheckAvailability(Request req, ConnectionToClient client) throws SQLException, IOException {
		Order requestedOrder = (Order) req.getPayload();
		int guests = requestedOrder.getNumberOfGuests();

		// Count tables with enough seats in the restaurant [cite: 10, 25]
		int totalSuitableTables = tabledao.countSuitableTables(guests);

		if (totalSuitableTables == 0) {
			client.sendToClient(new Response(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY,
					Response.ResponseStatus.ERROR, "No suitable tables", null));
			return;
		}

		// Count active APPROVED/SEATED orders in the +/- 2 hour window
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

			if (diffInMinutes > 15) { // Late arrival rule 
				order.setOrderStatus(Order.OrderStatus.CANCELLED);
				orderdao.updateOrder(order);
				client.sendToClient(new Response(ResourceType.ORDER, ActionType.IDENTIFY_AT_TERMINAL,
						Response.ResponseStatus.ERROR, "Expired", null));
			} else {
				// Find a free physical table for immediate seating [cite: 40, 43]
				Integer tableNum = tabledao.findAvailableTable(order.getNumberOfGuests());
				if (tableNum != null) {
					order.setOrderStatus(Order.OrderStatus.SEATED);
					order.setArrivalTime(new Date());
					orderdao.updateOrder(order);

					// Mark table as occupied in DB [cite: 43]
					tabledao.updateTableStatus(tableNum, true);

					client.sendToClient(new Response(ResourceType.ORDER, ActionType.IDENTIFY_AT_TERMINAL,
							Response.ResponseStatus.SUCCESS, "Table assigned: " + tableNum, order.getOrderNumber()));
				} else {
					client.sendToClient(new Response(ResourceType.ORDER, ActionType.IDENTIFY_AT_TERMINAL,
							Response.ResponseStatus.ERROR, "No table ready yet", null));
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
			if (order.getSubscriberId() != null) //if he is a subscriber
				amount *= 0.9; // 10% Subscriber discount 

			order.setTotalPrice(amount);
			order.setOrderStatus(Order.OrderStatus.PAID);
			order.setLeavingTime(new Date());

			if (orderdao.updateOrder(order)) {
				// Note: Physical table status update should happen here
				client.sendToClient(new Response(ResourceType.ORDER, ActionType.PAY_BILL,
						Response.ResponseStatus.SUCCESS, "Paid", amount));
				sendOrdersToAllClients();
			}
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
}