package clientGui.managerTeam;

import java.net.URL;
import java.util.ResourceBundle;

import client.MessageListener;
import clientGui.ClientUi;
import clientGui.navigation.MainNavigator;
import clientGui.user.SubscriberOptionController;
import clientLogic.EmployeeLogic;
import clientLogic.UserLogic;
import entities.ActionType;
import entities.Employee;
import entities.Employee.Role;
import entities.Response;
import entities.Subscriber;
import entities.Order.OrderStatus;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class RegisterEmployeeController extends MainNavigator implements Initializable, MessageListener<Object> {
	@FXML
	private TextField txtUsername;

	@FXML
	private TextField txtPhone;

	@FXML
	private TextField txtEmail;

	@FXML
	private ComboBox<Role> selectRole;

	@FXML
	private Label lblMessage;

	private ActionEvent currentEvent; // Added to save the event for async navigation

	private boolean isManager;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		selectRole.getItems().setAll(Role.values());
	}

	public void initData(ClientUi clientUi, boolean isManager) {
		this.clientUi = clientUi;
		this.isManager = isManager;
	}

	/**
	 * Handles the registration process when "Register Now" is clicked.
	 */
	@FXML
	void handleRegisterBtn(ActionEvent event) {
		// 1. Get data from fields
		String username = txtUsername.getText();
		String phone = txtPhone.getText();
		String email = txtEmail.getText();
		Role role = selectRole.getValue();

		this.currentEvent = event; // Save current event
		selectRole.setValue(role);
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
			EmployeeLogic employee = new EmployeeLogic(clientUi);
			employee.registerEmployee(new Employee(username, "newEmployee1234",phone, email, role)); // CHANGED FROM
																										// 123456 TO 0
																										// (AUTO INC)
		} catch (Exception e) {
			System.out.println("employee register ");
		}

	}

	@Override
	public void onMessageReceive(Object msg) {
		try {
			if (msg instanceof Response) {
				Response res = (Response) msg;
				// Handle successful registration and navigate to Employee Options
				if (res.getAction() == ActionType.REGISTER_EMPLOYEE
						&& res.getStatus() == Response.ResponseStatus.SUCCESS) {
					Platform.runLater(() -> {
						// will send a mail to the employee to make his own password
						System.out.println(res.getMessage_from_server());
						ManagerOptionsController controller = super.loadScreen("managerTeam/EmployeeOption",
								currentEvent, clientUi);
						if (controller != null) {
							controller.initData(clientUi, isManager);
						}
					});
				} else if (res.getStatus() == Response.ResponseStatus.ERROR) {
					Platform.runLater(() -> lblMessage.setText(res.getMessage_from_server()));
				}
			} else
				System.out.println("nothing works");
		} catch (Exception e) {
			System.out.println("two ");
		}

	}

	/**
	 * Navigates back to the previous menu.
	 */
	@FXML
	void handleBackBtn(ActionEvent event) {
		// Fixed: Navigate back to SelectionScreen instead of Manager Dashboard
		ManagerOptionsController controller = super.loadScreen("managerTeam/EmployeeOption", event, clientUi);
		try {
			controller.initData(clientUi, this.isManager);
		} catch (NullPointerException e) {
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
