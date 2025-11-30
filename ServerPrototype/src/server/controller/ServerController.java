package server.controller;

import DAO.OrderDAO;
import DBConnection.DBConnection;
import Entities.Order;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import server.gui.ServerViewController;

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

    @Override
    protected void clientConnected(ConnectionToClient client) {
        String ip = client.getInetAddress().getHostAddress();
        String host = client.getInetAddress().getHostName();
        view.addClient(ip, host);
        view.log("Client connected: " + ip + " (" + host + ")");
    }

    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        String ip = client.getInetAddress().getHostAddress();
        view.removeClient(ip);
        view.log("Client disconnected: " + ip);
    }

    @Override
    public void handleMessageFromClient(Object msg, ConnectionToClient client) {
        System.out.println("Message received: " + msg + " from " + client);

        String str = (String) msg;
        int flag = 0;

        String[] parts = str.split(" ");

        String orderNumFromMsg = parts[0];                  // order_number
        String orderDateFromMsg = (parts.length > 1) ? parts[1] : null;  // order_date (אופציונלי)
        String guestsFromMsg    = (parts.length > 2) ? parts[2] : null;  // number_of_guests (אופציונלי)

        try {
            int orderNumber = Integer.parseInt(orderNumFromMsg);
            Order order = orderController.getOrder(orderNumber);

            if (order != null) { // Found
                if (orderDateFromMsg != null && guestsFromMsg != null) {

                    java.sql.Date newDate = java.sql.Date.valueOf(orderDateFromMsg);
                    int newGuests = Integer.parseInt(guestsFromMsg);

                    orderController.updateOrder(order);

                    order = orderController.getOrder(orderNumber);

                    System.out.println("Server Found (after save)");
                } else {
                    System.out.println("Server Found (search only)");
                }

                // שולחים לכולם את ההזמנה
                this.sendToAllClients(order.toString());
                flag = 1;
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                this.sendToAllClients("Error");
            } catch (Exception ignore) {}
            return;
        }

        if (flag != 1) {
            System.out.println("Not Found");
            this.sendToAllClients("Error");
        }
    }



}
