package clientLogic;

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

		public void enterToWaitingList(WaitingList waitingList) {
			
			Request req = new Request(ResourceType.WAITING_LIST, ActionType.ENTER_WAITING_LIST, null, waitingList);
			client.sendRequest(req);
		}


}
