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
			case IDENTIFY_AT_TERMINAL:
			    if (req.getId() != null) {
			        Order order_at_bistro = orderdao.getByConfirmationCode(req.getId()); //only if approved
			        
			        if (order_at_bistro != null && order_at_bistro.getStatus() == Order.OrderStatus.APPROVED) {
			            long now = new Date().getTime();
			            long orderTime = order_at_bistro.getOrder_date().getTime();
			            long diffInMinutes = (now - orderTime) / 60000;

			            // 15-minute rule enforcement 
			            if (diffInMinutes > 15) {
			                order_at_bistro.setStatus(Order.OrderStatus.CANCELLED);
			                orderdao.updateOrder(order_at_bistro);
			                client.sendToClient(new Request(ResourceType.ORDER, ActionType.IDENTIFY_AT_TERMINAL, null, "Order Expired (15 min late)"));
			            } else {
			                // Success - Move to SEATED status
			                order_at_bistro.setStatus(Order.OrderStatus.SEATED);
			                
			                // Subscriber recognition for 10% discount later 
			                if (order_at_bistro.getSubscriber_id() != null) {
			                    System.out.println("Subscriber " + order_at_bistro.getSubscriber_id() + " confirmed. 10% discount flag is set.");
			                }
			                
			                orderdao.updateOrder(order_at_bistro);
			                client.sendToClient(new Request(ResourceType.ORDER, ActionType.IDENTIFY_AT_TERMINAL, order_at_bistro.getOrder_number(), true));
			            }
			            sendOrdersToAllClients();
			        } else {
			            client.sendToClient(new Request(ResourceType.ORDER, ActionType.IDENTIFY_AT_TERMINAL, null, false));
			        }
			    }
			case PAY_BILL:
			    if (req.getId() != null) {
			        Order orderToPay = orderdao.getOrder(req.getId());
			        
			        // Verification: Only a SEATED order can be paid
			        if (orderToPay != null && orderToPay.getStatus() == Order.OrderStatus.SEATED) {
			            
			            double finalAmount = orderToPay.getTotal_price();

			            // Apply 10% Subscriber Discount
			            if (orderToPay.getSubscriber_id() != null) {
			                finalAmount = finalAmount * 0.9;
			                System.out.println("Subscriber discount applied (10%). Original: " + orderToPay.getTotal_price() + ", Final: " + finalAmount);
			            }

			            // Update the order object with the final calculated price and status
			            orderToPay.setTotal_price(finalAmount);
			            orderToPay.setStatus(Order.OrderStatus.PAID);
			            
			            if (orderdao.updateOrder(orderToPay)) {
			                // Return success to the terminal/client with the final amount to display
			                client.sendToClient(new Request(ResourceType.ORDER, ActionType.PAY_BILL, orderToPay.getOrder_number(), finalAmount));
			                
			                sendOrdersToAllClients();
			            } else {
			                client.sendToClient(new Request(ResourceType.ORDER, ActionType.PAY_BILL, null, "Error: Failed to update payment in database."));
			            }
			        } else {
			            client.sendToClient(new Request(ResourceType.ORDER, ActionType.PAY_BILL, null, "Error: Order not found or not currently seated."));
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