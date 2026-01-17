package clientGui.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import client.MessageListener;
import clientGui.ClientUi;
import clientGui.managerTeam.ManagerOptionsController;
import clientGui.navigation.MainNavigator;
import clientLogic.EmployeeLogic;
import clientLogic.UserLogic;
import entities.Response;
import entities.Response.ResponseStatus;
import entities.Alarm;
import entities.Customer;
import entities.CustomerType;
import entities.Employee;
import javafx.application.Platform;

/**
 * Controller for registering a new subscriber.
 * Handles user input and communication with the server to create a subscriber.
 */
public class RegisterSubscriberController extends MainNavigator implements MessageListener<Object> {
	@FXML
	private TextField txtUsername;

	@FXML
	private TextField txtPhone;

	@FXML
	private TextField txtEmail;

	@FXML
	private Label lblMessage;
	private Employee.Role isManager;
	private UserLogic UserLogic;
	private ActionEvent currentEvent;
	private EmployeeLogic employeeLogic;

	private Employee emp;

	/**
	 * Initializes the controller with employee and session data.
	 * 
	 * @param emp       The logged-in employee
	 * @param clientUi  The client UI instance
	 * @param isManager The role of the employee
	 */
	public void initData(Employee emp, ClientUi clientUi, Employee.Role isManager) {
		this.emp = emp;
		this.clientUi = clientUi;
		this.isManager = isManager;
		employeeLogic = new EmployeeLogic(clientUi);
	}

	/**
	 * Handles the registration process when "Register Now" is clicked.
	 * 
	 * @param event The action event
	 */
	@FXML
	void handleRegisterBtn(ActionEvent event) {
		// 1. Get data from fields
		String username = txtUsername.getText();
		String phone = txtPhone.getText();
		String email = txtEmail.getText();

		// Save current event

		// 2. Validate Input (Basic checks)
		if (username.isEmpty() || phone.isEmpty() || email.isEmpty()) {
			lblMessage.setText("Error: All fields are required!");
			lblMessage.setStyle("-fx-text-fill: #ff6b6b;"); // Red color
			return;
		}

		if (!email.contains("@")) {
			lblMessage.setText("Error: Invalid email format.");
			lblMessage.setStyle("-fx-text-fill: #ff6b6b;");
			return;
		}
		try {
			// Auto-increment ID is handled by server, passing 0 as placeholder
			employeeLogic.createSubscriber(new Customer(0, 0, username, phone, email, CustomerType.SUBSCRIBER));
			this.currentEvent = event;
		} catch (Exception e) {
			System.out.println("Error creating subscriber");
			e.printStackTrace();
		}

	}

	@Override
	public void onMessageReceive(Object msg) {
		if (!(msg instanceof Response))
			return;
		Response res = (Response) msg;

		Platform.runLater(() -> {
			try {
				switch (res.getResource()) {
					case EMPLOYEE:
						handleUserResponse(res);
						break;
					default:
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private void handleUserResponse(Response res) {
		if (res.getStatus() == ResponseStatus.SUCCESS) {
			try {
				ManagerOptionsController controller = super.loadScreen(
						"managerTeam/EmployeeOption",
						currentEvent,
						clientUi);

				if (controller != null) {
					Alarm.showAlert("SUCCESS", "Subscriber added successfully", AlertType.INFORMATION);
					controller.initData(emp, this.clientUi, this.isManager);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (res.getStatus() == ResponseStatus.ERROR) {
			if (lblMessage != null) {
				lblMessage.setText(res.getMessage_from_server());
			}
		}
	}

	/**
	 * Navigates back to the previous menu.
	 * 
	 * @param event The action event
	 */
	@FXML
	void handleBackBtn(ActionEvent event) {
		ManagerOptionsController controller = super.loadScreen("managerTeam/EmployeeOption", event, clientUi);
		if (controller != null) {
			controller.initData(emp, clientUi, this.isManager);
		} else {
			System.err.println("Error: Could not load ManagerOptionsController.");
		}
	}

	/**
	 * Clears the input fields after successful registration.
	 */
	private void clearFields() {
		txtUsername.clear();
		txtPhone.clear();
		txtEmail.clear();
	}
}