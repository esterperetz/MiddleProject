package clientLogic;

import java.sql.Date;

import clientGui.ClientUi;
import entities.ActionType;
import entities.Request;
import entities.ResourceType;
import entities.WaitingList;

/**
 * Logic class for handling Waiting List requests.
 */
public class WaitingListLogic {

	private final ClientUi client;

	public WaitingListLogic(ClientUi client) {
		this.client = client;
	}

	/**
	 * Requests the entire waiting list.
	 */
	public void getAllWaitingListCustomer() {

		Request req = new Request(ResourceType.WAITING_LIST, ActionType.GET_ALL_LIST, null, null);
		client.sendRequest(req);
	}

	/**
	 * Adds an entry to the waiting list.
	 * 
	 * @param waitingList The waiting list entry
	 */
	public void enterToWaitingList(WaitingList waitingList) {

		Request req = new Request(ResourceType.WAITING_LIST, ActionType.ENTER_WAITING_LIST, null, waitingList);
		client.sendRequest(req);
	}

	/**
	 * Requests waiting list entries for a specific date.
	 * 
	 * @param date The date to filter by
	 */
	public void getWaitingListByDate(java.time.LocalDate date) {
		java.sql.Date sqlDate = java.sql.Date.valueOf(date);
		Request req = new Request(ResourceType.WAITING_LIST, ActionType.GET_WAITING_LIST_BY_DATE, null, sqlDate);
		client.sendRequest(req);
	}

}
