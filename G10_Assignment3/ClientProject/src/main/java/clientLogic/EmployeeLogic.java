package clientLogic;

import java.sql.Date;

import clientGui.ClientUi;
import entities.ActionType;
import entities.Customer;
import entities.Employee;
import entities.OpeningHours;
import entities.Request;
import entities.ResourceType;

/**
 * Logic class for handling Employee-related requests.
 * Constructs requests for login, registration, password updates, and subscriber
 * management.
 */
public class EmployeeLogic {
	private final ClientUi client;

	public EmployeeLogic(ClientUi client) {
		this.client = client;
	}

	/**
	 * Sends a login request for an employee.
	 * 
	 * @param employee The employee credentials
	 */
	public void loginEmployee(Employee employee) {
		Request req = new Request(ResourceType.EMPLOYEE, ActionType.LOGIN, null, employee);
		client.sendRequest(req);
	}

	/**
	 * Sends a request to register a new employee.
	 * 
	 * @param employee The new employee details
	 */
	public void registerEmployee(Employee employee) {
		Request req = new Request(ResourceType.EMPLOYEE, ActionType.REGISTER_EMPLOYEE, null, employee);
		client.sendRequest(req);
	}

	/**
	 * Sends a request to update an employee's password.
	 * 
	 * @param employee The employee with updated password
	 */
	public void updatePassword(Employee employee) {
		Request req = new Request(ResourceType.EMPLOYEE, ActionType.UPDATE, null, employee);
		client.sendRequest(req);
	}

	/**
	 * Sends a request to create new opening hours.
	 * 
	 * @param openingHours The opening hours to create
	 */
	public void createOpeningHours(OpeningHours openingHours) {
		Request req = new Request(ResourceType.BUSINESS_HOUR, ActionType.CREATE, null, openingHours);
		client.sendRequest(req);
	}

	/**
	 * Sends a request to cancel/delete opening hours for a specific date.
	 * 
	 * @param date The date to delete opening hours for
	 */
	public void cancelOpeningHours(Date date) {
		Request req = new Request(ResourceType.BUSINESS_HOUR, ActionType.DELETE, null, date);
		client.sendRequest(req);
	}

	/**
	 * Sends a request to create a new subscriber.
	 * 
	 * @param customer The customer details to promote to subscriber
	 */
	public void createSubscriber(Customer customer) {
		// System.out.println("in create client"); // Debug print
		Request req = new Request(ResourceType.EMPLOYEE, ActionType.REGISTER_SUBSCRIBER, null, customer);
		client.sendRequest(req);
	}

	/**
	 * Sends a request to get all opening hours.
	 */
	public void getAllOpeningHours() {
		Request req = new Request(ResourceType.BUSINESS_HOUR, ActionType.GET_ALL, null, null);
		client.sendRequest(req);
	}
}
