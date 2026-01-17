package clientLogic;

import clientGui.ClientUi;
import entities.ActionType;
import entities.Request;
import entities.ResourceType;

/**
 * Logic class for handling Table-related requests.
 * Constructs requests for creating, updating, deleting, and retrieving tables.
 */
public class TableLogic {

	private final ClientUi client;

	public TableLogic(ClientUi client) {
		this.client = client;
	}

	/**
	 * Requests a specific table by confirmation code and subscriber ID.
	 * 
	 * @param confomationCode The confirmation code
	 * @param subId           The subscriber ID
	 */
	public void getTable(int confomationCode, int subId) {
		Request req = new Request(ResourceType.TABLE, ActionType.GET, subId, confomationCode);
		client.sendRequest(req);
	}

	/**
	 * Requests all tables from the server.
	 */
	public void getAllTables() {
		Request req = new Request(ResourceType.TABLE, ActionType.GET_ALL, null, null);
		client.sendRequest(req);
	}

	/**
	 * Creates a new table.
	 * 
	 * @param table The table to create
	 */
	public void createTable(entities.Table table) {
		Request req = new Request(ResourceType.TABLE, ActionType.CREATE, null, table);
		client.sendRequest(req);
	}

	/**
	 * Updates an existing table.
	 * 
	 * @param table The updated table object
	 */
	public void updateTable(entities.Table table) {
		Request req = new Request(ResourceType.TABLE, ActionType.UPDATE, null, table);
		client.sendRequest(req);
	}

	/**
	 * Deletes a table.
	 * 
	 * @param tableNumber The table number to delete
	 */
	public void deleteTable(int tableNumber) {
		Request req = new Request(ResourceType.TABLE, ActionType.DELETE, tableNumber, null);
		client.sendRequest(req);
	}
}
