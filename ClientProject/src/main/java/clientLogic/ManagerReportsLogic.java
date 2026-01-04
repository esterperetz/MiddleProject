package clientLogic;

import clientGui.ClientUi;
import entities.ActionType;
import entities.Request;
import entities.ResourceType;

public class ManagerReportsLogic {

	
	private final ClientUi client;

	public ManagerReportsLogic(ClientUi client) {
		this.client = client;
	}
	
	
	public void getReports() {
		Request req = new Request(ResourceType.REPORT, ActionType.GET_MONTHLY_REPORT, null, null);
        client.sendRequest(req);
	}
	
	
	
}
