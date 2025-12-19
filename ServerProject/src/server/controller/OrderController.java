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
			case GET_USER_ORDERS:
				int subId;
				if (req.getPayload() != null) {
					if (req.getPayload() instanceof Integer) {
						subId = (Integer) req.getPayload();
						// called function from OrderDao

						List<Order> history = orderdao.getOrdersBySubscriberId(subId);

						// send to client what he send requast
						client.sendToClient(new Response(req.getResource(), ActionType.GET_USER_ORDERS,
								Response.ResponseStatus.SUCCESS, null, history));
					} else {
						client.sendToClient(new Response(req.getResource(), ActionType.GET_USER_ORDERS,
								Response.ResponseStatus.ERROR, "Error: Subscriber ID is missing.", null));
					}
				} else
					System.out.println("Error");

				break;
			case GET_ALL:
				List<Order> orders = orderdao.getAllOrders();
				client.sendToClient(new Response(req.getResource(), ActionType.GET_ALL, Response.ResponseStatus.SUCCESS,
						null, orders));
				break;
			
			case GET_ALL_BY_SUBSCRIBER_ID:
				if(req.getId() == null) {
					client.sendToClient(new Response(req.getResource(), ActionType.GET_ALL_BY_SUBSCRIBER_ID,Response.ResponseStatus.ERROR,"Error: GET_BY_ID requires an ID.",null));
					break;
				}
				List<Order> subOrders = orderdao.getOrdersBySubscriberId(req.getId());
				client.sendToClient(new Response(req.getResource(), ActionType.GET_ALL_BY_SUBSCRIBER_ID,
						Response.ResponseStatus.SUCCESS, null, subOrders));
				break;
				
				
			case GET_BY_ID:
				if (req.getId() == null) {
					client.sendToClient(new Response(req.getResource(), ActionType.GET_ALL,
							Response.ResponseStatus.ERROR, "Error: GET_BY_ID requires an ID.", null));
					break;
				}
				Order order = orderdao.getOrder(req.getId());
				client.sendToClient(new Response(req.getResource(), ActionType.GET_BY_ID,
						Response.ResponseStatus.SUCCESS, null, order));
				break;

			case CREATE:
				if (req.getPayload() == null || !(req.getPayload() instanceof Order)) {
					client.sendToClient(new Response(req.getResource(), ActionType.CREATE,
							Response.ResponseStatus.ERROR, "Error: CREATE action requires an Order payload.", null));
					break;
				}
				Order o = (Order) req.getPayload();

				// Validate Mandatory Identification
				if (o.getClient_email() == null || o.getClient_Phone() == null) {
					client.sendToClient(new Response(req.getResource(), ActionType.CREATE,
							Response.ResponseStatus.ERROR, "Error: Identification details are mandatory.", null));
					break;
				}
				// Generate a random 4-digit confirmation code for the customer
				int generatedCode = 1000 + (int) (Math.random() * 9000);
				o.setConfirmation_code(generatedCode);
				boolean created = orderdao.createOrder(o);

				if (created) {
					client.sendToClient(new Response(req.getResource(), ActionType.CREATE,
							Response.ResponseStatus.SUCCESS, "Success: Order created.", null));
					sendOrdersToAllClients();
				} else {
					client.sendToClient(new Response(req.getResource(), ActionType.CREATE,
							Response.ResponseStatus.ERROR, "Error: Failed to create order.", null));
				}
				break;

			case UPDATE:
				if (req.getPayload() == null || !(req.getPayload() instanceof Order)) {
					client.sendToClient(new Response(req.getResource(), ActionType.UPDATE,
							Response.ResponseStatus.ERROR, "Error: UPDATE action requires an Order payload.", null));
					break;
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
				break;

			case DELETE:
				if (req.getId() == null) {
					client.sendToClient(new Response(req.getResource(), ActionType.DELETE,
							Response.ResponseStatus.ERROR, "Error: DELETE requires an ID.", null));
					break;
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
							client.sendToClient(new Response(req.getResource(), ActionType.DELETE,
									Response.ResponseStatus.ERROR,
									"Error: No tables in the restaurant can accommodate " + guests + " guests.", null));
							break;
						}

						// Check how many of those suitable tables are already booked
						int existingOrdersInTimeRange = orderdao
								.countActiveOrdersInTimeRange(requestedOrder.getOrder_date(), guests);

						// Calculate remaining availability
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
				break;
			case IDENTIFY_AT_TERMINAL:
				if (req.getId() != null) {
					Order order_at_bistro = orderdao.getByConfirmationCode(req.getId()); // only if approved

					if (order_at_bistro != null && order_at_bistro.getOrder_status() == Order.OrderStatus.APPROVED) {
						long now = new Date().getTime();
						long orderTime = order_at_bistro.getOrder_date().getTime();
						long diffInMinutes = (now - orderTime) / 60000;

						// 15-minute rule enforcement
						if (diffInMinutes > 15) {
							order_at_bistro.setOrder_status(Order.OrderStatus.CANCELLED);
							orderdao.updateOrder(order_at_bistro);
							client.sendToClient(new Response(ResourceType.ORDER, ActionType.IDENTIFY_AT_TERMINAL,
									Response.ResponseStatus.DATABASE_ERROR, "Order Expired (15 min late)", null));
						} else {
							// Success - Move to SEATED status
							order_at_bistro.setOrder_status(Order.OrderStatus.SEATED);

							// Subscriber recognition for 10% discount later
							if (order_at_bistro.getSubscriber_id() != null) {
								System.out.println("Subscriber " + order_at_bistro.getSubscriber_id()
										+ " confirmed. 10% discount flag is set.");
							}

							orderdao.updateOrder(order_at_bistro);
							client.sendToClient(new Response(ResourceType.ORDER, ActionType.IDENTIFY_AT_TERMINAL,
									Response.ResponseStatus.SUCCESS, null, order_at_bistro.getOrder_number()));
						}
						sendOrdersToAllClients();
					} else {
						client.sendToClient(new Response(ResourceType.ORDER, ActionType.IDENTIFY_AT_TERMINAL,
								Response.ResponseStatus.NOT_FOUND, null, false));
					}
				}
			case PAY_BILL:
				if (req.getId() != null) {
					Order orderToPay = orderdao.getOrder(req.getId());

					// Verification: Only a SEATED order can be paid
					if (orderToPay != null && orderToPay.getOrder_status() == Order.OrderStatus.SEATED) {

						double finalAmount = orderToPay.getTotal_price();

						// Apply 10% Subscriber Discount
						if (orderToPay.getSubscriber_id() != null) {
							finalAmount = finalAmount * 0.9;
							System.out.println("Subscriber discount applied (10%). Original: "
									+ orderToPay.getTotal_price() + ", Final: " + finalAmount);
						}

						// Update the order object with the final calculated price and status
						orderToPay.setTotal_price(finalAmount);
						orderToPay.setOrder_status(Order.OrderStatus.PAID);

						if (orderdao.updateOrder(orderToPay)) {
							// Return success to the terminal/client with the final amount to display
							client.sendToClient(new Response(ResourceType.ORDER, ActionType.PAY_BILL,
									Response.ResponseStatus.SUCCESS, "Order_number:" + orderToPay.getOrder_number(),
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