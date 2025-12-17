package server.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import DAO.WaitingListDAO;
import Entities.ActionType;
import Entities.Request;
import Entities.ResourceType;
import Entities.WaitingList;
import ocsf.server.ConnectionToClient;

public class WaitingListController {

    private final WaitingListDAO waitingListDAO = new WaitingListDAO();

    public void handle(Request req, ConnectionToClient client) throws IOException {
        if (req.getResource() != ResourceType.WAITING_LIST) {
            client.sendToClient("Error: Incorrect resource type. Expected WAITING_LIST.");
            return;
        }

        try {
            switch (req.getAction()) {
                case GET_ALL:
                    List<WaitingList> list = waitingListDAO.getAllWaitingList();
                    client.sendToClient(new Request(ResourceType.WAITING_LIST, ActionType.GET_ALL, null, list));
                    break;

                case ENTER_WAITING_LIST: 
                    if (req.getPayload() instanceof WaitingList) {
                        WaitingList newItem = (WaitingList) req.getPayload();
                        
                        if (newItem.getIdentificationDetails() == null || newItem.getIdentificationDetails().isEmpty()) {
                             client.sendToClient("Error: Identification details are mandatory.");
                             return;
                        }

                        if (waitingListDAO.enterWaitingList(newItem)) {
                            client.sendToClient("Success: Added to waiting list.");
                            sendListToAllClients(); 
                        } else {
                            client.sendToClient("Error: Failed to add to waiting list.");
                        }
                    }
                    break;

                case EXIT_WAITING_LIST:
                    if (req.getId() != null) {
                        if (waitingListDAO.exitWaitingList(req.getId())) {
                            client.sendToClient("Success: Removed from waiting list.");
                            sendListToAllClients();
                        } else {
                            client.sendToClient("Error: Failed to remove from waiting list.");
                        }
                    } else {
                        client.sendToClient("Error: ID required for EXIT.");
                    }
                    break;

                default:
                    client.sendToClient("Error: Unknown action for Waiting List.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            client.sendToClient("Database Error: " + e.getMessage());
        }
    }

    private void sendListToAllClients() {
        try {
            List<WaitingList> list = waitingListDAO.getAllWaitingList();
            Request updateMsg = new Request(ResourceType.WAITING_LIST, ActionType.GET_ALL, null, list);
            Router.sendToAllClients(updateMsg);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}