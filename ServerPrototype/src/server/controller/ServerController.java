package server.controller;

import java.sql.SQLException;
import java.util.List;

import DAO.OrderDAO;
import DBConnection.DBConnection;
import Entities.Order;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import server.gui.ServerViewController;
//This class handles the server. It talks with clients and manages orders
public class ServerController extends AbstractServer {

    private DBConnection model;
    private ServerViewController view;
	private OrderController orderController;
	private OrderDAO orderDao;

    public ServerController(int port, DBConnection model, ServerViewController view) {
        super(port);
        this.model = model;
        this.view = view;
        this.orderDao = new OrderDAO(model);
        this.orderController = new OrderController(orderDao);
    }

    /**
     * add Clients to the GUI table and Logs the connection
     */
    @Override
    protected void clientConnected(ConnectionToClient client) {
        String ip = client.getInetAddress().getHostAddress();
        String host = client.getInetAddress().getHostName();
        view.addClient(ip, host);
        view.log("Client connected: " + ip + " (" + host + ")");
    }

    /**
     * removes the client from the GUI table and logs the disconnection
     */
    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        String ip = client.getInetAddress().getHostAddress();
        view.removeClient(ip);
        view.log("Client disconnected: " + ip);
    }

    /**
     *Handles messages received from clients, supports:
     * 1. Search order by ID
     * 2. Update order (date + number of guests)
     */
    @Override
    public void handleMessageFromClient(Object msg, ConnectionToClient client) {
        System.out.println("Message received: " + msg + " from " + client);

        String str = msg.toString().trim();  // המרה למחרוזת וביטול רווחים מיותרים
        int flag = 0;
        
        if (str.equals("GET_ALL_ORDERS")) {
        	try {
				List<Order> allOrders = orderController.getAllOrders();
				if(allOrders != null) {
					
					System.out.println("Server Found (search only)");
					this.sendToAllClients(allOrders.toString());
					return;
					
				}
				else {
					System.out.println("Not Found");
		            sendErrorToAllClients();
		            return;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        else if(str.equals("quit")) {
        	clientDisconnected(client);
        	sendToAllClients("Disconnecting the client from the server.");
        	
        }

        String[] parts = str.split(" ");

        if (parts.length == 0) {
            sendErrorToAllClients();
            return;
        }

        String orderNumFromMsg = parts[0];                  // order_number
        String orderDateFromMsg = (parts.length > 1) ? parts[1] : null;  // order_date (אופציונלי)
        String guestsFromMsg    = (parts.length > 2) ? parts[2] : null;  // number_of_guests (אופציונלי)

        int orderNumber;
        try {
            orderNumber = Integer.parseInt(orderNumFromMsg);  // כאן יכול לזרוק NumberFormatException
        } catch (NumberFormatException e) {
            System.out.println("Invalid order number: " + orderNumFromMsg);
            sendErrorToAllClients();
            return;
        }

        try {
            Order order = orderController.getOrder(orderNumber);

            if (order != null) { // נמצא
                if (orderDateFromMsg != null && guestsFromMsg != null) {
                    try {
                        java.sql.Date newDate = java.sql.Date.valueOf(orderDateFromMsg);
                        int newGuests = Integer.parseInt(guestsFromMsg);
                        order.setOrder_date(newDate);
                        order.setNumber_of_guests(newGuests);
                        // עדכון ההזמנה (צריך למלא את הפונקציה updateOrder עם הערכים)
                        orderController.updateOrder(order);

                        order = orderController.getOrder(orderNumber); // קבלת ההזמנה אחרי העדכון
                        System.out.println("Server Found (after save)");
                    } catch (Exception ex) {
                        System.out.println("Invalid date or guests number: " + ex.getMessage());
                        sendErrorToAllClients();
                        return;
                    }
                } else {
                    System.out.println("Server Found (search only)");
                }

                // שולחים לכולם את ההזמנה
                this.sendToAllClients(order.toString());
                flag = 1;
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorToAllClients();
            return;
        }

        if (flag != 1) {
            System.out.println("Not Found");
            sendErrorToAllClients();
        }
    }
    /**
     * Sends an error message to all connected clients.
     */

    private void sendErrorToAllClients() {
        try {
            this.sendToAllClients("Order Was not found, try again.");
        } catch (Exception ignore) {}
    }




}
