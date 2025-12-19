package server.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import DAO.OrderDAO;
import DAO.TableDAO;
import DAO.WaitingListDAO;
import Entities.*;
import ocsf.server.ConnectionToClient;

public class WaitingListController {

	private final WaitingListDAO waitingListDAO = new WaitingListDAO();
	private final OrderDAO orderdao = new OrderDAO();
	private final TableDAO tabledao = new TableDAO();

	public void handle(Request req, ConnectionToClient client) throws IOException {
		if (req.getResource() != ResourceType.WAITING_LIST)
			return;

		try {
			switch (req.getAction()) {
			case GET_ALL:
				client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.GET_ALL,
						Response.ResponseStatus.SUCCESS, null, waitingListDAO.getAllWaitingList()));
				break;

			case ENTER_WAITING_LIST:
				processEntry(req, client);
				break;

			case EXIT_WAITING_LIST:
				if (req.getId() != null && waitingListDAO.exitWaitingList(req.getId())) {
					sendListToAllClients();
				}
				break;
			// Manual PROMOTE_TO_ORDER removed to ensure automation only
			default:
				break;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * * Logic used by the background thread to promote a customer. Transitions from
	 * WaitingList to SEATED Order.
	 */
	public boolean autoPromote(WaitingList entry) throws SQLException {
		// Try to find and lock a table atomically
		Integer tableNum = tabledao.findAndOccupyTable(entry.getNumberOfGuests());
		if (tableNum == null)
			return false;

		Order promotedOrder = new Order(0, new Date(), entry.getNumberOfGuests(), entry.getConfirmationCode(),
				entry.getSubscriberId(), new Date(), entry.getFullName(), "N/A", "N/A", new Date(), 0.0,
				Order.OrderStatus.SEATED);
		promotedOrder.setTable_number(tableNum);

		if (orderdao.createOrder(promotedOrder)) {
			waitingListDAO.exitWaitingList(entry.getWaitingId());
			sendListToAllClients();
			// Sync all orders and tables for all clients
			Router.sendToAllClients(new Response(ResourceType.ORDER, ActionType.GET_ALL,
					Response.ResponseStatus.SUCCESS, null, orderdao.getAllOrders()));
			Router.sendToAllClients(new Response(ResourceType.TABLE, ActionType.GET_ALL,
					Response.ResponseStatus.SUCCESS, null, tabledao.getAllTables()));
			return true;
		} else {
			tabledao.updateTableStatus(tableNum, false); // Rollback if order fails
			return false;
		}
	}

	private void processEntry(Request req, ConnectionToClient client) throws SQLException, IOException {
		WaitingList item = (WaitingList) req.getPayload();
		int code = 1000 + (int) (Math.random() * 9000);
		item.setConfirmationCode(code);
		item.setEnterTime(new Date());

		if (waitingListDAO.enterWaitingList(item)) {
			client.sendToClient(new Response(ResourceType.WAITING_LIST, ActionType.ENTER_WAITING_LIST,
					Response.ResponseStatus.SUCCESS, "Entered list", code));
			sendListToAllClients();
		}
	}

	public void sendListToAllClients() {
		try {
			List<WaitingList> list = waitingListDAO.getAllWaitingList();
			Router.sendToAllClients(new Response(ResourceType.WAITING_LIST, ActionType.GET_ALL,
					Response.ResponseStatus.SUCCESS, null, list));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}