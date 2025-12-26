package clientLogic;

import clientGui.ClientUi;
import entities.ActionType;
import entities.Employee;
import entities.Request;
import entities.ResourceType;

public class EmployeeLogic {
	private final ClientUi client;
	
	public EmployeeLogic(ClientUi client) {
        this.client = client;
    }
	
	public void getManagerByEmployee(Employee employee) {
		Request req = new Request(ResourceType.EMPLOYEE, ActionType.LOGIN, null, employee);
		client.sendRequest(req);
	}
}
