package server.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import DAO.OrderDAO;
import DAO.WaitingListDAO;
import Entities.ActionType;
import Entities.Order;
import Entities.Order.OrderStatus;
import Entities.Request;
import Entities.ResourceType;
import Entities.WaitingList;
import ocsf.server.ConnectionToClient;

public class WaitingListController {

    private final WaitingListDAO waitingListDAO = new WaitingListDAO();
    private final OrderDAO orderdao = new OrderDAO();

    public void handle(Request req, ConnectionToClient client) throws IOException {
        if (req.getResource() != ResourceType.WAITING_LIST) {
            client.sendToClient("Error: Incorrect resource type. Expected WAITING_LIST.");
            return;
        }

        try {
            switch (req.getAction()) {

            case GET_ALL: {
                List<WaitingList> list = waitingListDAO.getAllWaitingList();
                client.sendToClient(new Request(ResourceType.WAITING_LIST, ActionType.GET_ALL, null, list));
                break;
            }

            case ENTER_WAITING_LIST: {
                if (!(req.getPayload() instanceof WaitingList)) {
                    client.sendToClient("Error: ENTER_WAITING_LIST requires a WaitingList payload.");
                    break;
                }

                WaitingList item = (WaitingList) req.getPayload();

                int generatedCode = 1000 + (int) (Math.random() * 9000);
                item.setConfirmationCode(generatedCode);
                item.setEnterTime(new Date());

                boolean ok = waitingListDAO.enterWaitingList(item);
                client.sendToClient(new Request(ResourceType.WAITING_LIST, ActionType.ENTER_WAITING_LIST, generatedCode, ok));

                if (ok) {
                    sendListToAllClients();
                }
                break;
            }

            case EXIT_WAITING_LIST: {
                if (req.getId() == null) {
                    client.sendToClient("Error: ID required for EXIT_WAITING_LIST.");
                    break;
                }

                boolean ok = waitingListDAO.exitWaitingList(req.getId());
                client.sendToClient(ok ? "Success: Removed from waiting list." : "Error: Failed to remove from waiting list.");

                if (ok) {
                    sendListToAllClients();
                }
                break;
            }

            case PROMOTE_TO_ORDER: {
                if (req.getId() == null) {
                    client.sendToClient(new Request(ResourceType.WAITING_LIST, ActionType.PROMOTE_TO_ORDER, null, false));
                    break;
                }

                boolean promoted = promoteToOrder(req.getId(), client);
                client.sendToClient(new Request(ResourceType.WAITING_LIST, ActionType.PROMOTE_TO_ORDER, null, promoted));

                if (promoted) {
                    sendListToAllClients();
                    Router.sendToAllClients(new Request(ResourceType.ORDER, ActionType.GET_ALL, null, orderdao.getAllOrders()));
                }
                break;
            }

            default:
                client.sendToClient("Error: Unknown action for Waiting List.");
                break;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            client.sendToClient("Database Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            client.sendToClient("Error: " + e.getMessage());
        }
    }

    private boolean promoteToOrder(int waitingId, ConnectionToClient client) throws SQLException, IOException {
        WaitingList entry = waitingListDAO.getByWaitingId(waitingId);
        if (entry == null) {
            return false;
        }

        String idDetails = entry.getIdentificationDetails();
        if (idDetails == null || idDetails.isEmpty()) {
            throw new IllegalArgumentException("Identification details cannot be null or empty.");
        }

        Order promotedOrder = new Order(
        	    0,
        	    new Date(),
        	    entry.getNumberOfGuests(),
        	    entry.getConfirmationCode(),
        	    entry.getSubscriberId(),
        	    new Date(),
        	    null,
        	    null,
        	    null,
        	    null,
        	    null,
        	    0.0,
        	    OrderStatus.APPROVED
        	);
        if (orderdao.createOrder(promotedOrder)) {
            // Remove from waiting list and sync clients
            waitingListDAO.exitWaitingList(waitingId);
            client.sendToClient(new Request(ResourceType.WAITING_LIST, ActionType.PROMOTE_TO_ORDER, null, true));
            
            sendListToAllClients();
            // Sync the orders list because a new APPROVED order was added
            Router.sendToAllClients(new Request(ResourceType.ORDER, ActionType.GET_ALL, null, orderdao.getAllOrders()));
        }

        return false;
    }

    private void sendListToAllClients() {
        try {
            List<WaitingList> list = waitingListDAO.getAllWaitingList();
            Router.sendToAllClients(new Request(ResourceType.WAITING_LIST, ActionType.GET_ALL, null, list));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
