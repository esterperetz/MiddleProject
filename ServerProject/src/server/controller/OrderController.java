package server.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import DAO.OrderDAO;
import DAO.TableDAO;
import Entities.ActionType;
import Entities.Order;
import Entities.Request;
import Entities.ResourceType;
import Entities.Response;
import ocsf.server.ConnectionToClient;

public class OrderController {

	private final OrderDAO orderdao = new OrderDAO();
	private final TableDAO tabledao = new TableDAO();

	public void handle(Request req, ConnectionToClient client) {
		if (req.getResource() != ResourceType.ORDER) {
			try {
				client.sendToClient(new Response(req.getResource(), ActionType.GET_ALL, Response.ResponseStatus.ERROR,
						"Error: Expected ORDER.", null));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		try {
			switch (req.getAction()) {
			case GET_ALL:
				getAllOrders(req, client);
				break;
			case GET_BY_ID:
				getOrderById(req, client);
				break;
			case CREATE:
				createOrder(req, client);
				break;
			case UPDATE:
				updateOrder(req, client);
				break;
			case DELETE:
				deleteOrder(req, client);
				break;
			case CHECK_AVAILABILITY:
				checkAvailability(req, client);
				break;
			case IDENTIFY_AT_TERMINAL:
				identifyAtTerminal(req, client);
				break;
			case PAY_BILL:
				payBill(req, client);
				break;
			default:
				client.sendToClient(new Response(req.getResource(), req.getAction(), Response.ResponseStatus.ERROR,
						"Unsupported action", null));
			}
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
	}

	private void getAllOrders(Request req, ConnectionToClient client) throws IOException, SQLException {
		List<Order> orders = orderdao.getAllOrders();
		client.sendToClient(
				new Response(req.getResource(), ActionType.GET_ALL, Response.ResponseStatus.SUCCESS, null, orders));
	}

	private void getOrderById(Request req, ConnectionToClient client) throws IOException, SQLException {
		if (req.getId() == null)
			return;
		Order order = orderdao.getOrder(req.getId());
		client.sendToClient(
				new Response(req.getResource(), ActionType.GET_BY_ID, Response.ResponseStatus.SUCCESS, null, order));
	}

	private void createOrder(Request req, ConnectionToClient client) throws IOException, SQLException {
		Order o = (Order) req.getPayload();
		int generatedCode = 1000 + (int) (Math.random() * 9000);
		o.setConfirmation_code(generatedCode);
		if (orderdao.createOrder(o)) {
			client.sendToClient(new Response(req.getResource(), ActionType.CREATE, Response.ResponseStatus.SUCCESS,
					"Order created", null));
			sendOrdersToAllClients();
		}
	}

	private void updateOrder(Request req, ConnectionToClient client) throws IOException, SQLException {
		Order updatedOrder = (Order) req.getPayload();
		if (orderdao.updateOrder(updatedOrder)) {
			client.sendToClient(new Response(req.getResource(), ActionType.UPDATE, Response.ResponseStatus.SUCCESS,
					"Order updated", null));
			sendOrdersToAllClients();
		}
	}

	private void deleteOrder(Request req, ConnectionToClient client) throws IOException, SQLException {
		if (orderdao.deleteOrder(req.getId())) {
			client.sendToClient(new Response(req.getResource(), ActionType.DELETE, Response.ResponseStatus.SUCCESS,
					"Order deleted", null));
			sendOrdersToAllClients();
		}
	}

	private void checkAvailability(Request req, ConnectionToClient client) throws IOException, SQLException {
		Order requestedOrder = (Order) req.getPayload();
		int totalSuitableTables = tabledao.countSuitableTables(requestedOrder.getNumber_of_guests()); //amount of possible tables
		int existingOrders = orderdao.countActiveOrdersInTimeRange(requestedOrder.getOrder_date(), //amount of orders in time-frame with at least int guests size.
				requestedOrder.getNumber_of_guests());
		boolean isAvailable = (totalSuitableTables - existingOrders) > 0;
		client.sendToClient(new Response(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY,
				Response.ResponseStatus.SUCCESS, null, isAvailable));
	}

	private void identifyAtTerminal(Request req, ConnectionToClient client) throws IOException, SQLException {
		if (req.getId() == null)
			return;
		Order orderAtBistro = orderdao.getByConfirmationCode(req.getId());

		if (orderAtBistro != null && orderAtBistro.getOrder_status() == Order.OrderStatus.APPROVED) {
			long diffInMinutes = (new Date().getTime() - orderAtBistro.getOrder_date().getTime()) / 60000;

			if (diffInMinutes > 15) { // visitor is late
				orderAtBistro.setOrder_status(Order.OrderStatus.CANCELLED);
				orderdao.updateOrder(orderAtBistro);
				client.sendToClient(new Response(ResourceType.ORDER, ActionType.IDENTIFY_AT_TERMINAL,
						Response.ResponseStatus.ERROR, "Expired", null));
			} else {
				// Find physical table
				Integer tableNum = tabledao.findAvailableTable(orderAtBistro.getNumber_of_guests());
				if (tableNum != null) {
					//updates order parameters//
					tabledao.updateTableStatus(tableNum, true); 
					orderAtBistro.setTable_number(tableNum);
					orderAtBistro.setArrivalTime(new Date());
					orderAtBistro.setOrder_status(Order.OrderStatus.SEATED);
					orderdao.updateOrder(orderAtBistro);
					client.sendToClient(new Response(ResourceType.ORDER, ActionType.IDENTIFY_AT_TERMINAL,
							Response.ResponseStatus.SUCCESS, "Table assigned: " + tableNum,
							orderAtBistro.getOrder_number()));
				} else {
				
					client.sendToClient(new Response(ResourceType.ORDER, ActionType.IDENTIFY_AT_TERMINAL,
							Response.ResponseStatus.ERROR, "No table available yet. Please wait.", null));
				}
			}
			sendOrdersToAllClients();
		}
	}

	private void payBill(Request req, ConnectionToClient client) throws IOException, SQLException {
		if (req.getId() == null)
			return;
		Order orderToPay = orderdao.getOrder(req.getId());

		if (orderToPay != null && orderToPay.getOrder_status() == Order.OrderStatus.SEATED) {
			orderToPay.setOrder_status(Order.OrderStatus.PAID);
			orderToPay.setLeaveTime(new Date()); // Record leave time

			if (orderdao.updateOrder(orderToPay)) {
				if (orderToPay.getTable_number() != null) {
					tabledao.updateTableStatus(orderToPay.getTable_number(), false); //frees table
				}
				client.sendToClient(new Response(ResourceType.ORDER, ActionType.PAY_BILL,
						Response.ResponseStatus.SUCCESS, "Paid", orderToPay.getTotal_price()));
				sendOrdersToAllClients();
			}
		}
	}

	private void sendOrdersToAllClients() {
		try {
			Router.sendToAllClients(new Response(ResourceType.ORDER, ActionType.GET_ALL,
					Response.ResponseStatus.SUCCESS, null, orderdao.getAllOrders()));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}