package clientLogic;

import clientGui.ClientUi;
import entities.ActionType;
import entities.Request;
import entities.ResourceType;

public class TableLogic {

	private final ClientUi client;

	public TableLogic(ClientUi client) {
		this.client = client;
	}

	public void getTable(int confomationCode, int subId) {
		Request req = new Request(ResourceType.TABLE, ActionType.GET, subId, confomationCode);
		client.sendRequest(req);
	}

	public void getAllTables() {
		Request req = new Request(ResourceType.TABLE, ActionType.GET_ALL, null, null);
		client.sendRequest(req);
	}

	public void createTable(entities.Table table) {
		Request req = new Request(ResourceType.TABLE, ActionType.CREATE, null, table);
		client.sendRequest(req);
	}

	public void updateTable(entities.Table table) {
		Request req = new Request(ResourceType.TABLE, ActionType.UPDATE, null, table);
		client.sendRequest(req);
	}

	public void deleteTable(int tableNumber) {
		Request req = new Request(ResourceType.TABLE, ActionType.DELETE, tableNumber, null);
		client.sendRequest(req);
	}
}
