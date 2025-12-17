package server.controller;

import java.sql.SQLException;
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
			client.sendToClient("Error: Incorrect resource type requested. Expected ORDER.");
			return;
		}

		try {
			switch (req.getAction()) {
			case GET_ALL:
				List<Order> orders = orderdao.getAllOrders();
				client.sendToClient(new Request(req.getResource(), ActionType.GET_ALL, null, orders));
				break;

			case GET_BY_ID:
				if (req.getId() == null) {
					client.sendToClient("Error: GET_BY_ID requires an ID.");
					break;
				}
				Order order = orderdao.getOrder(req.getId());
				client.sendToClient(new Request(req.getResource(), ActionType.GET_BY_ID, req.getId(), order));
				break;

			case CREATE:
				if (req.getPayload() == null || !(req.getPayload() instanceof Order)) {
					client.sendToClient("Error: CREATE action requires an Order payload.");
					break;
				}
				Order o = (Order) req.getPayload();

				// Validate Mandatory Identification
				if (o.getIdentification_details() == null || o.getIdentification_details().isEmpty()) {
					client.sendToClient("Error: Identification details are mandatory.");
					break;
				}
				// Generate a random 4-digit confirmation code for the customer
				int generatedCode = 1000 + (int) (Math.random() * 9000);
				o.setConfirmation_code(generatedCode);
				boolean created = orderdao.createOrder(o);

				if (created) {
					client.sendToClient("Success: Order created.");
					sendOrdersToAllClients();
				} else {
					client.sendToClient("Error: Failed to create order.");
				}
				break;

			case UPDATE:
				if (req.getPayload() == null || !(req.getPayload() instanceof Order)) {
					client.sendToClient("Error: UPDATE action requires an Order payload.");
					break;
				}
				Order updatedOrder = (Order) req.getPayload();
				boolean updated = orderdao.updateOrder(updatedOrder);
				if (updated) {
					client.sendToClient("Success: Order updated.");
					sendOrdersToAllClients();
				} else {
					client.sendToClient("Error: Failed to update order.");
				}
				break;

			case DELETE:
				if (req.getId() == null) {
					client.sendToClient("Error: DELETE requires an ID.");
					break;
				}
				boolean deleted = orderdao.deleteOrder(req.getId());
				if (deleted) {
					client.sendToClient("Success: Order deleted.");
					sendOrdersToAllClients();
				} else {
					client.sendToClient("Error: Failed to delete order.");
				}
				break;

			case CHECK_AVAILABILITY:
				if (req.getPayload() instanceof Order) {
					Order requestedOrder = (Order) req.getPayload();
					int guests = requestedOrder.getNumber_of_guests();

					try {
						// Check how many tables can physically fit this many guests
						int totalSuitableTables = tabledao.countSuitableTables(guests);

						// If no table in the restaurant is big enough for this group
						if (totalSuitableTables == 0) {
							client.sendToClient(
									"Error: No tables in the restaurant can accommodate " + guests + " guests.");
							break;
						}

						// Check how many of those suitable tables are already booked
						int existingOrdersInTimeRange = orderdao
								.countActiveOrdersInTimeRange(requestedOrder.getOrder_date(), guests);

						// Calculate remaining availability
						int availableTables = totalSuitableTables - existingOrdersInTimeRange;

						if (availableTables > 0) {

							client.sendToClient(
									new Request(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY, null, true));
						} else {

							client.sendToClient(
									new Request(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY, null, false));
						}

					} catch (SQLException e) {
						e.printStackTrace();
						client.sendToClient("Database Error during availability check: " + e.getMessage());
					}
				}
				break;
			default:
				client.sendToClient("Unsupported action: " + req.getAction());
			}
		} catch (SQLException e) {
			e.printStackTrace();
			client.sendToClient("Database error: " + e.getMessage());
		}
	}

	private void sendOrdersToAllClients() {
		try {
			List<Order> orders = orderdao.getAllOrders();
			Request updateMsg = new Request(ResourceType.ORDER, ActionType.GET_ALL, null, orders);
			Router.sendToAllClients(updateMsg);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}