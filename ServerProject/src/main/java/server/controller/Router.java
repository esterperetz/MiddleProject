package server.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import entities.Request;
import entities.ResourceType;
import ocsf.server.ConnectionToClient;


public class Router {

	private final OrderController orderController;
	private final CustomerController customerController;
	private final TableController tableController;
	private final WaitingListController waitingListController;
	private final BusinessHourController businessHourController;
	private final EmployeeController employeeController;
	private final ReportController reportController;
	private static List<ConnectionToClient> clients;

	public Router() {
		this.orderController = new OrderController();
		this.customerController = new CustomerController();
		this.tableController = new TableController();
		this.waitingListController = new WaitingListController();
		this.businessHourController = new BusinessHourController();
		this.employeeController = new EmployeeController();
		this.reportController = new ReportController(); 
		
		if (clients == null) {
			clients = new ArrayList<>();
		}
	}

	public void route(Request req, ConnectionToClient client) throws IOException, SQLException {
		ResourceType resource = req.getResource();

		switch (resource) {
		case ORDER:
			orderController.handle(req, client, clients);
			break;

		case CUSTOMER:
			customerController.handle(req, client);
			break;

		case TABLE:
			tableController.handle(req, client);
			break;

		case WAITING_LIST:
			waitingListController.handle(req, client);
			break;
		case BUSINESS_HOUR:
			businessHourController.handle(req, client);
            break;
		case EMPLOYEE: 
			employeeController.handle(req, client);
            break;
		case REPORT:
			reportController.handle(req, client);
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