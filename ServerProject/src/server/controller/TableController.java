package server.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import DAO.TableDAO;
import Entities.ActionType;
import Entities.Request;
import Entities.ResourceType;
import Entities.Response;
import Entities.Table;
import ocsf.server.ConnectionToClient;

public class TableController {

	private final TableDAO tableDAO = new TableDAO();

	public void handle(Request req, ConnectionToClient client) throws IOException {
        if (req.getResource() != ResourceType.TABLE) {
             client.sendToClient(new Response(req.getResource(), req.getAction(), Response.ResponseStatus.ERROR, "Invalid resource", null));
             return; 
        }

        try {
            switch (req.getAction()) {
                case GET_ALL:
                    List<Table> tables = tableDAO.getAllTables();
                    client.sendToClient(new Response(ResourceType.TABLE, ActionType.GET_ALL, Response.ResponseStatus.SUCCESS, null, tables));
                    break;

                case CREATE:
                    if (req.getPayload() instanceof Table) {
                        Table newTable = (Table) req.getPayload();
                        if (tableDAO.getTable(newTable.getTableNumber()) != null) {
                            client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.ERROR, "Table exists", null));
                            return;
                        }
                        if (tableDAO.addTable(newTable)) {
                            client.sendToClient(new Response(ResourceType.TABLE, ActionType.CREATE, Response.ResponseStatus.SUCCESS, "Added", null));
                            sendTablesToAllClients(); 
                        }
                    }
                    break;

                case UPDATE: 
                    if (req.getPayload() instanceof Table) {
                        Table updateTable = (Table) req.getPayload();
                        if (tableDAO.updateTable(updateTable)) {
                            client.sendToClient(new Response(ResourceType.TABLE, ActionType.UPDATE, Response.ResponseStatus.SUCCESS, "Updated", null));
                            sendTablesToAllClients();
                        }
                    }
                    break;

                case DELETE:
                    if (req.getId() != null && tableDAO.deleteTable(req.getId())) {
                        client.sendToClient(new Response(ResourceType.TABLE, ActionType.DELETE, Response.ResponseStatus.SUCCESS, "Deleted", null));
                        sendTablesToAllClients();
                    }
                    break;

                default:
                    client.sendToClient(new Response(ResourceType.TABLE, req.getAction(), Response.ResponseStatus.ERROR, "Unknown action", null));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            client.sendToClient(new Response(ResourceType.TABLE, req.getAction(), Response.ResponseStatus.DATABASE_ERROR, e.getMessage(), null));
        }
    }

	private void sendTablesToAllClients() {
		try {
			List<Table> tables = tableDAO.getAllTables();
			Response updateMsg = new Response(ResourceType.TABLE, ActionType.GET_ALL, Response.ResponseStatus.SUCCESS, null, tables);
			Router.sendToAllClients(updateMsg);
		} catch (SQLException e) { e.printStackTrace(); }
	}
}