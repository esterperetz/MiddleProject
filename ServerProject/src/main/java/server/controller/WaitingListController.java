package server.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import DAO.OrderDAO;
import DAO.WaitingListDAO;
import entities.ActionType;
import entities.Order;
import entities.Request;
import entities.ResourceType;
import entities.Response;
import entities.WaitingList;
import entities.Order.OrderStatus;
import ocsf.server.ConnectionToClient;

public class WaitingListController {

	private final WaitingListDAO waitingListDAO = new WaitingListDAO();
	private final OrderDAO orderDAO = new OrderDAO();

	public void handle(Request req, ConnectionToClient client) throws IOException {
		if (req.getResource() != ResourceType.WAITING_LIST) {
			client.sendToClient(new Response(req.getResource(), req.getAction(), Response.ResponseStatus.ERROR,
					"Incorrect resource type.", null));
			return;
		}

		try {
			switch (req.getAction()) {
			case GET_ALL:
				handleGetAll(req, client);
				break;

			case ENTER_WAITING_LIST:
				handleEnterWaitingList(req, client);
				break;

			case EXIT_WAITING_LIST:
				handleExitWaitingList(req, client);
				break;

			case PROMOTE_TO_ORDER:
				handlePromoteToOrder(req.getId(), client);
				break;

			default:
				client.sendToClient(new Response(ResourceType.WAITING_LIST, req.getAction(),
						Response.ResponseStatus.ERROR, "Unknown action", null));
				break;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			client.sendToClient(new Response(ResourceType.WAITING_LIST, req.getAction(),
					Response.ResponseStatus.DATABASE_ERROR, e.getMessage(), null));
		}
	}

	private void handleGetAll(Request req, ConnectionToClient client) throws SQLException, IOException {
		List<WaitingList> list = waitingListDAO.getAllWaitingList();
		client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.GET_ALL, Response.ResponseStatus.SUCCESS,
				null, list));
	}

	private void handleEnterWaitingList(Request req, ConnectionToClient client) throws SQLException, IOException {
		WaitingList item = (WaitingList) req.getPayload();
		int generatedCode = 1000 + (int) (Math.random() * 9000); // Generate 4-digit code
		item.setConfirmationCode(generatedCode);
		item.setEnterTime(new Date());

		if (waitingListDAO.enterWaitingList(item)) {
			client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.ENTER_WAITING_LIST,
					Response.ResponseStatus.SUCCESS, String.valueOf(generatedCode), true));
			sendListToAllClients();
		}
	}

	private void handleExitWaitingList(Request req, ConnectionToClient client) throws SQLException, IOException {
		if (req.getId() == null) {
			client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.EXIT_WAITING_LIST,
					Response.ResponseStatus.ERROR, "Missing ID", null));
			return;
		}
		if (waitingListDAO.exitWaitingList(req.getId())) {
			client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.EXIT_WAITING_LIST,
					Response.ResponseStatus.SUCCESS, "Removed", true));
			sendListToAllClients();
		}
	}

	public boolean handlePromoteToOrder(Integer waitingId, ConnectionToClient client) throws SQLException, IOException {
		if (waitingId == null) {
			if (client != null)
				client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.PROMOTE_TO_ORDER,
						Response.ResponseStatus.ERROR, "Missing ID", null));
			return false;
		}

		// 1. Fetch entry from waiting list
		WaitingList entry = waitingListDAO.getByWaitingId(waitingId);
		if (entry == null)
			return false;

		// 2. Create new APPROVED order object
		Order promotedOrder = new Order(0, new Date(), entry.getNumberOfGuests(), entry.getConfirmationCode(),
				entry.getCustomerId(), null, new Date(), null, null, 0.0, OrderStatus.APPROVED);

		// 3. Save to database and remove from waiting list
		if (orderDAO.createOrder(promotedOrder)) {
			waitingListDAO.exitWaitingList(waitingId);

			// 4. Sync all clients with updated lists
			List<WaitingList> updatedList = waitingListDAO.getAllWaitingList();
			Router.sendToAllClients(new Response(ResourceType.WAITING_LIST, ActionType.GET_ALL,
					Response.ResponseStatus.SUCCESS, null, updatedList));
			Router.sendToAllClients(new Response(ResourceType.ORDER, ActionType.GET_ALL,
					Response.ResponseStatus.SUCCESS, null, orderDAO.getAllOrders()));

			// 5. Notify the specific requester if exists
			if (client != null) {
				client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.PROMOTE_TO_ORDER,
						Response.ResponseStatus.SUCCESS, null, true));
			}
			return true;
		}
		return false;
	}

	private void sendListToAllClients() throws SQLException {
		List<WaitingList> list = waitingListDAO.getAllWaitingList();
		Router.sendToAllClients(new Response(ResourceType.WAITING_LIST, ActionType.GET_ALL,
				Response.ResponseStatus.SUCCESS, null, list));
	}
}