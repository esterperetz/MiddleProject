package server.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import DAO.CustomerDAO;
import DAO.OrderDAO;
import DAO.TableDAO;
import entities.ActionType;
import entities.Customer;
import entities.Order;
import entities.Request;
import entities.ResourceType;
import entities.Response;
import entities.Table;
import ocsf.server.ConnectionToClient;

public class TableController {

	private final TableDAO tableDAO = new TableDAO();
	private final OrderDAO orderDAO = new OrderDAO();
	private final CustomerDAO customerDAO = new CustomerDAO();

	public void handle(Request req, ConnectionToClient client) throws IOException {
		if (req.getResource() != ResourceType.TABLE) {
			client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.ERROR,
					"Error: Incorrect resource type. Expected TABLE.", null));
			return;
		}

		try {
			switch (req.getAction()) {
			case GET_ALL:
				handleGetAll(client);
				break;
			case GET:
				handleGetTable(req, client);
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
			default:
				client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.ERROR,
						"Error: Unknown action for Table resource.", null));

			}
		} catch (SQLException e) {
			client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.ERROR,
					"Database Error: ", e.getMessage()));
		}
	}

	// --- Action Handlers ---

	private void handleGetAll(ConnectionToClient client) throws IOException, SQLException {
		List<Table> tables = tableDAO.getAllTables();
		client.sendToClient(new Response(ResourceType.TABLE, ActionType.GET_ALL, Response.ResponseStatus.SUCCESS,
				"get all tables", tables));
	}

	private void handleGetTable(Request req, ConnectionToClient client) throws IOException, SQLException {
		int conformationCode = (int) req.getPayload();
		int subscriberCode = (int)req.getId();
		System.out.println("the code is: ------ " + conformationCode);
		Customer customer = customerDAO.getCustomerBySubscriberCode(subscriberCode);
		if(customer == null) {
			client.sendToClient(new Response(ResourceType.TABLE, ActionType.GET, Response.ResponseStatus.ERROR,
					"CANOT find coustomerId by subscriber code ", null));
			return;
		}
		Order order = orderDAO.getOrderByConfirmationCodeApproved(conformationCode,customer.getCustomerId());
		if (order == null) {
			client.sendToClient(new Response(ResourceType.TABLE, ActionType.GET, Response.ResponseStatus.ERROR,
					"CANOT find order with this conformation code", null));
			return;
		}

		Integer tableNumber = tableDAO.findAvailableTable(order.getNumberOfGuests());

		if (tableNumber == null) {
			client.sendToClient(new Response(ResourceType.TABLE, ActionType.GET, Response.ResponseStatus.ERROR,
					"CANOT find suit table", null));
			return;
		}

		boolean updateOrder = orderDAO.updateOrderSeating(order.getOrderNumber(), tableNumber,
				Order.OrderStatus.SEATED);
		boolean update = tableDAO.updateTableStatus(tableNumber, 1);
		if (!update || !updateOrder) {
			client.sendToClient(new Response(ResourceType.TABLE, ActionType.GET, Response.ResponseStatus.DATABASE_ERROR,
					"CANNOT update", null));
			return;
		}
		client.sendToClient(new Response(ResourceType.TABLE, ActionType.GET, Response.ResponseStatus.SUCCESS,
				"get tableNumber", tableNumber));

	}

	private void handleCreate(Request req, ConnectionToClient client) throws IOException, SQLException {
		Table newTable = (Table) req.getPayload();
		if (newTable == null) {
			client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.ERROR,
					"Error: Invalid payload. Expected Table object.", null));
			return;
		}

		if (tableDAO.getTable(newTable.getTableNumber()) != null) {
			client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.ERROR,
					"Error: Table number already exists.", null));

			return;
		}

		if (tableDAO.addTable(newTable)) {
			client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.SUCCESS,
					"Success: Table added.", newTable));

		} else {
			client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.ERROR,
					"Error: Failed to add table.", null));

		}
	}

	private void handleUpdate(Request req, ConnectionToClient client) throws IOException, SQLException {
		Table updateTable = (Table) req.getPayload();
		if (updateTable == null) {
			client.sendToClient(new Response(ResourceType.TABLE, ActionType.UPDATE, Response.ResponseStatus.ERROR,
					"Error: Invalid payload. Expected Table object.", null));
			return;
		}

		if (tableDAO.updateTable(updateTable)) {
			client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.SUCCESS,
					"Success: Table updated.", updateTable));

		} else {
			client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.ERROR,
					"Error: Failed to update table.", null));

		}
	}

	private void handleDelete(Request req, ConnectionToClient client) throws IOException, SQLException {
		if (req.getId() == null) {
			client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.ERROR,
					"Error: ID required for DELETE.", null));
			return;
		}

		if (tableDAO.deleteTable(req.getId())) {
			client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.SUCCESS,
					"Success: Table deleted.", req.getId()));
		} else {
			client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.ERROR,
					"Error: Failed to delete table.", null));

		}
	}

//    private void sendTablesToAllClients() throws SQLException, IOException {
//        List<Table> tables = tableDAO.getAllTables();
//        Request updateMsg = new Request(ResourceType.TABLE, ActionType.GET_ALL, null, tables);
//        Router.sendToAllClients(updateMsg);
//    }
}