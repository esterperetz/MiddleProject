package server.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import DAO.TableDAO;
import entities.ActionType;
import entities.Request;
import entities.ResourceType;
import entities.Table;
import ocsf.server.ConnectionToClient;

public class TableController {

	private final TableDAO tableDAO = new TableDAO();

	public void handle(Request req, ConnectionToClient client) throws IOException {
        if (req.getResource() != ResourceType.TABLE) {
             client.sendToClient("Error: Incorrect resource type. Expected TABLE.");
             return; 
        }

        try {
            switch (req.getAction()) {
                case GET_ALL:
                    List<Table> tables = tableDAO.getAllTables();
                    client.sendToClient(new Request(ResourceType.TABLE, ActionType.GET_ALL, null, tables));
                    break;

                case CREATE:
                    if (req.getPayload() instanceof Table) {
                        Table newTable = (Table) req.getPayload();
                        if (tableDAO.getTable(newTable.getTableNumber()) != null) {
                            client.sendToClient("Error: Table number already exists.");
                            return;
                        }
                        
                        if (tableDAO.addTable(newTable)) {
                            client.sendToClient("Success: Table added.");
                            sendTablesToAllClients(); 
                        } else {
                            client.sendToClient("Error: Failed to add table.");
                        }
                    }
                    break;

                case UPDATE: 
                    if (req.getPayload() instanceof Table) {
                        Table updateTable = (Table) req.getPayload();
                        if (tableDAO.updateTable(updateTable)) {
                            client.sendToClient("Success: Table updated.");
                            sendTablesToAllClients();
                        } else {
                            client.sendToClient("Error: Failed to update table.");
                        }
                    }
                    break;

                case DELETE:
                    if (req.getId() != null) {
                        if (tableDAO.deleteTable(req.getId())) {
                            client.sendToClient("Success: Table deleted.");
                            sendTablesToAllClients();
                        } else {
                            client.sendToClient("Error: Failed to delete table.");
                        }
                    } else {
                        client.sendToClient("Error: ID required for DELETE.");
                    }
                    break;

                default:
                    client.sendToClient("Error: Unknown action for Table resource.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            client.sendToClient("Database Error: " + e.getMessage());
        }
    }

	private void sendTablesToAllClients() {
		try {
			List<Table> tables = tableDAO.getAllTables();
			Request updateMsg = new Request(ResourceType.TABLE, ActionType.GET_ALL, null, tables);
			Router.sendToAllClients(updateMsg);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}