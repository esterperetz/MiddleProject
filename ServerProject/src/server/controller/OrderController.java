
package server.controller;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import DAO.OrderDAO;
import DAO.TableDAO;
import Entities.*;
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

	private void handleGetUserOrders(Request req, ConnectionToClient client) throws SQLException, IOException {
		int subId;
		if (req.getPayload() != null) {
			if (req.getPayload() instanceof Integer) {
				subId = (Integer) req.getPayload();
				List<Order> history = orderdao.getOrdersBySubscriberId(subId);

				client.sendToClient(new Response(req.getResource(), ActionType.GET_USER_ORDERS,
						Response.ResponseStatus.SUCCESS, null, history));
			} else {
				client.sendToClient(new Response(req.getResource(), ActionType.GET_USER_ORDERS,
						Response.ResponseStatus.ERROR, "Error: Subscriber ID is missing.", null));
			}
		} else
			System.out.println("Error");
	}

	private void handleGetAll(Request req, ConnectionToClient client) throws SQLException, IOException {
		List<Order> orders = orderdao.getAllOrders();
		client.sendToClient(new Response(req.getResource(), ActionType.GET_ALL, Response.ResponseStatus.SUCCESS,
				null, orders));
	}

	private void handleGetAllBySubscriberId(Request req, ConnectionToClient client) throws SQLException, IOException {
		if(req.getId() == null) {
			client.sendToClient(new Response(req.getResource(), ActionType.GET_ALL_BY_SUBSCRIBER_ID,Response.ResponseStatus.ERROR,"Error: GET_BY_ID requires an ID.",null));
			return;
		}
		List<Order> subOrders = orderdao.getOrdersBySubscriberId(req.getId());
		client.sendToClient(new Response(req.getResource(), ActionType.GET_ALL_BY_SUBSCRIBER_ID,
				Response.ResponseStatus.SUCCESS, null, subOrders));
	}

	private void handleGetById(Request req, ConnectionToClient client) throws SQLException, IOException {
		if (req.getId() == null) {
			client.sendToClient(new Response(req.getResource(), ActionType.GET_ALL,
					Response.ResponseStatus.ERROR, "Error: GET_BY_ID requires an ID.", null));
			return;
		}
		Order order = orderdao.getOrder(req.getId());
		client.sendToClient(new Response(req.getResource(), ActionType.GET_BY_ID,
				Response.ResponseStatus.SUCCESS, null, order));
	}

	private void handleCreate(Request req, ConnectionToClient client) throws SQLException, IOException {
		if (req.getPayload() == null || !(req.getPayload() instanceof Order)) {
			client.sendToClient(new Response(req.getResource(), ActionType.CREATE,
					Response.ResponseStatus.ERROR, "Error: CREATE action requires an Order payload.", null));
			return;
		}
		Order o = (Order) req.getPayload();
		
		// Validate Mandatory Identification
		if (o.getClientEmail() == null || o.getClientPhone() == null) {
			client.sendToClient(new Response(req.getResource(), ActionType.CREATE,
					Response.ResponseStatus.ERROR, "Error: Identification details are mandatory.", null));
			return;
		}
		
		// Generate a random 4-digit confirmation code for the customer
		int generatedCode = 1000 + (int) (Math.random() * 9000);
		o.setConfirmationCode(generatedCode);
		boolean created = orderdao.createOrder(o);

		if (created) {
			client.sendToClient(new Response(req.getResource(), ActionType.CREATE,
					Response.ResponseStatus.SUCCESS, "Success: Order created.", null));
			sendOrdersToAllClients();
		} else {
			client.sendToClient(new Response(req.getResource(), ActionType.CREATE,
					Response.ResponseStatus.ERROR, "Error: Failed to create order.", null));
		}
	}

	private void handleUpdate(Request req, ConnectionToClient client) throws SQLException, IOException {
		if (req.getPayload() == null || !(req.getPayload() instanceof Order)) {
			client.sendToClient(new Response(req.getResource(), ActionType.UPDATE,
					Response.ResponseStatus.ERROR, "Error: UPDATE action requires an Order payload.", null));
			return;
		}
		Order updatedOrder = (Order) req.getPayload();
		boolean updated = orderdao.updateOrder(updatedOrder);
		if (updated) {
			client.sendToClient(new Response(req.getResource(), ActionType.UPDATE,
					Response.ResponseStatus.SUCCESS, "Success: Order updated.", null));
			sendOrdersToAllClients();
		} else {
			client.sendToClient(new Response(req.getResource(), ActionType.UPDATE,
					Response.ResponseStatus.ERROR, "Error: Failed to update order.", null));
		}
	}

	private void handleDelete(Request req, ConnectionToClient client) throws SQLException, IOException {
		if (req.getId() == null) {
			client.sendToClient(new Response(req.getResource(), ActionType.DELETE,
					Response.ResponseStatus.ERROR, "Error: DELETE requires an ID.", null));
			return;
		}
		boolean deleted = orderdao.deleteOrder(req.getId());
		if (deleted) {
			client.sendToClient(new Response(req.getResource(), ActionType.DELETE,
					Response.ResponseStatus.SUCCESS, "Success: Order deleted.", null));
			sendOrdersToAllClients();
		} else {
			client.sendToClient(new Response(req.getResource(), ActionType.DELETE,
					Response.ResponseStatus.ERROR, "Error: Failed to delete order.", null));
		}
	}

	private void handleCheckAvailability(Request req, ConnectionToClient client) throws SQLException, IOException {
		if (req.getPayload() instanceof Order) {
			Order requestedOrder = (Order) req.getPayload();
			// Check how many tables can physically fit this many guests
			int guests = requestedOrder.getNumberOfGuests();

			try {
				int totalSuitableTables = tabledao.countSuitableTables(guests);
				
				// If no table in the restaurant is big enough for this group
				if (totalSuitableTables == 0) {
					client.sendToClient(new Response(req.getResource(), ActionType.DELETE,
							Response.ResponseStatus.ERROR,
							"Error: No tables in the restaurant can accommodate " + guests + " guests.", null));
					return;
				}

				int existingOrdersInTimeRange = orderdao
						.countActiveOrdersInTimeRange(requestedOrder.getOrderDate(), guests);

				int availableTables = totalSuitableTables - existingOrdersInTimeRange;

				if (availableTables > 0) {
					client.sendToClient(new Response(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY,
							Response.ResponseStatus.SUCCESS, "Available tables", true));
				} else {
					client.sendToClient(new Response(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY,
							Response.ResponseStatus.SUCCESS, "No available tables", false));
				}

			} catch (SQLException e) {
				e.printStackTrace();
				client.sendToClient(new Response(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY,
						Response.ResponseStatus.DATABASE_ERROR,
						"Database Error during availability check:" + e.getMessage(), false));
			}
		}
	}

	private void handleIdentifyAtTerminal(Request req, ConnectionToClient client) throws SQLException, IOException {
		if (req.getId() != null) {
			Order order_at_bistro = orderdao.getByConfirmationCode(req.getId());

			if (order_at_bistro != null && order_at_bistro.getOrderStatus() == Order.OrderStatus.APPROVED) {
				long now = new Date().getTime();
				long orderTime = order_at_bistro.getOrderDate().getTime();
				long diffInMinutes = (now - orderTime) / 60000;

				if (diffInMinutes > 15) {
					order_at_bistro.setOrderStatus(Order.OrderStatus.CANCELLED);
					orderdao.updateOrder(order_at_bistro);
					client.sendToClient(new Response(ResourceType.ORDER, ActionType.IDENTIFY_AT_TERMINAL,
							Response.ResponseStatus.DATABASE_ERROR, "Order Expired (15 min late)", null));
				} else {
					order_at_bistro.setOrderStatus(Order.OrderStatus.SEATED);

					if (order_at_bistro.getSubscriberId() != null) {
						System.out.println("Subscriber " + order_at_bistro.getSubscriberId()
								+ " confirmed. 10% discount flag is set.");
					}

					orderdao.updateOrder(order_at_bistro);
					client.sendToClient(new Response(ResourceType.ORDER, ActionType.IDENTIFY_AT_TERMINAL,
							Response.ResponseStatus.SUCCESS, null, order_at_bistro.getOrderNumber()));
				}
				sendOrdersToAllClients();
			} else {
				client.sendToClient(new Response(ResourceType.ORDER, ActionType.IDENTIFY_AT_TERMINAL,
						Response.ResponseStatus.NOT_FOUND, null, false));
			}
		}
	}

	private void handlePayBill(Request req, ConnectionToClient client) throws SQLException, IOException {
		if (req.getId() != null) {
			Order orderToPay = orderdao.getOrder(req.getId());

			if (orderToPay != null && orderToPay.getOrderStatus() == Order.OrderStatus.SEATED) {

				double finalAmount = orderToPay.getTotalPrice();

				if (orderToPay.getSubscriberId() != null) {
					finalAmount = finalAmount * 0.9;
					System.out.println("Subscriber discount applied (10%). Original: "
							+ orderToPay.getTotalPrice() + ", Final: " + finalAmount);
				}
				
				// Update the order object with the final calculated price and status
				orderToPay.setTotalPrice(finalAmount);
				orderToPay.setOrderStatus(Order.OrderStatus.PAID);

				if (orderdao.updateOrder(orderToPay)) {
					// Return success to the terminal/client with the final amount to display
					client.sendToClient(new Response(ResourceType.ORDER, ActionType.PAY_BILL,
							Response.ResponseStatus.SUCCESS, "Order_number:" + orderToPay.getOrderNumber(),
							finalAmount));

					sendOrdersToAllClients();
				} else {
					client.sendToClient(
							new Response(ResourceType.ORDER, ActionType.PAY_BILL, Response.ResponseStatus.ERROR,
									"Error: Failed to update payment in database.", null));
				}
			} else {
				client.sendToClient(
						new Response(ResourceType.ORDER, ActionType.PAY_BILL, Response.ResponseStatus.ERROR,
								"Error: Order not found or not currently seated.", null));
			}
		}
	}

	private void sendOrdersToAllClients() {
		try {
			List<Order> orders = orderdao.getAllOrders();
			Response updateMsg = new Response(ResourceType.ORDER, ActionType.GET_ALL, Response.ResponseStatus.SUCCESS,
					null, orders);
			Router.sendToAllClients(updateMsg);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}