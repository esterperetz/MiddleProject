package clientLogic;

import java.sql.Date;

import clientGui.ClientUi;
import entities.ActionType;
import entities.Request;
import entities.ResourceType;
import entities.WaitingList;

public class WaitingListLogic {

	private final ClientUi client;

	public WaitingListLogic(ClientUi client) {
		this.client = client;
	}

	public void getAllWaitingListCustomer() {

		Request req = new Request(ResourceType.WAITING_LIST, ActionType.GET_ALL_LIST, null, null);
		client.sendRequest(req);
	}

	public void enterToWaitingList(WaitingList waitingList) {

		Request req = new Request(ResourceType.WAITING_LIST, ActionType.ENTER_WAITING_LIST, null, waitingList);
		client.sendRequest(req);
	}
	public void getWaitingListByDate(java.time.LocalDate date) {
	    java.sql.Date sqlDate = java.sql.Date.valueOf(date);	    
	    Request req = new Request(ResourceType.WAITING_LIST, ActionType.GET_WAITING_LIST_BY_DATE, null, sqlDate);	    
	    client.sendRequest(req);
	}

}
