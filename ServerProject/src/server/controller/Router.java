package server.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Entities.Request;
import Entities.ResourceType;
import ocsf.server.ConnectionToClient;

public class Router {

	private final OrderController orderController;
	private final SubscriberController subscriberController;
	private final TableController tableController;
	private final WaitingListController waitingListController;
	private final EmployeeLoginController employeeLoginController;
	private static List<ConnectionToClient> clients;

	public Router() {
		this.orderController = new OrderController();
		this.subscriberController = new SubscriberController();
		this.tableController = new TableController();
		this.waitingListController = new WaitingListController();
		this.employeeLoginController = new EmployeeLoginController();

		if (clients == null) {
			clients = new ArrayList<>();
		}
	}

	public void route(Request req, ConnectionToClient client) throws IOException, SQLException {
		ResourceType resource = req.getResource();

		switch (resource) {
		case ORDER:
			orderController.handle(req, client);
			break;

		case SUBSCRIBER:
			subscriberController.handle(req, client);
			break;

		case TABLE:
			tableController.handle(req, client);
			break;

		case WAITING_LIST:
			waitingListController.handle(req, client);
			break;
			
		case EMPLOYEE:
			employeeLoginController.handle(req, client);
			break;

		default:
			client.sendToClient("Unknown resource type: " + resource);
		}
	}

	public List<ConnectionToClient> getClients() {
		return clients;
	}

	public void setClients(List<ConnectionToClient> clientsList) {
		clients = clientsList;
	}

	public void addClientOnline(ConnectionToClient client) {
		clients.add(client);
	}

	public void removeClientOffline(ConnectionToClient client) {
		clients.remove(client);
	}

	public static void sendToAllClients(Object message) {
		if (clients != null) {
			for (ConnectionToClient c : clients) {
				try {
					if (c.isAlive()) {
						c.sendToClient(message);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}