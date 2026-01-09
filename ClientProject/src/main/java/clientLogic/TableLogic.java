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

	public void getTable(int confomationCode,int subId ) {
		
		Request req = new Request(ResourceType.TABLE, ActionType.GET, subId, confomationCode);
		client.sendRequest(req);
	}
}
