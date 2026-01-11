package clientLogic;

import java.sql.Date;

import clientGui.ClientUi;
import entities.ActionType;
import entities.Customer;
import entities.Employee;
import entities.OpeningHours;
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
	
	public void createOpeningHours(OpeningHours openingHours) {
		Request req = new Request(ResourceType.BUSINESS_HOUR, ActionType.CREATE, null, openingHours);
		client.sendRequest(req);
	}
	
	public void cancelOpeningHours(Date date) {
		Request req = new Request(ResourceType.BUSINESS_HOUR, ActionType.DELETE, null, date);
		client.sendRequest(req);
	}
	public void createSubscriber(Customer customer) {
		System.out.println("in create client");
		Request req = new Request(ResourceType.EMPLOYEE, ActionType.REGISTER_SUBSCRIBER, null, customer);
		client.sendRequest(req);
	}
}
