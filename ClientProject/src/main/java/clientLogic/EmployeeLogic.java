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
	//change method name to loginEmployee
	public void loginEmployee(Employee employee) {
		Request req = new Request(ResourceType.EMPLOYEE, ActionType.LOGIN, null, employee);
		client.sendRequest(req);
	}

	public void registerEmployee(Employee employee) {
		Request req = new Request(ResourceType.EMPLOYEE, ActionType.REGISTER_EMPLOYEE, null, employee);
		client.sendRequest(req);
	}
	
	public void updatePassword(Employee employee) {
		Request req = new Request(ResourceType.EMPLOYEE, ActionType.UPDATE, null, employee);
		client.sendRequest(req);
	}
}
